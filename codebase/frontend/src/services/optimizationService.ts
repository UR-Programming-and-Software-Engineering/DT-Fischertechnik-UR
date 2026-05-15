import api from './api';
import { SimulationParameters } from '@/types/simulationControl';

export const optimizationService = {
  optimize: async (
    parameters: SimulationParameters,
    fixPackageGenerationInterval: boolean,
    fixMachinePowerMultipliers: string[]
  ) => {
    const response = await api.post('/optimization/optimize', {
      parameters,
      fixPackageGenerationInterval,
      fixMachinePowerMultipliers,
    });
    return response.data;
  },
};

export default optimizationService;
