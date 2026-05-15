import { usePackageStore } from '@/store/packageStore';

export function usePackageTracking() {
  const { packages, machineSteps } = usePackageStore();

  return {
    packages,
    machineSteps,
  };
}
