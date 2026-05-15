import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { BaseMachine } from '@/types/machines';
import { MachineState } from '@/types/machines';
import { usePackageStore } from './packageStore';
// Simple store that maps machine names to machine objects
interface MachineStore {
  machines: { [machineName: string]: BaseMachine };
  updateMachines: (machines: BaseMachine[]) => void;
  hasPackage: (machineName: string) => boolean;
  getMachineDisplayState: (machineName: string) => MachineState;
  getMachineColor: (machineName: string) => string;
  getMachineStepColor: (index: number) => string;
  getMachineStepBadgeColor: (index: number) => string;
  getMachineStepState: (index: number) => MachineState;
}

export const useMachineStore = create<MachineStore>()(
  devtools(
    (set, get) => ({
      machines: {},

      updateMachines: (machines: BaseMachine[]) => {
        const machineMap: { [key: string]: BaseMachine } = {};

        machines.forEach((machine) => {
          if (machine.machineName) {
            machineMap[machine.machineName] = machine;
          }
        });

        set({ machines: machineMap });
      },
      hasPackage: (machineName: string) => {
        const packageStore = usePackageStore.getState();
        return packageStore.packages.some((pkg) => pkg.machineName === machineName);
      },

      getMachineDisplayState: (machineName: string): MachineState => {
        const machineState = get().machines[machineName]?.state;
        if (machineState == MachineState.OVERHEATED) {
          return MachineState.OVERHEATED;
        }
        return get().hasPackage(machineName) ? MachineState.RUNNING : MachineState.IDLE;
      },

      getMachineStepState: (index: number): MachineState => {
        const packageStore = usePackageStore.getState();
        const { packages, machineSteps } = packageStore;
        const machineState = get().machines[machineSteps[index]]?.state;
        if (machineState == MachineState.OVERHEATED) {
          return MachineState.OVERHEATED;
        }
        const stepHasPackage = packages.some((pkg) => pkg.index === index);

        return stepHasPackage ? MachineState.RUNNING : MachineState.IDLE;
      },

      getMachineColor: (machineName: string): string => {
        const machineStatesToColor: Record<MachineState, string> = {
          [MachineState.OVERHEATED]: '#F15A3B',
          [MachineState.RUNNING]: '#10b981',
          [MachineState.IDLE]: '#6b7280',
        };
        return machineStatesToColor[get().getMachineDisplayState(machineName)] || '#6b7280';
      },
      getMachineStepColor: (index: number): string => {
        const machineStatesToColor: Record<MachineState, string> = {
          [MachineState.OVERHEATED]: 'border-red-400 bg-red-50',
          [MachineState.RUNNING]: 'border-green-400 bg-green-50',
          [MachineState.IDLE]: 'border-gray-300 bg-white',
        };
        return machineStatesToColor[get().getMachineStepState(index)] || 'border-gray-300 bg-white';
      },
      getMachineStepBadgeColor: (index: number): string => {
        const machineStatesToColor: Record<MachineState, string> = {
          [MachineState.OVERHEATED]: 'bg-red-100 text-red-700',
          [MachineState.RUNNING]: 'bg-green-100 text-green-700',
          [MachineState.IDLE]: 'bg-gray-100 text-gray-700',
        };
        return (
          machineStatesToColor[get().getMachineStepState(index)] || 'bg-gray-100 text-gray-700'
        );
      },
    }),
    {
      name: 'machine-store', // This will be the name shown in Redux DevTools
    }
  )
);
