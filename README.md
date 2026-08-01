# 图片墙 (Picture Wall)

## 🛠 技术栈
- Frontend: Vue 3 + WindiCSS
- Backend: Spring Boot 2.7 (Java 8)
- Database: MySQL 8.0

## � Docker 镜像加速配置 (Docker Accelerator)
由于网络原因，构建可能较慢。强烈建议配置阿里云镜像加速器：

1. 打开 Docker Desktop 设置 -> **Docker Engine**
2. 在 `registry-mirrors` 数组中添加阿里云加速地址：
```json
{
  "registry-mirrors": [
    "https://registry.cn-hangzhou.aliyuncs.com"
  ]
}
```
3. 点击 **Apply & Restart**

## 🚀 启动指南 (How to Run)

### 方式一：Docker 启动 (推荐)
1. 确保已配置上述镜像加速。
2. 在根目录执行：`docker compose up --build`
3. 等待容器启动完成...

### 方式二：本地开发环境启动
适用于需要在 Java 8 环境下直接运行的场景。详细步骤请看下方的 [本地开发指南](#-本地开发指南-local-development-guide)。

## 🔗 服务地址 (Services)
- Frontend: http://localhost:4140
- Backend API: http://localhost:8140/api/list
- Database: localhost:33060 (user: root / pass: root)

## 🧪 测试账号
- 本项目为公开图片墙，无需登录即可使用。


---

## 📖 本地开发指南 (Local Development Guide)

本文档提供在 Java 8 环境下直接运行项目的完整说明，无需 Docker。

### 📋 前置要求 (Prerequisites)

请确保已安装以下软件：

- **Java 8** (JDK 1.8)
- **Maven 3.6+**
- **MySQL 8.0**
- **Node.js 16+** 和 **npm**

#### 验证安装

```bash
java -version    # 应显示 1.8.x
mvn -version     # 应显示 3.6+
mysql --version  # 应显示 8.0.x
node -v          # 应显示 16.x 或更高
npm -v           # 应显示 npm 版本
```

### 🗄️ 数据库配置 (Database Setup)

#### 1. 安装 MySQL 8.0

**macOS (使用 Homebrew):**
```bash
brew install mysql@8.0
brew services start mysql@8.0
```

**Windows:**
下载并安装 [MySQL Community Server 8.0](https://dev.mysql.com/downloads/mysql/)

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
```

#### 2. 创建数据库

启动 MySQL 并创建数据库：

```bash
## 登录 MySQL (密码默认为空，或根据安装时设置的密码)
mysql -u root -p

## 在 MySQL 命令行中执行
CREATE DATABASE picture_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

#### 3. 配置数据库用户（可选）

如果需要使用非 root 用户：

```sql
CREATE USER 'picture_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON picture_db.* TO 'picture_user'@'localhost';
FLUSH PRIVILEGES;
```

> **注意**: 项目使用 JPA 的 `ddl-auto: update` 模式，数据库表会在首次启动时自动创建。

### 🔧 后端配置与启动 (Backend Setup)

#### 1. 修改配置文件

编辑 `picture-backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    # 将 picture-mysql 改为 localhost
    url: jdbc:mysql://localhost:3306/picture_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&enabledTLSProtocols=TLSv1.2
    username: root
    password: root  # 修改为你的 MySQL 密码
```

#### 2. 构建并启动后端

```bash
cd picture-backend

## 构建项目
mvn clean package

## 启动应用
java -jar target/picture-backend-0.0.1-SNAPSHOT.jar
```

或者使用 Maven 插件直接运行：

```bash
mvn spring-boot:run
```

#### 3. 验证后端

后端启动成功后，访问：
- API 测试: http://localhost:8140/api/list
- 应该返回空数组 `[]` (初始状态)

> **注意**: 图片会保存到项目根目录的 `images/` 文件夹，该文件夹会自动创建。

### 🎨 前端配置与启动 (Frontend Setup)

#### 1. 安装依赖

```bash
cd picture-frontend

## 安装 npm 依赖
npm install
```

#### 2. 配置开发服务器

检查 `vite.config.js` 中的代理配置（已配置好，无需修改）：

```javascript
server: {
  port: 4140,
  proxy: {
    '/api': {
      target: 'http://localhost:8140',
      changeOrigin: true
    }
  }
}
```

#### 3. 启动前端开发服务器

```bash
npm run dev
```

#### 4. 访问应用

前端启动成功后，访问：
- 应用地址: http://localhost:4140

### 🚀 完整启动流程

按以下顺序启动：

```bash
## 1. 确保 MySQL 正在运行
## macOS: brew services start mysql@8.0
## Linux: sudo systemctl start mysql

## 2. 启动后端（新终端窗口）
cd picture-backend
mvn spring-boot:run

## 3. 启动前端（新终端窗口）
cd picture-frontend
npm run dev
```

### 🔗 服务地址汇总

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端应用 | http://localhost:4140 | Vue 3 应用 |
| 后端 API | http://localhost:8140/api/list | Spring Boot REST API |
| 数据库 | localhost:3306 | MySQL 8.0 |

### 🐛 常见问题 (Troubleshooting)

#### 后端无法连接数据库

**错误**: `Communications link failure`

**解决**:
1. 确认 MySQL 正在运行: `mysql -u root -p`
2. 检查 `application.yml` 中的数据库密码是否正确
3. 确认数据库 `picture_db` 已创建

#### 端口已被占用

**错误**: `Address already in use: 8140` 或 `4140`

**解决**:
```bash
## macOS/Linux - 查找并终止占用端口的进程
lsof -i :8140  # 或 :4140
kill -9 <PID>

## Windows
netstat -ano | findstr :8140
taskkill /PID <PID> /F
```

#### Maven 构建失败

**错误**: `Plugin ... not found`

**解决**:
```bash
## 清理 Maven 缓存并重新构建
mvn clean install -U
```

#### 前端依赖安装失败

**解决**:
```bash
## 清理缓存并重新安装
rm -rf node_modules package-lock.json
npm install
```

#### 图片上传保存路径问题

默认图片保存在 `<项目根目录>/images/`。确保应用有写入权限。

### 📝 开发提示

- **热重载**: 前端修改会自动刷新，后端修改需要重启服务
- **日志查看**: 后端日志会显示 SQL 语句（`show-sql: true`）
- **数据库工具**: 推荐使用 MySQL Workbench 或 DBeaver 查看数据
- **API 测试**: 推荐使用 Postman 或 curl 测试 API 接口

### 🔄 切换回 Docker 模式

如需切换回 Docker 启动，请将 `application.yml` 中的数据库地址改回：

```yaml
url: jdbc:mysql://picture-mysql:3306/picture_db...
```

然后使用: `docker compose up --build`
