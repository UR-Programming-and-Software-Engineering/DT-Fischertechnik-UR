// Store-related interfaces

export interface Package {
  id: string;
  index: number;
  machineName: string;
}

export interface PackageStore {
  packages: Package[];
  machineSteps: string[];
  
  // Actions
  updatePackages: (packages: Package[]) => void;
  setMachineSteps: (steps: string[]) => void;
}

export interface MachineStore {
  // To be defined based on actual machine store structure
  [key: string]: any;
}
