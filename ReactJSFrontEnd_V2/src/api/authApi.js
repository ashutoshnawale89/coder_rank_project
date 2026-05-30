import axiosClient, { unwrap } from './axiosClient';

export const authApi = {
  register: ({ username, email, password }) =>
    axiosClient.post('/api/auth/register', { username, email, password }).then(unwrap),

  login: ({ username, password }) =>
    axiosClient.post('/api/auth/login', { username, password }).then(unwrap)
};
