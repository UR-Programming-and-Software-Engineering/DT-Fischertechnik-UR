import { useEffect } from 'react';
import SimulationExplorer from '@/pages/Simulation/SimulationAnalysis/components/SimulationExplorer';
import SimulationReportingDashboard from '@/pages/Simulation/SimulationAnalysis/components/SimulationReportingDashboard';
import { useSimulationStore } from '@/store/simulationStore';
import { simulationService } from '@/services/simulationService';

export default function SimulationAnalysis() {
  const { baseSimulations, detailedSimulations, isLoadingBase, fetchBaseSimulations } =
    useSimulationStore();

  // Get the last 3 detailed simulations for the reporting dashboard
  const detailedSimulationsArray = Object.values(detailedSimulations).slice(0, 3);

  useEffect(() => {
    // Fetch base simulations for the explorer
    if (baseSimulations.length === 0) {
      fetchBaseSimulations(10);
    }

    // Fetch the last 3 detailed simulations for the dashboard
    const fetchDetailedSimulations = async () => {
      try {
        const data = await simulationService.getLastDetailedSimulations(3);
        
        if (data.simulations) {
          // Add each detailed simulation to the store
          data.simulations.forEach((sim: any) => {
            useSimulationStore.getState().addDetailedSimulation(sim);
          });
        }
      } catch (error) {
        console.error('Failed to fetch detailed simulations:', error);
      }
    };

    if (Object.keys(detailedSimulations).length === 0) {
      fetchDetailedSimulations();
    }
  }, [fetchBaseSimulations, baseSimulations, detailedSimulations]);

  return (
    <div className="min-h-screen bg-gray-100 px-4 py-8">
      <div className="max-w-7xl mx-auto space-y-6">
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Simulation Analysis</h1>
          <p className="text-gray-600">Explore and analyze past simulation runs</p>
        </div>

        <SimulationReportingDashboard simulations={detailedSimulationsArray} />

        {isLoadingBase ? (
          <div className="text-center py-4">Loading simulations...</div>
        ) : (
          <SimulationExplorer simulations={baseSimulations} />
        )}
      </div>
    </div>
  );
}
