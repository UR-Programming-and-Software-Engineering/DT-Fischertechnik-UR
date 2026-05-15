import { useEffect, useRef } from 'react';
import { useSimulationStore } from '@/store/simulationStore';

/**
 * Custom hook that handles polling for current simulation data
 * Automatically starts polling when simulation is running and stops when it's not
 */
export function useSimulationPolling(intervalMs: number = 3000) {
  const {
    simulationRunning,
    currentSimulationDetails,
    fetchCurrentSimulation
  } = useSimulationStore();
  
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  // Start/stop polling based on simulation state
  useEffect(() => {
    // Only poll if simulation is running
    if (simulationRunning) {
      // Fetch immediately when starting
      fetchCurrentSimulation();
      
      // Set up polling interval
      intervalRef.current = setInterval(() => {
        fetchCurrentSimulation();
      }, intervalMs);
    } else {
      // Stop polling when simulation is not running
      if (currentSimulationDetails) fetchCurrentSimulation(); // fetch one last time
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
  }, [simulationRunning, fetchCurrentSimulation, intervalMs]);

  // Also cleanup on component unmount
  useEffect(() => {
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, []);

  return {
    isPolling: simulationRunning && intervalRef.current !== null,
    currentSimulation: currentSimulationDetails
  };
}
