FROM node:24-alpine AS web
WORKDIR /web
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
# 打包前端頁面
COPY --from=web /web/dist ./src/main/resources/static
# 測試交給CI
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 10001 reader \
    && mkdir /data \
    && chown reader /data
COPY --from=build /app/target/reader-backend-*.jar app.jar
ENV STORAGE_ROOT=/data
USER reader
EXPOSE 8080
# 容許慢速啟動
HEALTHCHECK --interval=10s --timeout=5s --start-period=300s --start-interval=5s --retries=6 \
    CMD bash -c '</dev/tcp/127.0.0.1/8080' || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
