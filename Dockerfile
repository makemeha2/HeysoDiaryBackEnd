# syntax=docker/dockerfile:1.7
FROM maven:3.9-eclipse-temurin-17 AS build

ARG MAVEN_MIRROR_URL=""
ARG MAVEN_RETRY_COUNT=4

WORKDIR /app

# 1) pom 먼저 복사 → 의존성만 먼저 받기(캐시 효율)
COPY pom.xml ./

RUN --mount=type=cache,target=/root/.m2 \
  set -eux; \
  if [ -n "$MAVEN_MIRROR_URL" ]; then \
  printf '%s\n' \
  '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">' \
  '  <mirrors>' \
  '    <mirror>' \
  '      <id>custom</id>' \
  '      <mirrorOf>*</mirrorOf>' \
  "      <url>${MAVEN_MIRROR_URL}</url>" \
  '    </mirror>' \
  '  </mirrors>' \
  '</settings>' > /tmp/settings.xml; \
  SETTINGS_ARGS="--settings /tmp/settings.xml"; \
  else \
  SETTINGS_ARGS=""; \
  fi; \
  for i in $(seq 1 "$MAVEN_RETRY_COUNT"); do \
  mvn -B -U -ntp -DskipTests -Dmaven.wagon.http.retryHandler.count=5 $SETTINGS_ARGS dependency:go-offline && break; \
  if [ "$i" -eq "$MAVEN_RETRY_COUNT" ]; then exit 1; fi; \
  sleep 5; \
  done

# 2) 이제 소스 복사
COPY . .

# 3) 소스 포함된 상태에서 package 실행 (여기서 main class 탐지됨)
RUN --mount=type=cache,target=/root/.m2 \
  set -eux; \
  if [ -f /tmp/settings.xml ]; then SETTINGS_ARGS="--settings /tmp/settings.xml"; else SETTINGS_ARGS=""; fi; \
  for i in $(seq 1 "$MAVEN_RETRY_COUNT"); do \
  mvn -B -U -ntp -DskipTests -Dmaven.wagon.http.retryHandler.count=5 $SETTINGS_ARGS package && break; \
  if [ "$i" -eq "$MAVEN_RETRY_COUNT" ]; then exit 1; fi; \
  sleep 5; \
  done

# ---- runtime ----
FROM eclipse-temurin:17-jre

# healthcheck에서 curl을 쓰려면 런타임 이미지에 설치 필요
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

# (권장) non-root 실행
RUN useradd -u 10001 -m appuser
USER 10001

WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 19090
ENTRYPOINT ["java","-jar","/app/app.jar"]
