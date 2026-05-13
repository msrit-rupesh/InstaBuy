import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const url = config.url || '';
    config.headers.set("Content-Type","application/json");

    const isAuthRoute = url.includes('/auth/');
    if (isAuthRoute) {
      return config;
    }

    const token = localStorage.getItem('accessToken');
    if (token) {
      // Axios v1: safe way → config.headers is AxiosHeaders
      config.headers.set('Authorization', `Bearer ${token}`);
      console.log(`[API Request] JWT added for: ${config.method?.toUpperCase()} ${url}`);
    } else {
      console.warn(`[API Request] NO token found for: ${config.method?.toUpperCase()} ${url}`);
    }

    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && !error.config.url.includes('/auth/login')) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;