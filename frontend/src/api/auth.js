import http from './http.js';

export function getCsrf() {
  return http.get('/auth/csrf');
}

export function register(values) {
  return http.post('/auth/register', values);
}

export function login(values) {
  return http.post('/auth/login', values);
}

export function logout() {
  return http.post('/auth/logout');
}

export function getMe() {
  return http.get('/auth/me');
}
