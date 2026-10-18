import axios from 'axios';

const http = axios.create({
  baseURL: '/api',
  withCredentials: true,
});

const AUTH_ENDPOINTS = ['/auth/login', '/auth/register', '/auth/me', '/auth/csrf', '/auth/logout'];

function isAuthEndpoint(url = '') {
  return AUTH_ENDPOINTS.some((endpoint) => url.includes(endpoint));
}

function readCookie(name) {
  const prefix = `${name}=`;
  const part = document.cookie
    .split('; ')
    .find((item) => item.startsWith(prefix));
  return part ? decodeURIComponent(part.slice(prefix.length)) : null;
}

http.interceptors.request.use((config) => {
  const method = (config.method || 'get').toUpperCase();
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const token = readCookie('XSRF-TOKEN');
    if (token) {
      config.headers = config.headers || {};
      config.headers['X-XSRF-TOKEN'] = token;
    }
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response, config } = error;
    if (!response || !config) {
      return Promise.reject(error);
    }

    if (
      response.status === 403
      && !config._csrfRetried
      && !String(config.url).includes('/auth/csrf')
    ) {
      config._csrfRetried = true;
      try {
        await http.get('/auth/csrf');
        return http(config);
      } catch (refreshError) {
        return Promise.reject(refreshError);
      }
    }

    if (response.status === 401 && !isAuthEndpoint(config.url)) {
      window.dispatchEvent(new Event('auth:unauthorized'));
    }

    return Promise.reject(error);
  },
);

export default http;
