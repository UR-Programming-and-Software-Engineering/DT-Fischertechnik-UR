import { useState, useEffect } from 'react';
import { Slider } from '@/components/ui/slider';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { Settings2, Save, RotateCcw, Clock, Wrench, ChevronUp, ChevronDown } from 'lucide-react';
import { getSpeedDescription } from '@/lib/simulation-utils';
import { useSimulationStore } from '@/store/simulationStore';
import { useSimulationPolling } from '@/hooks/useSimulationPolling';
import { SimulationStatus } from '@/pages/Simulation/SimulationControl/components/SimulationStatus';
import { simulationService } from '@/services/simulationService';
import type {
  MachinePowerMultipliers,
  SimulationParameters,
  ExpandedSections,
  MachineInfo,
  SavedConfig,
  PresetPayload,
} from '@/types/simulationControl';

export default function SimulationControl() {
  // flag to prevent saving until after loading
  const [isInitialLoadComplete, setIsInitialLoadComplete] = useState(false);

  // Simulation store
  const {
    setSimulationRunning,
    fetchCurrentSimulation,
    setSimulationError,
    clearCurrentSimulation,
  } = useSimulationStore();

  // Start polling for simulation data when simulation is running
  const { isPolling } = useSimulationPolling(3000); // Poll every 3 seconds

  // State for all parameters
  const [parameters, setParameters] = useState<SimulationParameters>({
    simulationDays: 0,
    simulationHours: 2,
    simulationMinutes: 30,
    speedMultiplier: 2.0,
    packageGenerationInterval: 1.0, // minutes per package
    machinePowerMultipliers: {
      SortingLine01: 1.0,
      ConveyorBelt01: 1.0,
      VacuumGripper01: 1.0,
      VacuumGripper02: 1.0,
      MultiProcessing01: 1.0,
      HighBay01: 1.0,
    },
  });

  // State for UI control
  const [expandedSections, setExpandedSections] = useState<ExpandedSections>({
    basic: true,
    machines: false,
  });

  // Helper functions
  const getTotalSimulationMinutes = () => {
    return (
      parameters.simulationDays * 24 * 60 +
      parameters.simulationHours * 60 +
      parameters.simulationMinutes
    );
  };

  const getSimulationDuration = () => {
    const totalMinutes = getTotalSimulationMinutes();
    const durationMinutes = totalMinutes / parameters.speedMultiplier;

    const hours = Math.floor(durationMinutes / 60);
    const minutes = Math.floor(durationMinutes % 60);
    const seconds = Math.floor((durationMinutes % 1) * 60);

    if (hours > 0) {
      return `${hours}h ${minutes}m ${seconds}s`;
    } else if (minutes > 0) {
      return `${minutes}m ${seconds}s`;
    } else {
      return `${seconds}s`;
    }
  };

  const onSimulationTimeChange = (
    field: 'simulationDays' | 'simulationHours' | 'simulationMinutes',
    value: number
  ): void => {
    setParameters((prev) => ({
      ...prev,
      [field]: value,
    }));
  };
  const machineInfo: MachineInfo[] = [
    {
      id: 'SortingLine01',
      name: 'SortingLine01',
      icon: '🔄',
      description: 'Sorts packages by color',
    },
    {
      id: 'ConveyorBelt01',
      name: 'ConveyorBelt01',
      icon: '🚚',
      description: 'Transports packages through system',
    },
    {
      id: 'VacuumGripper01',
      name: 'VacuumGripper01',
      icon: '🏗️',
      description: 'Moves packages between ConveyorBelt, MultiProcessing, and HighBay',
    },
    {
      id: 'VacuumGripper02',
      name: 'VacuumGripper02',
      icon: '🏗️',
      description: 'Moves packages between SortingLine and ConveyorBelt',
    },
    {
      id: 'MultiProcessing01',
      name: 'MultiProcessing01',
      icon: '⚙️',
      description: 'Multi-stage processing station, bakes and drills packages',
    },
    {
      id: 'HighBay01',
      name: 'HighBay01',
      icon: '🪜',
      description: 'HighBay storage and retrieval system',
    },
  ];

  // Load saved config from localStorage on component mount
  useEffect(() => {
    const savedConfig = localStorage.getItem('simulationConfig');
    if (savedConfig) {
      try {
        const parsed: any = JSON.parse(savedConfig);

        // Handle migration from old machineDelays to new machinePowerMultipliers
        if (parsed.parameters) {
          let migratedParameters = { ...parsed.parameters };

          // Check if old machineDelays exists and convert to machinePowerMultipliers
          if (parsed.parameters.machineDelays && !parsed.parameters.machinePowerMultipliers) {
            migratedParameters.machinePowerMultipliers = {};

            // Convert delay values to power multipliers (inverse relationship)
            Object.entries(parsed.parameters.machineDelays).forEach(
              ([machineId, delay]: [string, any]) => {
                // Convert delay to power multiplier: delay of 0.5 -> power of 2.0, delay of 2.0 -> power of 0.5
                migratedParameters.machinePowerMultipliers[machineId] =
                  Math.round((1 / (delay as number)) * 10) / 10;
              }
            );

            // Remove old property
            delete migratedParameters.machineDelays;
          }

          // Ensure machinePowerMultipliers exists with defaults if missing
          if (!migratedParameters.machinePowerMultipliers) {
            migratedParameters.machinePowerMultipliers = {
              SortingLine01: 1.0,
              ConveyorBelt01: 1.0,
              VacuumGripper01: 1.0,
              VacuumGripper02: 1.0,
              MultiProcessing01: 1.0,
              HighBay01: 1.0,
            };
          }

          setParameters(migratedParameters);
        }
        if (parsed.expandedSections) {
          setExpandedSections(parsed.expandedSections);
        }
      } catch (error) {
        console.error('Error loading saved config:', error);
        // Clear localStorage if parsing fails to prevent further issues
        localStorage.removeItem('simulationConfig');
      }
    }
    // Mark initial load as complete AFTER loading is done
    setIsInitialLoadComplete(true);
  }, []);

  // Save config to localStorage whenever it changes (but only after initial load)
  useEffect(() => {
    // Don't save during initial load - this prevents overwriting saved data
    if (!isInitialLoadComplete) return;

    const configToSave: SavedConfig = {
      parameters,
      expandedSections,
      timestamp: new Date().toISOString(),
    };
    localStorage.setItem('simulationConfig', JSON.stringify(configToSave));
  }, [parameters, expandedSections, isInitialLoadComplete]);

  // Event handlers
  const onParameterChange = (
    //verhindert dass machinePowerMultipliers als Key übergeben wird
    key: keyof Omit<SimulationParameters, 'machinePowerMultipliers'>,
    value: number
  ): void => {
    setParameters((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const onMachinePowerChange = (machineId: keyof MachinePowerMultipliers, value: number): void => {
    setParameters((prev) => ({
      ...prev,
      machinePowerMultipliers: {
        ...prev.machinePowerMultipliers,
        [machineId]: value,
      },
    }));
  };

  // Slider callback handlers with proper typing
  const handleSliderChange =
    (key: keyof Omit<SimulationParameters, 'machinePowerMultipliers'>) =>
    (value: number[]): void => {
      onParameterChange(key, value[0]);
    };

  const handleMachineSliderChange =
    (machineId: keyof MachinePowerMultipliers) =>
    (value: number[]): void => {
      onMachinePowerChange(machineId, value[0]);
    };

  const toggleSection = (section: keyof ExpandedSections): void => {
    setExpandedSections((prev) => ({
      ...prev,
      [section]: !prev[section],
    }));
  };

  const onSavePreset = async (): Promise<void> => {
    try {
      const payload: PresetPayload = {
        parameters,
        name: `Preset ${new Date().toLocaleString()}`,
        timestamp: new Date().toISOString(),
      };

      const response = await fetch('/api/simulation-preset', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        console.log('Preset saved successfully');
      } else {
        console.error('Failed to save preset');
      }
    } catch (error) {
      console.error('Error saving preset:', error);
    }
  };

  const onResetToDefaults = (): void => {
    const defaultParameters: SimulationParameters = {
      simulationDays: 0,
      simulationHours: 2,
      simulationMinutes: 30,
      speedMultiplier: 2.0,
      packageGenerationInterval: 1.0,
      machinePowerMultipliers: {
        SortingLine01: 1.0,
        ConveyorBelt01: 1.0,
        VacuumGripper01: 1.0,
        VacuumGripper02: 1.0,
        MultiProcessing01: 1.0,
        HighBay01: 1.0,
      },
    };
    setParameters(defaultParameters);
  };

  // Send configuration to backend
  const sendConfigToBackend = async (): Promise<boolean> => {
    try {
      await simulationService.startSimulation(parameters);
      console.log('Simulation started successfully');
      return true;
    } catch (error) {
      console.error('Error sending configuration to backend:', error);
      return false;
    }
  };

  // Handle simulation control
  const handleStartSimulation = async (): Promise<void> => {
    try {
      // Send configuration to backend first
      const configSent = await sendConfigToBackend();
      if (!configSent) {
        setSimulationError('Failed to send configuration to backend');
        return;
      }

      // Set simulation as running
      setSimulationRunning(true);
      setSimulationError(null);

      // Set up timeout (2 minutes = 120000ms)
      const timeoutPromise = new Promise<void>((_, reject) => {
        setTimeout(() => {
          reject(new Error('Simulation failed to start - timeout after 2 minutes'));
        }, 120000);
      });

      // Try to fetch simulation data with timeout
      try {
        await Promise.race([fetchCurrentSimulation(), timeoutPromise]);
      } catch (error) {
        if (error instanceof Error && error.message.includes('timeout')) {
          setSimulationError('Simulation failed to start');
        } else {
          setSimulationError('Failed to start simulation');
        }
      }
    } catch (error) {
      console.error('Error starting simulation:', error);
      setSimulationError('Failed to start simulation');
    }
  };

  const handleStopSimulation = async (): Promise<void> => {
    try {
      // Stop the simulation on the backend
      const response = await simulationService.stopSimulation();

      if (response.ok) {
        // Only stop the simulation, but keep the data for the completed screen
        setSimulationRunning(false);
        console.log('Simulation stopped successfully');

        // Fetch final simulation state with timeEnded after a short delay
        setTimeout(() => {
          console.log('Fetching final simulation state...');
          fetchCurrentSimulation();
        }, 500);
      } else {
        console.error('Failed to stop simulation');
        setSimulationError('Failed to stop simulation');
      }
    } catch (error) {
      console.error('Error stopping simulation:', error);
      setSimulationError('Error stopping simulation');
    }
  };

  return (
    <div className="bg-gray-100">
      <div className="max-w-4xl mx-auto p-6 ">
        <div className="space-y-4">
          {/* Simulation Control Buttons */}
          <div className="flex gap-2 mb-4">
            <Button onClick={handleStartSimulation} className="bg-green-600 hover:bg-green-700">
              Start Simulation
            </Button>
            <Button variant="outline" onClick={handleStopSimulation}>
              Stop Simulation
            </Button>
          </div>
          {/* Parameter Status Overview */}
          <Card className="border-blue-200 bg-blue-50/50">
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <Settings2 className="h-5 w-5 text-blue-600" />
                  <span className="font-medium text-blue-900">Current Configuration</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={onSavePreset}
                    className="text-blue-700 border-blue-300 hover:bg-blue-100 bg-transparent"
                  >
                    <Save className="h-4 w-4 mr-1" />
                    Save Preset
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={onResetToDefaults}
                    className="text-gray-700 border-gray-300 hover:bg-gray-100 bg-transparent"
                  >
                    <RotateCcw className="h-4 w-4 mr-1" />
                    Reset
                  </Button>
                </div>
              </div>

              <div className="mt-3 flex flex-wrap gap-2">
                <Badge variant="outline" className="bg-white">
                  Sim: {getTotalSimulationMinutes()}min ({parameters.simulationDays}d{' '}
                  {parameters.simulationHours}h {parameters.simulationMinutes}m)
                </Badge>
                <Badge variant="outline" className="bg-white">
                  Duration: {getSimulationDuration()}
                </Badge>
                <Badge variant="outline" className="bg-white">
                  {getSpeedDescription(parameters.speedMultiplier)}
                </Badge>
                <Badge variant="outline" className="bg-white">
                  {parameters.packageGenerationInterval}min/pkg
                </Badge>
              </div>
            </CardContent>
          </Card>

          <SimulationStatus />

          {/* Basic Parameters */}
          <Collapsible open={expandedSections.basic} onOpenChange={() => toggleSection('basic')}>
            <Card>
              <CollapsibleTrigger asChild>
                <CardHeader className="cursor-pointer hover:bg-gray-50 transition-colors">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2">
                      <Clock className="h-5 w-5 text-blue-600" />
                      <CardTitle className="text-lg">Basic Parameters</CardTitle>
                    </div>
                    {expandedSections.basic ? (
                      <ChevronUp className="h-5 w-5 text-gray-400" />
                    ) : (
                      <ChevronDown className="h-5 w-5 text-gray-400" />
                    )}
                  </div>
                  <CardDescription>Core simulation timing and behavior settings</CardDescription>
                </CardHeader>
              </CollapsibleTrigger>

              <CollapsibleContent>
                <CardContent className="space-y-6">
                  {/* Simulation Time */}
                  <div className="space-y-3">
                    <Label className="text-sm font-medium">Simulation Time to Simulate</Label>
                    <div className="grid grid-cols-3 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="days" className="text-xs text-gray-600">
                          Days
                        </Label>
                        <input
                          id="days"
                          type="number"
                          min="0"
                          max="365"
                          placeholder="0"
                          value={parameters.simulationDays || ''}
                          onChange={(e) =>
                            onSimulationTimeChange('simulationDays', parseInt(e.target.value) || 0)
                          }
                          className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="hours" className="text-xs text-gray-600">
                          Hours
                        </Label>
                        <input
                          id="hours"
                          type="number"
                          min="0"
                          max="23"
                          placeholder="0"
                          value={parameters.simulationHours || ''}
                          onChange={(e) =>
                            onSimulationTimeChange('simulationHours', parseInt(e.target.value) || 0)
                          }
                          className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="minutes" className="text-xs text-gray-600">
                          Minutes
                        </Label>
                        <input
                          id="minutes"
                          type="number"
                          min="0"
                          max="59"
                          placeholder="0"
                          value={parameters.simulationMinutes || ''}
                          onChange={(e) =>
                            onSimulationTimeChange(
                              'simulationMinutes',
                              parseInt(e.target.value) || 0
                            )
                          }
                          className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                        />
                      </div>
                    </div>
                    <div className="text-xs text-gray-500">
                      Total: {getTotalSimulationMinutes()} minutes | Real duration:{' '}
                      {getSimulationDuration()}
                    </div>
                  </div>

                  <Separator />

                  {/* Speed Multiplier */}
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="speed" className="text-sm font-medium">
                        Simulation Speed
                      </Label>
                      <span className="text-sm text-gray-600">
                        {getSpeedDescription(parameters.speedMultiplier)}
                      </span>
                    </div>
                    <Slider
                      id="speed"
                      min={1}
                      max={500}
                      step={0.5}
                      value={[parameters.speedMultiplier]}
                      onValueChange={handleSliderChange('speedMultiplier')}
                      className="w-full"
                    />
                    <div className="flex justify-between text-xs text-gray-500">
                      <span>1x (Real-time)</span>
                      <span>250x</span>
                      <span>500x (Fast)</span>
                    </div>
                  </div>

                  <Separator />

                  {/* Package Generation Interval */}
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="packageInterval" className="text-sm font-medium">
                        Package Generation Interval
                      </Label>
                      <span className="text-sm text-gray-600">
                        {parameters.packageGenerationInterval} min/package
                      </span>
                    </div>
                    <Slider
                      id="packageInterval"
                      min={1}
                      max={5}
                      step={0.1}
                      value={[parameters.packageGenerationInterval]}
                      onValueChange={handleSliderChange('packageGenerationInterval')}
                      className="w-full"
                    />
                    <div className="flex justify-between text-xs text-gray-500">
                      <span>1 min/package (Fast)</span>
                      <span>5 min/package (Slow)</span>
                    </div>
                  </div>
                </CardContent>
              </CollapsibleContent>
            </Card>
          </Collapsible>
          {/* Machine-Specific Power Multipliers */}
          <Collapsible
            open={expandedSections.machines}
            onOpenChange={() => toggleSection('machines')}
          >
            <Card>
              <CollapsibleTrigger asChild>
                <CardHeader className="cursor-pointer hover:bg-gray-50 transition-colors">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2">
                      <Wrench className="h-5 w-5 text-orange-600" />
                      <CardTitle className="text-lg">Machine Power Multipliers</CardTitle>
                    </div>
                    {expandedSections.machines ? (
                      <ChevronUp className="h-5 w-5 text-gray-400" />
                    ) : (
                      <ChevronDown className="h-5 w-5 text-gray-400" />
                    )}
                  </div>
                  <CardDescription>
                    Adjust power multipliers for individual machines (higher = faster, lower =
                    slower)
                  </CardDescription>
                </CardHeader>
              </CollapsibleTrigger>

              <CollapsibleContent>
                <CardContent className="space-y-4">
                  {machineInfo.map((machine, index) => (
                    <div key={machine.id}>
                      <div className="space-y-3">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center space-x-2">
                            <span className="text-lg">{machine.icon}</span>
                            <div>
                              <Label className="text-sm font-medium">{machine.name}</Label>
                              <p className="text-xs text-gray-500">{machine.description}</p>
                            </div>
                          </div>
                          <div className="flex items-center space-x-2">
                            <span className="text-sm text-gray-600 min-w-[60px] text-right">
                              {parameters.machinePowerMultipliers[machine.id]?.toFixed(1)}x
                            </span>
                            {parameters.machinePowerMultipliers[machine.id] !== 1 && (
                              <Badge variant="secondary" className="text-xs">
                                Modified
                              </Badge>
                            )}
                          </div>
                        </div>
                        <Slider
                          min={0.5}
                          max={2.0}
                          step={0.1}
                          value={[parameters.machinePowerMultipliers[machine.id] || 1]}
                          onValueChange={handleMachineSliderChange(machine.id)}
                          className="w-full"
                        />
                        <div className="flex justify-between text-xs text-gray-500">
                          <span>0.5x (Low Power)</span>
                          <span>1x (Normal)</span>
                          <span>2x (High Power)</span>
                        </div>
                      </div>
                      {index < machineInfo.length - 1 && <Separator className="mt-4" />}
                    </div>
                  ))}
                </CardContent>
              </CollapsibleContent>
            </Card>
          </Collapsible>
          {/* Debug Info */}
          <Card className="bg-gray-50">
            <CardHeader>
              <CardTitle className="text-sm">Current Configuration (Debug)</CardTitle>
            </CardHeader>
            <CardContent>
              <pre className="text-xs overflow-auto bg-white p-2 rounded border">
                {JSON.stringify({ parameters }, null, 2)}
              </pre>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
