export const WORK_MINUTES = 25;
export const SHORT_BREAK_MINUTES = 5;
export const LONG_BREAK_MINUTES = 15;
export const LONG_BREAK_EVERY = 4;

function secondsFromEnv(value, fallback) {
  const seconds = Number(value);
  return Number.isFinite(seconds) && seconds > 0 ? seconds : fallback;
}

export const PHASES = {
  WORK: {
    label: '专注',
    seconds: secondsFromEnv(import.meta.env.VITE_WORK_SECONDS, WORK_MINUTES * 60),
  },
  SHORT_BREAK: {
    label: '短休息',
    seconds: secondsFromEnv(import.meta.env.VITE_SHORT_BREAK_SECONDS, SHORT_BREAK_MINUTES * 60),
  },
  LONG_BREAK: {
    label: '长休息',
    seconds: secondsFromEnv(import.meta.env.VITE_LONG_BREAK_SECONDS, LONG_BREAK_MINUTES * 60),
  },
};
