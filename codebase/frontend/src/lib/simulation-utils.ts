// Simulation-related utility functions

export const formatTime = (minutes: number): string => {
  if (minutes === 0) return 'Continuous';
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (mins === 0) return `${hours}h`;
  return `${hours}h ${mins}m`;
};

export const formatMachineName = (key: string): string => {
  const nameMap: Record<string, string> = {
    SortingLine01: 'SortingLine01',
    ConveyorBelt01: 'ConveyorBelt01',
    VacuumGripper01: 'VacuumGripper01',
    VacuumGripper02: 'VacuumGripper02',
    MultiProcessing01: 'MultiProcessing01',
    HighBay01: 'HighBay01',
  };
  return nameMap[key] || key;
};

export const getSpeedDescription = (multiplier: number): string => {
  if (multiplier < 1) return `${multiplier}x (Slow)`;
  if (multiplier === 1) return `${multiplier}x (Real-time)`;
  if (multiplier <= 2) return `${multiplier}x (Slightly Fast)`;
  if (multiplier <= 5) return `${multiplier}x (Fast)`;
  if (multiplier <= 10) return `${multiplier}x (Very Fast)`;
  return `${multiplier}x (Extremely Fast)`;
};

export const getParameterStatus = (parameters: {
  packageGenerationInterval: number;
  machinePowerMultipliers: { [key: string]: number };
}): string[] => {
  const status: string[] = [];
  if (parameters.packageGenerationInterval !== 1.0)
    status.push(`${parameters.packageGenerationInterval}min/pkg Generation`);

  const modifiedMachines = Object.entries(parameters.machinePowerMultipliers).filter(
    ([, multiplier]) => multiplier !== 1.0
  );
  if (modifiedMachines.length > 0) {
    status.push(`${modifiedMachines.length} Modified Machines`);
  }

  return status;
};

// Format date for simulation display
export const formatSimulationDate = (date: Date): string => {
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

// Calculate simulation duration from start and end times
export const calculateDuration = (startTime: Date, endTime: Date): string => {
  const durationMs = endTime.getTime() - startTime.getTime();
  const durationMinutes = Math.floor(durationMs / (1000 * 60));
  return formatTime(durationMinutes);
};

// Format duration from milliseconds to time format (HH:MM:SS, MM:SS, or SS)
export const formatDurationFromMs = (durationMs: number): string => {
  const totalSeconds = Math.floor(durationMs / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours > 0) {
    return `${hours}h:${minutes.toString().padStart(2, '0')}:${seconds
      .toString()
      .padStart(2, '0')}s`;
  }

  if (minutes > 0) {
    return `${minutes}m:${seconds.toString().padStart(2, '0')}s`;
  }

  return `${seconds}s`;
};
