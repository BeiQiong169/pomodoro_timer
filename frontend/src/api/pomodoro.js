import http from './http.js';

export function recordPomodoro(values) {
  return http.post('/pomodoro-sessions', values);
}
