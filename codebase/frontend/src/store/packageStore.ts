import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { Package, PackageStore } from '@/types/store';

export const usePackageStore = create<PackageStore>()(
  devtools(
    (set,get) => ({
      packages: [],
      machineSteps: [],
      
      updatePackages: (packages: Package[]) => {
        set({ packages });
      },
      
      setMachineSteps: (steps: string[]) => {
        set({ machineSteps: steps });
      },
      getNameFromStep: (index: number) => {
        return get().machineSteps[index];
      },
    }),
    {
      name: 'package-store',
    }
  )
);
