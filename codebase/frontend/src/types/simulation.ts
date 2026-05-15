// Simulation-related types and interfaces
// Single source of truth for simulation data

import { SimulationParameters } from './simulationControl';

/**
 * Base simulation data interface - used for list/explorer views
 * Contains essential simulation information without detailed metrics
 */
export interface BaseSimulationData {
  id: string;
  name: string;
  timeCreated: string; // ISO string for consistency with backend
  duration: number; // Duration in milliseconds from backend
  timeEnded: string | null; // null until the simulation has actually ended
  parameters: SimulationParameters;
}

export interface SimulationDataDetails extends BaseSimulationData {
  totalPackages: number;
  expectedPackages: number;
  packageThroughput: {
    estimated: number;
    real: number;
  };
  timePerPackage: {
    estimated: number;
    real: number;
  };
  hourlyPackages: number[];
  energyByMachine?: Record<string, number>;
  totalEnergyPerHour?: Record<number, number>;
  processedStepsPerMachine?: Record<string, number>;
  totalEnergyPerMinute?: Record<number, number>;
  overheatingCounter?: Record<string, number>;
  totalEnergyUsage?: number;
}

// Utility types for component props
export interface CircularProgressProps {
  progress: number;
  color: string;
  size?: number;
}

export interface ParameterOptimizationProps {
  parameters: SimulationParameters;
  energyByMachine: Record<string, number>;
  formatMachineName: (machineId: string) => string;
}
