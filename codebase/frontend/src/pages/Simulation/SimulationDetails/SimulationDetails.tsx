import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import {
  Clock,
  Package,
  Settings,
  Database,
  BarChart3,
  ChevronUp,
  ChevronDown,
  TrendingUp,
} from 'lucide-react';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { SimulationDataDetails } from '@/types/simulation';
import SimulationCharts from '@/pages/Simulation/SimulationDetails/components/SimulationCharts';
import SimulationDetailsParameters from '@/pages/Simulation/SimulationDetails/components/SimulationDetailsParameters';
import EnergyAnalysis from '@/pages/Simulation/SimulationDetails/components/EnergyAnalysis/EnergyAnalysis';
import EnergyUsageOverview from '@/pages/Simulation/SimulationDetails/components/EnergyUsageOverview';
import { useSimulationStore } from '@/store/simulationStore';
import { formatDurationFromMs, formatMachineName } from '@/lib/simulation-utils';
import ParameterOptimization from './components/ParameterOptimization';

export default function SimulationDetails() {
  const { id } = useParams();
  const [simulation, setSimulation] = useState<SimulationDataDetails | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isEnergyAnalysisExpanded, setIsEnergyAnalysisExpanded] = useState(false);
  const [isOptimizationExpanded, setIsOptimizationExpanded] = useState(false);
  const { getDetailedSimulation, fetchDetailedSimulation } = useSimulationStore();

  useEffect(() => {
    const loadSimulation = async () => {
      if (!id) {
        setError('No simulation ID provided');
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError(null);

      try {
        // First check if simulation is already in store
        const cachedSimulation = getDetailedSimulation(id);
        if (cachedSimulation) {
          setSimulation(cachedSimulation);
          setIsLoading(false);
          return;
        }

        // If not in store, fetch from backend
        const fetchedSimulation = await fetchDetailedSimulation(id);
        if (fetchedSimulation) {
          setSimulation(fetchedSimulation);
        } else {
          setError(`Simulation with ID "${id}" not found`);
        }
      } catch (err) {
        setError('Failed to load simulation details');
        console.error('Error loading simulation:', err);
      } finally {
        setIsLoading(false);
      }
    };

    loadSimulation();
  }, [id, getDetailedSimulation, fetchDetailedSimulation]);

  console.log(
    'Rendering SimulationDetails with simulation:',
    simulation,
    'isLoading:',
    isLoading,
    'error:',
    error
  );

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-100 px-4 py-8">
        <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
          <div className="flex items-center justify-center py-8">
            <div className="text-lg">Loading simulation details...</div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !simulation) {
    return (
      <div className="min-h-screen bg-gray-100 px-4 py-8">
        <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
          <h1 className="text-2xl font-bold text-red-600">Simulation Not Found</h1>
          <p className="text-gray-600 mt-4">
            {error || `Simulation with ID "${id}" does not exist.`}
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 px-4 py-8">
      <div className="max-w-6xl mx-auto space-y-6">
        {/* Header Section */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-2xl">
              <Settings className="w-6 h-6" />
              {simulation.name}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {/* Simulation ID */}
              <div className="flex items-center gap-3">
                <Database className="w-5 h-5 text-blue-500" />
                <div>
                  <p className="text-sm text-gray-600">Simulation ID</p>
                  <p className="font-semibold">{simulation.id}</p>
                </div>
              </div>

              {/* Time Created */}
              <div className="flex items-center gap-3">
                <Clock className="w-5 h-5 text-green-500" />
                <div>
                  <p className="text-sm text-gray-600">Time Created</p>
                  <p className="font-semibold">
                    {new Date(simulation.timeCreated).toLocaleString()}
                  </p>
                </div>
              </div>

              {/* Duration */}
              <div className="flex items-center gap-3">
                <Clock className="w-5 h-5 text-purple-500" />
                <div>
                  <p className="text-sm text-gray-600">Duration</p>
                  <p className="font-semibold">{formatDurationFromMs(simulation.duration)}</p>
                </div>
              </div>

              {/* Total Packages */}
              <div className="flex items-center gap-3">
                <Package className="w-5 h-5 text-orange-500" />
                <div>
                  <p className="text-sm text-gray-600">Total Packages</p>
                  <p className="font-semibold">{simulation.totalPackages.toLocaleString()}</p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Simulation Details Heading */}
        <div className="w-full max-w-6xl mb-6">
          <h2 className="text-2xl font-semibold text-gray-800">Simulation Details</h2>
        </div>

        {/* Simulation Parameters - now using the extracted component */}
        <SimulationDetailsParameters parameters={simulation.parameters} />

        {/* Metric Heading */}
        <div className="w-full max-w-6xl mb-6">
          <h2 className="text-2xl font-semibold text-gray-800">Reporting Metrics</h2>
        </div>

        {/* Charts Section */}
        <SimulationCharts
          overheatingCounter={simulation.overheatingCounter || {}}
          hourlyPackages={simulation.hourlyPackages}
          packageThroughput={simulation.packageThroughput}
          timePerPackage={simulation.timePerPackage}
        />

        {/* Energy Usage Heading */}
        <div className="w-full max-w-6xl mb-6">
          <h2 className="text-2xl font-semibold text-gray-800">Energy Usage</h2>
        </div>

        {/* Energy Analysis Toggle */}
        <Collapsible open={isEnergyAnalysisExpanded} onOpenChange={setIsEnergyAnalysisExpanded}>
          <Card>
            <CollapsibleTrigger asChild>
              <CardHeader className="cursor-pointer hover:bg-gray-50 transition-colors">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <BarChart3 className="h-5 w-5 text-blue-600" />
                    <CardTitle className="text-lg">Energy Analysis</CardTitle>
                  </div>
                  {isEnergyAnalysisExpanded ? (
                    <ChevronUp className="h-5 w-5 text-gray-400" />
                  ) : (
                    <ChevronDown className="h-5 w-5 text-gray-400" />
                  )}
                </div>
                <CardDescription>
                  Detailed analysis of energy consumption, machine efficiency, and optimization
                  recommendations
                </CardDescription>
              </CardHeader>
            </CollapsibleTrigger>

            <CollapsibleContent>
              <CardContent>
                <EnergyAnalysis
                  energyByMachine={simulation.energyByMachine || {}}
                  totalEnergyUsage={simulation.totalEnergyUsage || 0}
                  totalPackages={simulation.totalPackages || 0}
                  overheatingCounter={simulation.overheatingCounter || {}}
                  parameters={simulation.parameters || {}}
                />
              </CardContent>
            </CollapsibleContent>
          </Card>
        </Collapsible>

        {/* Optimization Opportunities Heading */}
        <div className="w-full max-w-6xl mb-6">
          <h2 className="text-2xl font-semibold text-gray-800">Optimization Opportunities</h2>
        </div>

        {/* Optimization Opportunities Toggle */}
        <Collapsible open={isOptimizationExpanded} onOpenChange={setIsOptimizationExpanded}>
          <Card>
            <CollapsibleTrigger asChild>
              <CardHeader className="cursor-pointer hover:bg-gray-50 transition-colors">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <TrendingUp className="h-5 w-5 text-green-600" />
                    <CardTitle className="text-lg">Parameter Optimization</CardTitle>
                  </div>
                  {isOptimizationExpanded ? (
                    <ChevronUp className="h-5 w-5 text-gray-400" />
                  ) : (
                    <ChevronDown className="h-5 w-5 text-gray-400" />
                  )}
                </div>
                <CardDescription>
                  Machine parameter recommendations and optimization insights
                </CardDescription>
              </CardHeader>
            </CollapsibleTrigger>

            <CollapsibleContent>
              <CardContent>
                <ParameterOptimization
                  parameters={simulation.parameters || {}}
                  energyByMachine={simulation.energyByMachine || {}}
                  formatMachineName={formatMachineName}
                />
              </CardContent>
            </CollapsibleContent>
          </Card>
        </Collapsible>

        {/* Energy Usage Overview Charts */}
        <EnergyUsageOverview
          energyByMachine={simulation.energyByMachine || {}}
          totalEnergyPerHour={simulation.totalEnergyPerHour}
          totalEnergyPerMinute={simulation.totalEnergyPerMinute}
          parameters={simulation.parameters}
        />
      </div>
    </div>
  );
}
