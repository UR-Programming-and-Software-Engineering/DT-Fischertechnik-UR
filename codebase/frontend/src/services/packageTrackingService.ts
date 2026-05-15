import api from './api';

export const packageTrackingService = {
  getPackageTrackingInfo: async () => {
    try {
      const response = await api.get('/tracking/package-info');
      return response.data;
    } catch (error) {
      console.error('Error fetching package tracking info:', error);
      throw error;
    }
  },
};
