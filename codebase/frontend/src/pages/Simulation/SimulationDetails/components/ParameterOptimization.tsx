import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Checkbox } from '@/components/ui/checkbox';
import { TrendingUp, Loader2, Copy, Gauge, BatteryFull, BarChart2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { SimulationParameters, ExpandedSections, SavedConfig } from '@/types/simulationControl';
import { ParameterOptimizationProps } from '@/types/simulation';
import { optimizationService } from '@/services/optimizationService';

export default function ParameterOptimization(props: ParameterOptimizationProps) {
  const navigate = useNavigate();
  const [keepSettings, setKeepSettings] = useState<Record<string, boolean>>({});
  const [keepPackageInterval, setKeepPackageInterval] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);
  const [apiSuggestions, setApiSuggestions] = useState<{
    packagesPerHourPrediction: SimulationParameters;
    energyPerHourPrediction: SimulationParameters;
    energyPerPackagePredicition: SimulationParameters;
  } | null>(null);
  const [optimizationGoal, setOptimizationGoal] = useState<
    'energy-per-package' | 'packages-per-hour' | 'energy-per-hour'
  >('energy-per-package');

  const machinePowerMultipliers = props.parameters.machinePowerMultipliers;

  useEffect(() => {
    if (props.parameters?.machinePowerMultipliers && Object.keys(keepSettings).length === 0) {
      const initialSettings = Object.keys(props.parameters.machinePowerMultipliers).reduce(
        (acc, machine) => ({
          ...acc,
          [machine]: false,
        }),
        {}
      );
      setKeepSettings(initialSettings);
    }
  }, [props.parameters?.machinePowerMultipliers, keepSettings]);

  useEffect(() => {
    if (showSuggestions) {
      setShowSuggestions(false);
    }
  }, [optimizationGoal]);

  const handleKeepSettingChange = (machine: string, checked: boolean) => {
    setKeepSettings((prev) => ({
      ...prev,
      [machine]: checked,
    }));
  };

  // Helper function to get the selected suggestion based on optimization goal
  const getSelectedSuggestion = (): SimulationParameters | null => {
    if (!apiSuggestions) return null;

    switch (optimizationGoal) {
      case 'energy-per-package':
        return apiSuggestions.energyPerPackagePredicition;
      case 'packages-per-hour':
        return apiSuggestions.packagesPerHourPrediction;
      case 'energy-per-hour':
        return apiSuggestions.energyPerHourPrediction;
      default:
        return apiSuggestions.energyPerPackagePredicition;
    }
  };

  // Generate optimization suggestions for unchecked machines based on API data
  const generateSuggestions = () => {
    const selectedSuggestion = getSelectedSuggestion();
    if (!selectedSuggestion) {
      return {
        machineSuggestions: [],
        packageIntervalSuggestion: null,
      };
    }

    // Get machines that the user didn't check to keep current settings
    const machinesToOptimize = Object.keys(machinePowerMultipliers).filter(
      (machine) => !keepSettings[machine]
    );

    const machineSuggestions = machinesToOptimize.map((machine) => ({
      machine,
      currentMultiplier: machinePowerMultipliers[machine as keyof typeof machinePowerMultipliers],
      suggestedMultiplier: selectedSuggestion.machinePowerMultipliers[machine],
    }));

    const packageIntervalSuggestion = !keepPackageInterval
      ? {
          type: 'packageInterval',
          currentValue: props.parameters.packageGenerationInterval,
          suggestedValue: selectedSuggestion.packageGenerationInterval,
        }
      : null;

    return {
      machineSuggestions,
      packageIntervalSuggestion,
    };
  };

  const handleImproveSettings = async () => {
    setIsLoading(true);
    setApiError(null);

    try {
      // Get machines that the user checked to keep current settings
      const fixedMachines = Object.entries(keepSettings)
        .filter(([_, keep]) => keep)
        .map(([machine]) => machine);

      const data = await optimizationService.optimize(
        props.parameters,
        keepPackageInterval,
        fixedMachines
      );

      setApiSuggestions(data);

      // Add artificial delay to simulate a "Processing..." state
      await new Promise((resolve) => setTimeout(resolve, 2500));
    } catch (error) {
      console.error('Failed to fetch optimization suggestions:', error);
      const errorMessage = error instanceof Error ? error.message : 'Unknown error';

      setApiError(`Failed to fetch optimization suggestions: ${errorMessage}`);
      setShowSuggestions(false);
    } finally {
      setIsLoading(false);
      if (!apiError) {
        setShowSuggestions(true);
      }
    }
  };

  const handleStartSimulation = () => {
    // Get the appropriate suggestion set based on optimization goal
    const selectedSuggestion = getSelectedSuggestion();
    if (!selectedSuggestion) return;

    const optimizedParameters: SimulationParameters = {
      ...selectedSuggestion,
    };

    // Save to localStorage with expanded sections
    const allExpandedSections: ExpandedSections = {
      basic: true,
      machines: true,
    };

    const configToSave: SavedConfig = {
      parameters: optimizedParameters,
      expandedSections: allExpandedSections,
      timestamp: new Date().toISOString(),
    };

    localStorage.setItem('simulationConfig', JSON.stringify(configToSave));

    // Navigate to simulation control page
    navigate('/simulation-control');
  };

  const suggestions =
    showSuggestions && apiSuggestions
      ? generateSuggestions()
      : { machineSuggestions: [], packageIntervalSuggestion: null };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-green-500" />
            Optimization Opportunities
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-6">
            {/* Current Machine Settings */}
            <div>
              <h4 className="font-medium text-gray-800 mb-4">Current Machine Power Multipliers</h4>
              <div className="space-y-3">
                {/* Header Row */}
                <div className="grid grid-cols-3 gap-4 p-3 bg-gray-100 rounded-lg font-medium text-sm text-gray-700">
                  <div>Machine Name</div>
                  <div>Current Setting</div>
                  <div>Keep Current Setting</div>
                </div>

                {/* Data Rows */}
                {Object.entries(machinePowerMultipliers).map(([machine, multiplier]) => (
                  <div
                    key={machine}
                    className="grid grid-cols-3 gap-4 p-3 bg-gray-50 rounded-lg items-center"
                  >
                    <div className="font-medium text-gray-900">
                      {props.formatMachineName(machine)}
                    </div>
                    <div className="text-gray-700">
                      <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200">
                        {multiplier}x
                      </Badge>
                    </div>
                    <div>
                      <Checkbox
                        checked={keepSettings[machine] || false}
                        onChange={(e) => handleKeepSettingChange(machine, e.target.checked)}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Package Generation Interval Setting */}
            <div className="mt-6">
              <h4 className="font-medium text-gray-800 mb-4">Package Generation Interval</h4>
              <div className="space-y-3">
                {/* Header Row */}
                <div className="grid grid-cols-3 gap-4 p-3 bg-gray-100 rounded-lg font-medium text-sm text-gray-700">
                  <div>Parameter</div>
                  <div>Current Setting</div>
                  <div>Keep Current Setting</div>
                </div>

                {/* Package Interval Row */}
                <div className="grid grid-cols-3 gap-4 p-3 bg-gray-50 rounded-lg items-center">
                  <div className="font-medium text-gray-900">Package Generation Interval</div>
                  <div className="text-gray-700">
                    <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200">
                      {props.parameters.packageGenerationInterval} minutes
                    </Badge>
                  </div>
                  <div>
                    <Checkbox
                      checked={keepPackageInterval}
                      onChange={(e) => setKeepPackageInterval(e.target.checked)}
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* Improve Settings Button */}
            <div className="flex justify-center">
              <Button
                onClick={handleImproveSettings}
                className="bg-green-600 hover:bg-green-700 text-white px-8 py-2"
                disabled={
                  (Object.keys(machinePowerMultipliers).every((machine) => keepSettings[machine]) &&
                    keepPackageInterval) ||
                  isLoading
                }
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    Processing...
                  </>
                ) : (
                  'Improve Settings'
                )}
              </Button>

              {/* API Error Message */}
              {apiError && (
                <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-md text-red-700 text-sm">
                  {apiError}
                </div>
              )}
            </div>

            {/* Optimization Goal Selection */}
            <div className="mt-6">
              <h4 className="font-medium text-gray-800 mb-4">Optimization Goal</h4>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {/* Energy-Per-Package Option */}
                <div
                  className={`p-4 rounded-lg border-2 transition-colors cursor-pointer ${
                    optimizationGoal === 'energy-per-package'
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-200 bg-gray-50 hover:border-blue-300'
                  }`}
                  onClick={() => setOptimizationGoal('energy-per-package')}
                >
                  <div className="flex items-center gap-3 mb-2">
                    <BarChart2
                      className={`w-5 h-5 ${
                        optimizationGoal === 'energy-per-package'
                          ? 'text-blue-600'
                          : 'text-gray-500'
                      }`}
                    />
                    <h5
                      className={`font-medium ${
                        optimizationGoal === 'energy-per-package'
                          ? 'text-blue-600'
                          : 'text-gray-700'
                      }`}
                    >
                      Energy-Per-Package
                    </h5>
                  </div>
                  <p className="text-sm text-gray-600">
                    Minimize energy used per processed package
                  </p>
                </div>

                {/* Maximize Throughput Option */}
                <div
                  className={`p-4 rounded-lg border-2 transition-colors cursor-pointer ${
                    optimizationGoal === 'packages-per-hour'
                      ? 'border-green-500 bg-green-50'
                      : 'border-gray-200 bg-gray-50 hover:border-green-300'
                  }`}
                  onClick={() => setOptimizationGoal('packages-per-hour')}
                >
                  <div className="flex items-center gap-3 mb-2">
                    <Gauge
                      className={`w-5 h-5 ${
                        optimizationGoal === 'packages-per-hour'
                          ? 'text-green-600'
                          : 'text-gray-500'
                      }`}
                    />
                    <h5
                      className={`font-medium ${
                        optimizationGoal === 'packages-per-hour'
                          ? 'text-green-600'
                          : 'text-gray-700'
                      }`}
                    >
                      Packages-Per-Hour
                    </h5>
                  </div>
                  <p className="text-sm text-gray-600">Maximize throughput of packages per hour</p>
                </div>

                {/* Minimize Energy Consumption Option */}
                <div
                  className={`p-4 rounded-lg border-2 transition-colors cursor-pointer ${
                    optimizationGoal === 'energy-per-hour'
                      ? 'border-amber-500 bg-amber-50'
                      : 'border-gray-200 bg-gray-50 hover:border-amber-300'
                  }`}
                  onClick={() => setOptimizationGoal('energy-per-hour')}
                >
                  <div className="flex items-center gap-3 mb-2">
                    <BatteryFull
                      className={`w-5 h-5 ${
                        optimizationGoal === 'energy-per-hour' ? 'text-amber-600' : 'text-gray-500'
                      }`}
                    />
                    <h5
                      className={`font-medium ${
                        optimizationGoal === 'energy-per-hour' ? 'text-amber-600' : 'text-gray-700'
                      }`}
                    >
                      Energy-Per-Hour
                    </h5>
                  </div>
                  <p className="text-sm text-gray-600">
                    Minimize total energy consumption per hour
                  </p>
                </div>
              </div>
            </div>

            {/* Optimization Suggestions */}
            {showSuggestions &&
              apiSuggestions &&
              (suggestions.machineSuggestions.length > 0 ||
                suggestions.packageIntervalSuggestion) && (
                <div className="mt-6">
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-2">
                      <h4 className="font-medium text-gray-800">Optimization Suggestions</h4>
                      {optimizationGoal === 'energy-per-package' && (
                        <Badge className="bg-blue-100 text-blue-700 border-0">
                          Energy-Per-Package
                        </Badge>
                      )}
                      {optimizationGoal === 'packages-per-hour' && (
                        <Badge className="bg-green-100 text-green-700 border-0">
                          Packages-Per-Hour
                        </Badge>
                      )}
                      {optimizationGoal === 'energy-per-hour' && (
                        <Badge className="bg-amber-100 text-amber-700 border-0">
                          Energy-Per-Hour
                        </Badge>
                      )}
                    </div>
                    <Button
                      onClick={handleStartSimulation}
                      variant="outline"
                      size="sm"
                      className="flex items-center gap-2"
                    >
                      <Copy className="w-4 h-4" />
                      Copy to Simulation Control
                    </Button>
                  </div>
                  <div className="space-y-3">
                    {suggestions.machineSuggestions.map((suggestion) => (
                      <div
                        key={suggestion.machine}
                        className={`p-4 rounded-lg border ${
                          optimizationGoal === 'energy-per-package'
                            ? 'bg-blue-50 border-blue-200'
                            : optimizationGoal === 'packages-per-hour'
                            ? 'bg-green-50 border-green-200'
                            : 'bg-amber-50 border-amber-200'
                        }`}
                      >
                        <div className="flex items-center justify-between">
                          <div>
                            <h5
                              className={`font-medium ${
                                optimizationGoal === 'energy-per-package'
                                  ? 'text-blue-800'
                                  : optimizationGoal === 'packages-per-hour'
                                  ? 'text-green-800'
                                  : 'text-amber-800'
                              }`}
                            >
                              {props.formatMachineName(suggestion.machine)}
                            </h5>
                            <p
                              className={`text-sm mt-1 ${
                                optimizationGoal === 'energy-per-package'
                                  ? 'text-blue-700'
                                  : optimizationGoal === 'packages-per-hour'
                                  ? 'text-green-700'
                                  : 'text-amber-700'
                              }`}
                            >
                              Change from {suggestion.currentMultiplier}x →{' '}
                              {suggestion.suggestedMultiplier}x
                            </p>
                          </div>
                        </div>
                      </div>
                    ))}

                    {/* Package Interval Suggestion */}
                    {suggestions.packageIntervalSuggestion && (
                      <div
                        className={`p-4 rounded-lg border ${
                          optimizationGoal === 'energy-per-package'
                            ? 'bg-blue-50 border-blue-200'
                            : optimizationGoal === 'packages-per-hour'
                            ? 'bg-green-50 border-green-200'
                            : 'bg-amber-50 border-amber-200'
                        }`}
                      >
                        <div className="flex items-center justify-between">
                          <div>
                            <h5
                              className={`font-medium ${
                                optimizationGoal === 'energy-per-package'
                                  ? 'text-blue-800'
                                  : optimizationGoal === 'packages-per-hour'
                                  ? 'text-green-800'
                                  : 'text-amber-800'
                              }`}
                            >
                              Package Generation Interval
                            </h5>
                            <p
                              className={`text-sm mt-1 ${
                                optimizationGoal === 'energy-per-package'
                                  ? 'text-blue-700'
                                  : optimizationGoal === 'packages-per-hour'
                                  ? 'text-green-700'
                                  : 'text-amber-700'
                              }`}
                            >
                              Change from {suggestions.packageIntervalSuggestion.currentValue}{' '}
                              minutes → {suggestions.packageIntervalSuggestion.suggestedValue}{' '}
                              minutes
                            </p>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
