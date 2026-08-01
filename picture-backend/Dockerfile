# 使用国内镜像加速 (DaoCloud 公开镜像)
FROM docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app
COPY settings.xml .
COPY pom.xml .
COPY src ./src
# Use settings.xml for dependency download
RUN mvn -s settings.xml package -DskipTests

# 使用国内镜像加速 - Amazon Corretto 8
FROM docker.m.daocloud.io/library/amazoncorretto:8-alpine-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8140
ENTRYPOINT ["java", "-jar", "app.jar"]
