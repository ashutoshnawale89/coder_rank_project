import axiosClient, { unwrap } from './axiosClient';

export const submissionApi = {
  list: ({ page = 0, size = 20, sort = 'createdAt,desc' } = {}) =>
    axiosClient.get('/api/submissions', { params: { page, size, sort } }).then(unwrap),

  get: (id) => axiosClient.get(`/api/submissions/${id}`).then(unwrap)
};
