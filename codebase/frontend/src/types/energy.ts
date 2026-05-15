import { SimulationParameters } from './simulationControl';

export interface EnergyAnalysisProps {
  energyByMachine: Record<string, number>;
  totalEnergyUsage?: number;
  totalPackages: number;
  overheatingCounter: Record<string, number>;
  parameters: SimulationParameters;
}

export interface EnergyUsageOverviewProps {}

export interface EnrichedMachineData {
  machine: string;
  consumption: string;
  energyPerMachine: number;
  percentageOfTotal: string;
  energyPerPackage: string;
}

export interface AnalysisOverheatingEvents {
  machine: string;
  count: number;
  type: string;
}

export interface EnergyAnalysisMachinesProps {
  enrichedMachineData: EnrichedMachineData[];
  formatMachineName: (machineId: string) => string;
}

export interface OverheatingEventsProps {
  overheatingCounter?: Record<string, number>;
  enrichedMachineData: EnrichedMachineData[];
  analysisOverheatingEvents: AnalysisOverheatingEvents[];
}


