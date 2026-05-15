// Machine-related types and enums

export enum MachineState {
  IDLE = 'IDLE',
  RUNNING = 'RUNNING',
  // STOPPED = "STOPPED",
  // ERROR = "ERROR",
  OVERHEATED = 'OVERHEATED',
}

export enum Color {
  RED = 'RED',
  BLUE = 'BLUE',
  WHITE = 'WHITE',
}

export enum Branch {
  LEFT = 'LEFT',
  MIDDLE = 'MIDDLE',
  RIGHT = 'RIGHT',
}

// Base Machine Interface
export interface BaseMachine {
  id: string | null;
  machineName: string;
  state: MachineState;
  timestamp: string; // String format from backend
  // Energy usage properties (only present during simulation)
  energyUsage?: number[];
  energyUsageTimestamps?: number[];
  energyKWHperMinute?: number;
  energyKWHs?: number[];
  currentOverload?: number;
  overheatingCounter?: number;
}

// Legacy base machine interface - keeping for backward compatibility
export interface MachineBase {
  name: string;
  active: boolean;
}

// ConveyorBelt Interface
export interface ConveyorBelt extends BaseMachine {
  forward: boolean;
  conveyorRunning: boolean;
}

// Legacy ConveyorBelt interface
export interface LegacyConveyorBelt extends MachineBase {
  speed: number;
}

// SortingLine Interface
export interface SortingLine extends BaseMachine {
  conveyorRunning: boolean;
  colorPresent: Color | null;
  ejectedToBranch: Branch | null;
}

// VacuumGripper Interface
export interface VacuumGripper extends BaseMachine {
  rotation: number;
  height: number;
  forward: number;
}

// Legacy VacuumGripper interface
export interface LegacyVacuumGripper extends MachineBase {
  x: number;
  y: number;
  z: number;
}

// HighBay Interface
export interface HighBay extends BaseMachine {
  cantileverBackward: boolean;
  cantileverForward: boolean;
  cantileverUp: boolean;
  cantileverDown: boolean;
  horizontal: number;
  height: number;
  conveyorForward: boolean;
  conveyorBackward: boolean;
}

// MultiProcessing Interface
export interface MultiProcessing extends BaseMachine {
  ovenInward: boolean;
  ovenLight: boolean;
  actGripper: boolean;
  actRotClockwise: boolean;
  actSaw: boolean;
  conveyerForward: boolean;
  actCompressor: boolean;
}

// PunchMachine Interface
export interface PunchMachine extends MachineBase {
  holeCount: number;
}
