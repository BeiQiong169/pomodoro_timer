# CLAUDE.md

本文件是 `pomodoro_timer` 项目的开发约定，供后续 Claude 会话遵循。

## 项目概览

番茄钟 + 任务记录的全栈 MVP。

- 后端：Java 25 + Spring Boot 3.5.x + MyBatis 3.0.x + MySQL 8 + Maven
- 前端：React（JavaScript）+ Vite + Ant Design + axios + React Router
- 认证：Spring Security + HttpSession + Cookie
- 核心功能：番茄计时（开始/暂停/继续/重置）、任务列表、今日与近 7 天完成统计

## 架构铁律

1. **计时器只在前端运行。** 后端不维护计时状态、不做定时任务，只接收并保存已完成的番茄记录。
2. **用户身份只来自 Session。** 请求体中永远不要出现 `userId`；所有任务、番茄、统计 SQL 必须显式带 `user_id` 条件。
3. **只记录完成的 WORK 番茄。** 短休和长休只存在于前端状态机，不入库。
4. **倒计时基于 `endAt` 时间戳。** 每次 tick 用 `endAt - Date.now()` 重算，禁止每秒把剩余秒数减 1。
5. **浏览器只访问前端源。** 前端请求使用相对路径 `/api`，由 Vite 代理到 `http://localhost:8080`；不要让浏览器直接请求 8080。
6. **持久层只用 MyBatis XML。** 不引入 JPA、MyBatis-Plus 或动态 SQL 生成器。
7. **保持最小实现。** 不引入 Redux、TypeScript、图表库、Flyway、Docker、Lombok、JWT。

## 目录结构

```text
pomodoro_timer/
├─ backend/                  # Spring Boot
│  └─ src/main/java/com/pomodoro/
│     ├─ config/             # SecurityConfig
│     ├─ security/           # AppUserPrincipal、AppUserDetailsService
│     ├─ controller/         # Auth / Task / PomodoroSession / Stats
│     ├─ service/            # 业务规则、事务、用户隔离
│     ├─ mapper/             # MyBatis 接口
│     ├─ model/              # 数据库行对象
│     ├─ dto/                # 请求/响应 DTO
│     └─ exception/          # 统一异常处理
├─ frontend/                 # React + Vite
│  └─ src/
│     ├─ api/                # axios 实例和接口封装
│     ├─ auth/               # AuthContext、RequireAuth
│     ├─ pages/              # Login / Register / Dashboard
│     ├─ components/         # TimerCard / TaskPanel / StatsPanel / WeeklyBarChart
│     ├─ hooks/              # usePomodoroTimer、useSelectedTaskId
│     └─ constants/          # timer.js
├─ db/init.sql               # 建库、建账号、建表（root 执行一次）
├─ db/002-harden-app-user.sql # 收紧已有账号权限（执行一次）
└─ README.md
```

## 数据模型

### users

`id`、`username`（唯一）、`password_hash`、`created_at`

### task

`id`、`user_id`、`title`、`completed`、`completed_at`、`created_at`、`updated_at`

- 取消完成时 `completed_at` 置空。
- 重复提交「已完成」不能覆盖首次完成时间。

### pomodoro_session

`id`、`user_id`、`task_id`（可空）、`client_token`、`duration_minutes`、`completed_at`、`created_at`

- `UNIQUE (user_id, client_token)`，防止重复上报。
- `task_id` 外键 `ON DELETE SET NULL`，删除任务不丢历史统计。
- 统计按 `completed_at` 的日期分组，使用半开区间 `[start, end)`。
- 任务列表的番茄数通过 `LEFT JOIN + COUNT` 聚合，不在 `task` 表存冗余计数。
- 表结构由 `db/init.sql` 创建；应用账号只有 SELECT/INSERT/UPDATE/DELETE，应用启动时不执行 DDL。

## 后端约定

- 包名：`com.pomodoro`
- 分层：`Controller → Service → Mapper`；Controller 只做校验和取当前用户，业务规则放 Service。
- 所有 Controller 通过 `@AuthenticationPrincipal` 获取当前用户 id。
- 更新/删除他人资源返回 **404**，不返回 403，避免泄露资源是否存在。
- 事务：写操作 Service 方法加 `@Transactional`；统计查询只读。
- 注册重复用户名捕获 `DuplicateKeyException`，返回 409。
- 错误响应统一 JSON；状态码：400 校验、401 未登录、403 CSRF/权限、404 不存在、409 冲突、500 未知错误。
- 时间统一 `DATETIME`；JDBC 使用 `connectionTimeZone=%2B08:00&forceConnectionTimeZoneToSession=true`（URL 中 `+` 需编码），JVM 启动时固定 `Asia/Shanghai`。
- 本地数据库密码放在 `backend/src/main/resources/application-local.yml`（已加入 `.gitignore`）。

### API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/health` | 健康检查（公开；执行 `SELECT 1`，数据库不可用返回 503） |
| GET | `/api/auth/csrf` | 获取 CSRF Token |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录并创建 Session |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/auth/me` | 恢复登录态 |
| GET | `/api/tasks` | 当前用户任务列表（含番茄数） |
| POST | `/api/tasks` | 新建任务 |
| PUT | `/api/tasks/{id}` | 修改标题和/或完成状态 |
| DELETE | `/api/tasks/{id}` | 删除任务 |
| POST | `/api/pomodoro-sessions` | 记录完成番茄 |
| GET | `/api/stats/summary` | 今日汇总 + 近 7 天 |

创建番茄请求：`{taskId?, durationMinutes, clientToken}`，`completed_at` 由服务端生成。
统计响应固定包含 7 个 `last7Days` 项，缺失日期补 0。

### Session 认证要点

- 登录用 `AuthenticationManager` 校验。
- 认证成功后必须把 `SecurityContext` 显式保存进 Session（`SecurityContextRepository.saveContext`），否则会出现「登录 200，但后续 401」。
- 登录后调用 `request.changeSessionId()` 防会话固定。
- 启用 CSRF Cookie 双提交；前端写请求带 `X-XSRF-TOKEN`。
- 未登录返回 JSON 401，不要重定向到登录页。
- Session 存 Tomcat 内存即可，不引入 Spring Session/Redis。
- `SecurityConfig` 必须放行 `/api/health`；健康检查使用 `JdbcTemplate` 执行 `SELECT 1`，失败返回 503。

## 前端约定

- 路由：`/login`、`/register`、`/`（受保护 Dashboard）。
- 登录后单页布局：左侧 TimerCard，右侧 TaskPanel，下方 StatsPanel。
- 状态管理只用 React Context + hooks，不引入 Redux。
- `AuthContext` 负责登录态；axios 响应拦截器处理 401（清用户、跳登录）和 403（刷新 CSRF，不退登录）。
- 当前任务 id 存 `localStorage`；删除当前任务时清空选择。
- 计时开始后切换任务，只影响下一次番茄。
- 统计柱状图用纯 CSS 实现，不引入图表库。
- Ant Design 组件优先：`Card`、`List`、`Checkbox`、`Button`、`Popconfirm`、`Modal`、`Form`、`message`、`Statistic`、`Empty`、`Tooltip`。

### 计时器状态机

- 阶段：`WORK`（25 分钟）、`SHORT_BREAK`（5 分钟）、`LONG_BREAK`（15 分钟）。
- 状态：`IDLE` / `RUNNING` / `PAUSED`。
- 每完成 4 个 WORK，进入一次 LONG_BREAK。
- 动作：`START`、`PAUSE`、`RESUME`、`RESET`、`TICK`、`COMPLETE`。
- WORK 完成：POST 一次番茄记录，`completedInCycle + 1`，切到休息阶段并回到 IDLE，不自动开始。
- 休息完成：切回 WORK 并回到 IDLE。
- `RESET` 丢弃当前未完成的 `clientToken`。
- 用 `useRef` 防止重复完成；服务端唯一键兜底。
- 运行中的计时器首版不做刷新恢复；这是第二版增强。

## 常用命令

```bash
# 数据库初始化（MySQL 安装后执行一次）
mysql -u root -p < db/init.sql

# 已有旧环境收紧 pomodoro_app 权限（执行一次）
mysql -u root -p < db/002-harden-app-user.sql

# 后端
cd backend
mvn spring-boot:run
mvn test

# 前端
cd frontend
npm install
npm run dev
npm run build
```

- 前端地址：`http://localhost:5173`
- 后端地址：`http://localhost:8080`
- 浏览器统一使用 `localhost`，不要混用 `127.0.0.1`。
- MySQL 未安装时，先完成环境准备再启动后端。

## 实施顺序

按里程碑逐步推进，每步验证通过后再进入下一步：

1. M0：安装 MySQL，创建数据库和应用账号。
2. M1：后端骨架、表结构、健康检查。
3. M2：注册/登录/me/登出/CSRF。
4. M3：任务 CRUD + 用户隔离。
5. M4：番茄记录 + 今日/近 7 天统计。
6. M5：前端骨架 + 认证页面。
7. M6：任务面板。
8. M7：计时器。
9. M8：统计页面、单元测试、README。

## 验证要求

- 后端：`mvn test`；覆盖统计补零、用户隔离、番茄幂等、首次完成时间不被覆盖。
- 健康检查：数据库可用时返回 200，不可用时返回 503。
- 接口：用 curl 验证 201/200/204/400/401/403/404/409 和跨用户隔离。
- 前端：`npm run build` 通过；手工验收登录、任务、计时、统计、登出。
- 加速验证：通过 `frontend/src/constants/timer.js` 的环境变量把工作/休息秒数临时缩短，默认仍为 25/5/15 分钟；上报后端的 `durationMinutes` 仍按 25 计算。

## 明确不做

- JWT、邮箱验证、找回密码、第三方登录
- 任务优先级、标签、截止日期、分页、搜索
- Redux、TypeScript、图表库
- 计时器刷新恢复、多标签页同步、系统通知/声音
- Docker、CI/CD、生产部署、HTTPS
