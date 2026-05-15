import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { BaseSimulationData, SimulationDataDetails } from '@/types/simulation';
import { simulationService } from '@/services/simulationService';

interface SimulationStore {
  // Base simulations for list views
  baseSimulations: BaseSimulationData[];

  // Detailed simulations cache
  detailedSimulations: { [id: string]: SimulationDataDetails };

  // Current running simulation state
  simulationRunning: boolean;
  currentSimulationDetails: SimulationDataDetails | null;
  simulationError: string | null;

  // Loading states
  isLoadingBase: boolean;
  isLoadingDetail: boolean;

  // Actions
  setBaseSimulations: (simulations: BaseSimulationData[]) => void;
  addDetailedSimulation: (simulation: SimulationDataDetails) => void;
  getDetailedSimulation: (id: string) => SimulationDataDetails | undefined;

  // Running simulation actions
  setSimulationRunning: (running: boolean) => void;
  setCurrentSimulationDetails: (details: SimulationDataDetails | null) => void;
  setSimulationError: (error: string | null) => void;
  clearCurrentSimulation: () => void;
  fetchCurrentSimulation: () => Promise<void>;

  // Async actions
  fetchBaseSimulations: (limit?: number) => Promise<void>;
  fetchDetailedSimulation: (id: string) => Promise<SimulationDataDetails | null>;
}

export const useSimulationStore = create<SimulationStore>()(
  devtools(
    (set, get) => ({
      baseSimulations: [],
      detailedSimulations: {},
      simulationRunning: false,
      currentSimulationDetails: null,
      simulationError: null,
      isLoadingBase: false,
      isLoadingDetail: false,

      setBaseSimulations: (simulations: BaseSimulationData[]) => {
        set({ baseSimulations: simulations });
      },

      addDetailedSimulation: (simulation: SimulationDataDetails) => {
        set((state) => ({
          detailedSimulations: {
            ...state.detailedSimulations,
            [simulation.id]: simulation,
          },
        }));
      },

      getDetailedSimulation: (id: string): SimulationDataDetails | undefined => {
        return get().detailedSimulations[id];
      },

      // Running simulation actions
      setSimulationRunning: (running: boolean) => {
        set({ simulationRunning: running });
      },

      setCurrentSimulationDetails: (details: SimulationDataDetails | null) => {
        set({ currentSimulationDetails: details });
      },

      setSimulationError: (error: string | null) => {
        set({ simulationError: error });
      },

      clearCurrentSimulation: () => {
        set({
          currentSimulationDetails: null,
          simulationRunning: false,
          simulationError: null,
        });
      },

      fetchCurrentSimulation: async () => {
        try {
          const simulation = await simulationService.getRunningSimulation();
          console.log('Fetched current simulation:', simulation);
          set({
            currentSimulationDetails: simulation,
            simulationError: null,
          });
        } catch (error) {
          console.error('Failed to fetch current simulation:', error);
          set({ simulationError: 'Failed to fetch simulation data' });
        }
      },

      fetchBaseSimulations: async (limit = 10) => {
        set({ isLoadingBase: true });
        try {
          const data = await simulationService.getLastSimulations(limit);

          if (data.simulations) {
            set({ baseSimulations: data.simulations });
          }
        } catch (error) {
          console.error('Failed to fetch base simulations:', error);
        } finally {
          set({ isLoadingBase: false });
        }
      },

      fetchDetailedSimulation: async (id: string): Promise<SimulationDataDetails | null> => {
        // Check if already in cache
        const existing = get().detailedSimulations[id];
        if (existing) {
          return existing;
        }

        set({ isLoadingDetail: true });
        try {
          const simulation = await simulationService.getSimulation(id);

          // Add to cache
          get().addDetailedSimulation(simulation);

          return simulation;
        } catch (error) {
          console.error('Failed to fetch detailed simulation:', error);
          return null;
        } finally {
          set({ isLoadingDetail: false });
        }
      },
    }),
    {
      name: 'simulation-store',
    }
  )
);
