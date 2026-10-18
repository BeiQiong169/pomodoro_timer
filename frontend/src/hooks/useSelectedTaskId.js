import { useCallback, useState } from 'react';

const STORAGE_KEY = 'pomodoro.selectedTaskId';

function readSelectedTaskId() {
  const raw = window.localStorage.getItem(STORAGE_KEY);
  const id = Number(raw);
  return Number.isInteger(id) && id > 0 ? id : null;
}

export function useSelectedTaskId() {
  const [selectedTaskId, setSelectedTaskIdState] = useState(readSelectedTaskId);

  const setSelectedTaskId = useCallback((taskId) => {
    setSelectedTaskIdState(taskId);
    if (taskId == null) {
      window.localStorage.removeItem(STORAGE_KEY);
    } else {
      window.localStorage.setItem(STORAGE_KEY, String(taskId));
    }
  }, []);

  const clearSelectedTask = useCallback(() => {
    setSelectedTaskId(null);
  }, [setSelectedTaskId]);

  return { selectedTaskId, setSelectedTaskId, clearSelectedTask };
}
