import axiosClient, { unwrap } from './axiosClient';

export const healthApi = {
  check: () => axiosClient.get('/api/health').then(unwrap)
};
