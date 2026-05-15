import { useEffect, useRef, useState } from 'react';
import { useSimulationStore } from '@/store/simulationStore';
import { PackageTrackingInformation } from '@/types';
import { packageTrackingService } from '@/services/packageTrackingService';

export function usePackageTrackingPolling(intervalMs: number = 3000) {
  const { simulationRunning } = useSimulationStore();
  const nulledData: PackageTrackingInformation = {
    packagesByType: { RED: 0, BLUE: 0, WHITE: 0 },
    averageTimePerPackage: 0,
    troughputPerHour: 0,
  };
  const [packageData, setPackageData] = useState<PackageTrackingInformation>(nulledData);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  const normalizeData = (data: any): PackageTrackingInformation => ({
    ...nulledData,
    ...data,
    packagesByType: {
      ...nulledData.packagesByType,
      ...data.packagesByType,
    },
  });

  const fetchData = async () => {
    try {
      const data: PackageTrackingInformation = await packageTrackingService.getPackageTrackingInfo();
      setPackageData(normalizeData(data));
    } catch (error) {
      console.error('Error fetching package tracking data: ', error);
    }
  };

  // Start/stop polling based on simulation state
  useEffect(() => {
    // Only poll if simulation is running
    if (simulationRunning) {
      fetchData();

      // Set up polling interval
      intervalRef.current = setInterval(() => {
        fetchData();
      }, intervalMs);
    } else {
      // Stop polling when simulation is not running
      setPackageData(nulledData);
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    }

    // Cleanup on unmount or dependency changes
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [simulationRunning, intervalMs]);
  return packageData;
}
