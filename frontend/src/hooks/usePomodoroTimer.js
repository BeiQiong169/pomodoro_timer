import { useCallback, useEffect, useRef, useState } from 'react';
import { message } from 'antd';
import { recordPomodoro } from '../api/pomodoro.js';
import { LONG_BREAK_EVERY, PHASES, WORK_MINUTES } from '../constants/timer.js';

function createIdleState(phase = 'WORK', completedInCycle = 0) {
  return {
    phase,
    status: 'IDLE',
    endAt: null,
    remainingMs: PHASES[phase].seconds * 1000,
    completedInCycle,
    taskId: null,
    clientToken: null,
  };
}

export function usePomodoroTimer(selectedTaskId, onPomodoroRecorded) {
  const [timer, setTimer] = useState(() => createIdleState());
  const timerRef = useRef(timer);
  const completionRef = useRef(false);
  const onRecordedRef = useRef(onPomodoroRecorded);

  useEffect(() => {
    timerRef.current = timer;
  }, [timer]);

  useEffect(() => {
    onRecordedRef.current = onPomodoroRecorded;
  }, [onPomodoroRecorded]);

  const complete = useCallback((current) => {
    if (completionRef.current) {
      return;
    }
    completionRef.current = true;

    if (current.phase === 'WORK') {
      const nextCount = current.completedInCycle + 1;
      const nextPhase = nextCount >= LONG_BREAK_EVERY ? 'LONG_BREAK' : 'SHORT_BREAK';
      setTimer(createIdleState(nextPhase, nextCount));

      if (current.clientToken) {
        recordPomodoro({
          taskId: current.taskId,
          durationMinutes: WORK_MINUTES,
          clientToken: current.clientToken,
        })
          .then(() => {
            message.success(
              nextPhase === 'LONG_BREAK'
                ? '完成 4 个番茄，进入长休息'
                : '番茄完成，休息一下',
            );
            onRecordedRef.current?.();
          })
          .catch((err) => {
            message.error(err.response?.data?.message || '番茄记录保存失败');
          });
      } else {
        message.warning('本次专注未记录');
      }
      return;
    }

    const nextCount = current.phase === 'LONG_BREAK' ? 0 : current.completedInCycle;
    setTimer(createIdleState('WORK', nextCount));
    message.info('休息结束，开始下一轮专注');
  }, []);

  const start = useCallback(() => {
    completionRef.current = false;
    setTimer((prev) => {
      if (prev.status !== 'IDLE') {
        return prev;
      }
      const isWork = prev.phase === 'WORK';
      return {
        ...prev,
        status: 'RUNNING',
        endAt: Date.now() + prev.remainingMs,
        clientToken: isWork ? crypto.randomUUID() : null,
        taskId: isWork ? selectedTaskId : null,
      };
    });
  }, [selectedTaskId]);

  const pause = useCallback(() => {
    setTimer((prev) => {
      if (prev.status !== 'RUNNING' || prev.endAt == null) {
        return prev;
      }
      return {
        ...prev,
        status: 'PAUSED',
        endAt: null,
        remainingMs: Math.max(0, prev.endAt - Date.now()),
      };
    });
  }, []);

  const resume = useCallback(() => {
    completionRef.current = false;
    setTimer((prev) => {
      if (prev.status !== 'PAUSED') {
        return prev;
      }
      return {
        ...prev,
        status: 'RUNNING',
        endAt: Date.now() + prev.remainingMs,
      };
    });
  }, []);

  const reset = useCallback(() => {
    completionRef.current = false;
    setTimer((prev) => createIdleState(prev.phase, prev.completedInCycle));
  }, []);

  useEffect(() => {
    if (timer.status !== 'RUNNING') {
      return undefined;
    }

    const tick = () => {
      const current = timerRef.current;
      if (current.status !== 'RUNNING' || current.endAt == null) {
        return;
      }
      const remaining = Math.max(0, current.endAt - Date.now());
      if (remaining <= 0) {
        complete(current);
      } else {
        setTimer((prev) => (
          prev.status === 'RUNNING' ? { ...prev, remainingMs: remaining } : prev
        ));
      }
    };

    const intervalId = window.setInterval(tick, 250);
    return () => window.clearInterval(intervalId);
  }, [timer.status, complete]);

  useEffect(() => {
    const sync = () => {
      const current = timerRef.current;
      if (current.status !== 'RUNNING' || current.endAt == null) {
        return;
      }
      const remaining = Math.max(0, current.endAt - Date.now());
      if (remaining <= 0) {
        complete(current);
      } else {
        setTimer((prev) => (
          prev.status === 'RUNNING' ? { ...prev, remainingMs: remaining } : prev
        ));
      }
    };

    document.addEventListener('visibilitychange', sync);
    return () => document.removeEventListener('visibilitychange', sync);
  }, [complete]);

  return { timer, start, pause, resume, reset };
}
