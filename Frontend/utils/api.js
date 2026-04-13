import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Detect backend type on load
const detectBackend = async () => {
  try {
    // Probe for .NET SignalR negotiate endpoint
    await axios.get('/hubs/notifications/negotiate?negotiateVersion=1', { timeout: 1500 });
    localStorage.setItem('VITE_BACKEND_TYPE', 'DOTNET');
    return 'DOTNET';
  } catch (e) {
    localStorage.setItem('VITE_BACKEND_TYPE', 'SPRINGBOOT');
    return 'SPRINGBOOT';
  }
};

api.interceptors.request.use(
  async (config) => {
    // Re-verify if not set
    if (!localStorage.getItem('VITE_BACKEND_TYPE')) {
      await detectBackend();
    }

    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    // If we get a 404 or connection error, maybe the backend switched? 
    // Re-detect once to be safe.
    if (!error.response || error.response.status === 404) {
      await detectBackend();
    }

    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('role');
        window.location.href = '/login';
      }
      const errorMessage = error.response.data?.message || error.response.data || 'An error occurred';
      return Promise.reject(new Error(errorMessage));
    }
    return Promise.reject(error);
  }
);

export default api;
export { detectBackend };

export const setAuthToken = (token) => {
  localStorage.setItem('token', token);
};

export const getAuthToken = () => {
  return localStorage.getItem('token');
};

export const removeAuthToken = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  localStorage.removeItem('role');
};

export const setUserData = (user, role) => {
  localStorage.setItem('user', JSON.stringify(user));
  localStorage.setItem('role', role);
};

export const getUserData = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};

export const getUserRole = () => {
  return localStorage.getItem('role');
};
