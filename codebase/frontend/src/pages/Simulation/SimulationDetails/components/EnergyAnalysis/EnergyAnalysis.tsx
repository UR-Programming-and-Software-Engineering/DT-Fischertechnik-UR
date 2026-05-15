import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Zap } from 'lucide-react';
import { formatMachineName } from '@/lib/simulation-utils';
import { EnergyAnalysisProps, EnrichedMachineData } from '@/types/energy';
import EnergyAnalysisMachines from './components/EnergyAnalysisMachines';
import OverheatingEvents from './components/OverheatingEvents';

export default function EnergyAnalysis({
  energyByMachine,
  totalEnergyUsage,
  totalPackages,
  overheatingCounter,
  parameters,
}: EnergyAnalysisProps) {
  const machineAnalysis = Object.entries(energyByMachine).map(([machine, energy]) => ({
    machine,
    consumption: Number(energy).toFixed(2),
    energyPerMachine: Number(energy),
  }));

  // Calculate energy efficiency (Wh per package)
  const energyPerPackage =
    totalPackages > 0 && totalEnergyUsage !== undefined
      ? (totalEnergyUsage / totalPackages).toFixed(2)
      : '0.00';

  // Energy data calculations
  const energyData = {
    totalConsumption: totalEnergyUsage !== undefined ? totalEnergyUsage.toFixed(2) : '0.00',
    energyPerPackage,
    overheatingCount: overheatingCounter,
    overheatingImpact:
      Object.values(overheatingCounter).reduce((sum, count) => sum + count, 0) > 5
        ? 'High'
        : Object.values(overheatingCounter).reduce((sum, count) => sum + count, 0) > 2
        ? 'Medium'
        : 'Low',
  };

  // Calculate percentage of total consumption and energy rate for each machine
  const enrichedMachineData: EnrichedMachineData[] = machineAnalysis.map((machine) => ({
    ...machine,
    percentageOfTotal:
      totalEnergyUsage !== undefined
        ? ((machine.energyPerMachine / totalEnergyUsage) * 100).toFixed(1)
        : '0.0',
    energyPerPackage:
      totalPackages > 0 ? (machine.energyPerMachine / totalPackages).toFixed(2) : '0.00',
  }));

  const machineWithHighestEnergyPerPackage = enrichedMachineData
    .filter((m) => m.energyPerMachine > 0)
    .reduce(
      (highest, current) =>
        parseFloat(current.energyPerPackage) > parseFloat(highest.energyPerPackage)
          ? current
          : highest,
      enrichedMachineData.find((m) => m.energyPerMachine > 0) || enrichedMachineData[0]
    );

  const findMachinesWithMostOverheating = () => {
    if (!energyData.overheatingCount || Object.keys(energyData.overheatingCount).length === 0) {
      return { machines: ['No machines'], maxCount: 0 };
    }

    const maxCount = Math.max(...Object.values(energyData.overheatingCount));

    const machinesWithMaxOverheating = Object.entries(energyData.overheatingCount)
      .filter(([_, count]) => count === maxCount)
      .map(([machine, _]) => machine);

    return {
      machines:
        machinesWithMaxOverheating.length > 0 ? machinesWithMaxOverheating : ['No machines'],
      maxCount,
    };
  };

  const { machines: mostOverheatingMachines, maxCount: mostOverheatingCount } =
    findMachinesWithMostOverheating();
  const machineDisplayName =
    mostOverheatingMachines.length === 1
      ? formatMachineName(mostOverheatingMachines[0])
      : mostOverheatingMachines.map(formatMachineName).join(', ');

  const analysisOverheatingEvents = [
    {
      machine: machineDisplayName,
      count: mostOverheatingCount,
      type: 'Most Overheated Machine',
    },
    {
      machine: machineWithHighestEnergyPerPackage.machine,
      count: energyData.overheatingCount[machineWithHighestEnergyPerPackage.machine] || 0,
      type: 'Highest Energy-Per-Package',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Overall Energy Summary */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Zap className="w-5 h-5 text-yellow-500" />
            Overall Energy Analysis
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-blue-50 p-4 rounded-lg">
              <p className="text-sm text-blue-600 font-medium">Total Consumption</p>
              <p className="text-2xl font-bold text-blue-700">
                {totalEnergyUsage !== undefined ? totalEnergyUsage.toFixed(2) : '0.00'}
              </p>
              <p className="text-xs text-blue-500">Wh</p>
            </div>
            <div className="bg-green-50 p-4 rounded-lg">
              <p className="text-sm text-green-600 font-medium">Total Packages</p>
              <p className="text-2xl font-bold text-green-700">{totalPackages}</p>
              <p className="text-xs text-green-500">packages</p>
            </div>
            <div className="bg-purple-50 p-4 rounded-lg">
              <p className="text-sm text-purple-600 font-medium">Energy-Per-Package</p>
              <p className="text-2xl font-bold text-purple-700">{energyData.energyPerPackage}</p>
              <p className="text-xs text-purple-500">Wh/package</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <EnergyAnalysisMachines
        enrichedMachineData={enrichedMachineData}
        formatMachineName={formatMachineName}
      />

      <OverheatingEvents
        overheatingCounter={overheatingCounter}
        enrichedMachineData={enrichedMachineData}
        analysisOverheatingEvents={analysisOverheatingEvents}
      />

    </div>
  );
}
