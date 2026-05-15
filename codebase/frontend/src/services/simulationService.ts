import api from './api';
import { SimulationParameters } from '@/types/simulationControl';

export const simulationService = {
  getMachineSteps: async () => {
    try {
      const response = await api.get('/simulation/machine-steps');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch machine steps:', error);
      throw new Error('Failed to load machine steps');
    }
  },

  getRunningSimulation: async () => {
    try {
      const response = await api.get('/simulation/get-running-simulation');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch running simulation:', error);
      throw new Error('Failed to get running simulation status');
    }
  },

  getLastSimulations: async (limit: number) => {
    try {
      const response = await api.get(`/simulation/get-last-base?limit=${limit}`);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch last simulations:', error);
      throw new Error('Failed to load simulation history');
    }
  },

  getLastDetailedSimulations: async (limit: number) => {
    try {
      const response = await api.get(`/simulation/get-last-detailed?limit=${limit}`);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch detailed simulations:', error);
      throw new Error('Failed to load detailed simulation history');
    }
  },

  getSimulation: async (id: string) => {
    try {
      const response = await api.get(`/simulation/get-simulation/${id}`);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch simulation:', error);
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as any;
        if (axiosError.response?.status === 404) {
          throw new Error('Simulation not found');
        }
      }
      throw new Error('Failed to load simulation details');
    }
  },

  startSimulation: async (parameters: SimulationParameters) => {
    try {
      const payload = {
        parameters,
        timestamp: new Date().toISOString(),
      };
      const response = await api.post('/simulation/start-with-config', payload);
      return response.data;
    } catch (error) {
      console.error('Failed to start simulation:', error);
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as any;
        if (axiosError.response?.status === 400) {
          throw new Error('Invalid simulation parameters');
        }
        if (axiosError.response?.status === 409) {
          throw new Error('Another simulation is already running');
        }
      }
      throw new Error('Failed to start simulation');
    }
  },

  stopSimulation: async () => {
    try {
      const response = await api.post('/simulation/stop-simulation');
      return response.data;
    } catch (error) {
      console.error('Failed to stop simulation:', error);
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as any;
        if (axiosError.response?.status === 404) {
          throw new Error('No running simulation found');
        }
      }
      throw new Error('Failed to stop simulation');
    }
  },

  getSimulationPreset: async () => {
    try {
      const response = await api.get('/simulation-preset');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch simulation preset:', error);
      throw new Error('Failed to load simulation preset');
    }
  },
};

export default simulationService;
