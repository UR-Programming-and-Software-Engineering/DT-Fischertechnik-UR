import { MachineState,Color } from './machines';
import { BaseSimulationData } from './simulation';

// Base component props
export interface ClickableCardProps {
  name: string;
  index: number;
  state: MachineState;
  children?: React.ReactNode;
  cardHeight?: string;
  currentOverload: number;
  overheatingCounter: number;
  speedMultiplier: number;
}

// SimulationChartsProps moved to component file

export interface SimulationListItemProps {
  simulation: BaseSimulationData;
  onClick?: (simulationId: string) => void;
}

export interface SimulationReportingDashboardProps {
  // Props will be defined when the component is updated
  title?: string;
  description?: string;
  [key: string]: any;
}

export interface ProductionLineFlowProps {
  title?: string;
  showToggle?: boolean;
  className?: string;
}

export interface PackageFlowOverviewProps {
  title?: string;
  description?: string;
  className?: string;
  showMetrics?: boolean;
}
export interface PackageTrackingInformation {
	packagesByType: Record<Color,number>;
	averageTimePerPackage: number; // in minutes with (1 decimal)
	troughputPerHour: number; // with 1 decimal 
}

// Note: This interface is kept in the component itself as it requires SimulationParameters
// export interface EnergyUsageOverviewProps {
//   energyByMachine?: Record<string, number>;
//   totalEnergyPerHour?: Record<number, number>;
//   totalEnergyPerMinute?: Record<number, number>;
//   parameters: SimulationParameters;
// }



