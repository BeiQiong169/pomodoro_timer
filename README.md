# 番茄钟 + 任务记录

一个用于学习全栈开发的最小可用项目：前端运行番茄计时器，后端保存任务与已完成的番茄记录，并提供今日和近 7 天统计。

## 技术栈

- 后端：Java 25、Spring Boot 3.5、Spring Security（Session + CSRF）、MyBatis 3、MySQL 8、Maven
- 前端：React 18、Vite、Ant Design 5、axios、React Router
- 认证：HttpSession + Cookie、BCrypt 密码哈希

## 功能

- 注册、登录、登出，刷新页面保持登录
- 任务新增、编辑、完成/取消完成、删除
- 当前任务选择，保存到 `localStorage`
- 25 分钟专注、5 分钟短休、15 分钟长休
- 每完成 4 个专注进入长休息，长休结束后本轮计数归零
- 暂停、继续、重置；倒计时基于 `endAt` 时间戳
- 完成专注后上报番茄记录，`clientToken` 保证幂等
- 今日番茄数、专注分钟、完成任务数
- 近 7 天纯 CSS 柱状图，缺失日期补 0

## 目录结构

```text
backend/   Spring Boot 后端
frontend/  React 前端
db/        本地数据库脚本（含本地账号密码，未提交到仓库）
```

## 本地运行

### 环境

- JDK 25
- Maven 3.9+
- Node.js 20+
- MySQL 8

### 数据库

本地初始化脚本是 `db/init.sql`，包含本地账号密码，已加入 `.gitignore`，不会提交到仓库。

请先在本地准备：

- 数据库 `pomodoro`
- 应用账号，以及 `users`、`task`、`pomodoro_session` 三张表

数据库密码放在 `backend/src/main/resources/application-local.yml`，该文件同样不入库。

### 后端

```bash
cd backend
mvn spring-boot:run
```

- 后端地址：`http://localhost:8080`
- 健康检查：`GET /api/health`

### 前端

```bash
cd frontend
npm install
npm run dev
```

- 前端地址：`http://localhost:5173`
- 浏览器只访问前端地址，`/api` 由 Vite 代理到 `http://localhost:8080`

## 测试与构建

```bash
cd backend
mvn test

cd frontend
npm run build
```

## 开发期加速计时

默认时长为 25/5/15 分钟。开发时可临时缩短秒数：

```bash
cd frontend
VITE_WORK_SECONDS=3 VITE_SHORT_BREAK_SECONDS=2 VITE_LONG_BREAK_SECONDS=2 npm run dev
```

上报给后端的 `durationMinutes` 仍固定为 25。

## API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/health` | 健康检查 |
| GET | `/api/auth/csrf` | 获取 CSRF Token |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/auth/me` | 恢复登录态 |
| GET | `/api/tasks` | 当前用户任务列表（含番茄数） |
| POST | `/api/tasks` | 新建任务 |
| PUT | `/api/tasks/{id}` | 修改标题和/或完成状态 |
| DELETE | `/api/tasks/{id}` | 删除任务 |
| POST | `/api/pomodoro-sessions` | 上报完成的番茄 |
| GET | `/api/stats/summary` | 今日汇总 + 近 7 天统计 |

写请求使用 Cookie `XSRF-TOKEN` 和请求头 `X-XSRF-TOKEN`。未登录访问受保护接口返回 401，CSRF 校验失败返回 403。
