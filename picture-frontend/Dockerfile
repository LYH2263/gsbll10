# 使用国内镜像加速 (DaoCloud 公开镜像)
FROM docker.m.daocloud.io/library/node:20-alpine AS build
RUN npm config set registry https://registry.npmmirror.com
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# 使用国内镜像加速
FROM docker.m.daocloud.io/library/nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 4140
CMD ["nginx", "-g", "daemon off;"]
