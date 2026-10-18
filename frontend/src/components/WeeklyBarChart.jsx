import { Empty, Tooltip } from 'antd';

const MAX_BAR_HEIGHT = 140;

function formatDate(date) {
  return date.slice(5);
}

export default function WeeklyBarChart({ data }) {
  const maxCount = Math.max(...data.map((day) => day.pomodoroCount), 0);

  if (maxCount === 0) {
    return (
      <Empty
        image={Empty.PRESENTED_IMAGE_SIMPLE}
        description="近 7 天还没有番茄记录"
      />
    );
  }

  return (
    <div className="weekly-chart">
      {data.map((day) => {
        const height = day.pomodoroCount === 0
          ? 2
          : Math.max(4, (day.pomodoroCount / maxCount) * MAX_BAR_HEIGHT);

        return (
          <Tooltip
            key={day.date}
            title={`${day.date}：${day.pomodoroCount} 个番茄`}
          >
            <div className="weekly-chart-column">
              <div className="weekly-chart-bar-area">
                <div className="weekly-chart-bar" style={{ height }} />
              </div>
              <span className="weekly-chart-date">{formatDate(day.date)}</span>
            </div>
          </Tooltip>
        );
      })}
    </div>
  );
}
