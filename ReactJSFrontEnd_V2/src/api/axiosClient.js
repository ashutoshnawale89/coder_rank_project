import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const axiosClient = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' }
});

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('cr_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem('cr_token');
      localStorage.removeItem('cr_user');
      if (!window.location.pathname.startsWith('/login') &&
          !window.location.pathname.startsWith('/register')) {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  }
);

// Unwrap the backend ApiResponse envelope: { success, message, data, timestamp }
export const unwrap = (response) => response?.data?.data;

// Extract a friendly error message from an Axios error
export const extractErrorMessage = (err) => {
  const body = err?.response?.data;
  if (!body) return err?.message || 'Network error';
  if (Array.isArray(body.details) && body.details.length) {
    return `${body.message}: ${body.details.join(', ')}`;
  }
  return body.message || body.error || err.message || 'Request failed';
};

export default axiosClient;
