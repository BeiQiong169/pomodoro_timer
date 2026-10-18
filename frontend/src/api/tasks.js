import http from './http.js';

export function listTasks() {
  return http.get('/tasks');
}

export function createTask(values) {
  return http.post('/tasks', values);
}

export function updateTask(id, values) {
  return http.put(`/tasks/${id}`, values);
}

export function deleteTask(id) {
  return http.delete(`/tasks/${id}`);
}
