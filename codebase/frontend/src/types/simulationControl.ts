// Types for SimulationControl

export interface MachinePowerMultipliers {
  [key: string]: number;
  SortingLine01: number;
  ConveyorBelt01: number;
  VacuumGripper01: number;
  VacuumGripper02: number;
  MultiProcessing01: number;
  HighBay01: number;
}

export interface SimulationParameters {
  simulationDays: number;
  simulationHours: number;
  simulationMinutes: number;
  speedMultiplier: number;
  packageGenerationInterval: number; // minutes per package
  machinePowerMultipliers: MachinePowerMultipliers;
}

export interface ExpandedSections {
  basic: boolean;
  machines: boolean;
}

export interface MachineInfo {
  id: keyof MachinePowerMultipliers;
  name: string;
  icon: string;
  description: string;
}

export interface SavedConfig {
  parameters: SimulationParameters;
  expandedSections: ExpandedSections;
  timestamp: string;
}

export interface ApiPayload {
  parameters: SimulationParameters;
  timestamp: string;
}

export interface PresetPayload {
  parameters: SimulationParameters;
  name: string;
  timestamp: string;
}
