import axiosClient, { unwrap } from './axiosClient';

export const snippetApi = {
  create: ({ title, language, code }) =>
    axiosClient.post('/api/snippets', { title, language, code }).then(unwrap),

  list: ({ page = 0, size = 20, sort = 'createdAt,desc' } = {}) =>
    axiosClient.get('/api/snippets', { params: { page, size, sort } }).then(unwrap),

  get: (id) => axiosClient.get(`/api/snippets/${id}`).then(unwrap),

  delete: (id) => axiosClient.delete(`/api/snippets/${id}`).then(unwrap)
};
