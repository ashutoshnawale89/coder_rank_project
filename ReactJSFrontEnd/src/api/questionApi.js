import axiosClient, { unwrap } from './axiosClient';

export const questionApi = {
  list: ({ page = 0, size = 20, sort = 'createdAt,desc' } = {}) =>
    axiosClient.get('/api/questions', { params: { page, size, sort } }).then(unwrap),

  get: (id) => axiosClient.get(`/api/questions/${id}`).then(unwrap),

  solve: (id, { language, code }) =>
    axiosClient.post(`/api/questions/${id}/solve`, { language, code }).then(unwrap)
};

export const solutionApi = {
  list: ({ page = 0, size = 20, questionId, sort = 'createdAt,desc' } = {}) =>
    axiosClient
      .get('/api/solutions', { params: { page, size, sort, questionId } })
      .then(unwrap)
};

export const adminQuestionApi = {
  create: (payload) =>
    axiosClient.post('/api/admin/questions', payload).then(unwrap),

  get: (id) => axiosClient.get(`/api/admin/questions/${id}`).then(unwrap),

  update: (id, payload) =>
    axiosClient.put(`/api/admin/questions/${id}`, payload).then(unwrap),

  delete: (id) => axiosClient.delete(`/api/admin/questions/${id}`).then(unwrap)
};
