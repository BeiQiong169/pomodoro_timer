import http from './http.js';

export function getStatsSummary() {
  return http.get('/stats/summary');
}
