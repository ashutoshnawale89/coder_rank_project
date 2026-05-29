import axiosClient, { unwrap } from './axiosClient';

export const executionApi = {
  execute: ({ language, code, stdin, snippetId }) =>
    axiosClient.post('/api/execute', { language, code, stdin, snippetId }).then(unwrap)
};
