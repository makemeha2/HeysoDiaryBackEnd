# syntax=docker/dockerfile:1.7
FROM maven:3.9-eclipse-temurin-17 AS build

ARG MAVEN_MIRROR_URL=""
ARG MAVEN_RETRY_COUNT=4

WORKDIR /app
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
  fi; \
  if [ -n "$MAVEN_MIRROR_URL" ]; then \
  SETTINGS_ARGS="-s /tmp/settings.xml"; \
  else \
  SETTINGS_ARGS=""; \
  fi; \
  for i in $(seq 1 "$MAVEN_RETRY_COUNT"); do \
  mvn -B -U -ntp -DskipTests -Dmaven.wagon.http.retryHandler.count=5 $SETTINGS_ARGS package && break; \
  if [ "$i" -eq "$MAVEN_RETRY_COUNT" ]; then exit 1; fi; \
  sleep 5; \
  done

# ---- runtime ----
FROM eclipse-temurin:17-jre

# healthcheck에서 curl 사용 가능하도록 설치
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

# (옵션) non-root 사용자로 실행
RUN useradd -u 10001 -m appuser
USER 10001

WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 19090
ENTRYPOINT ["java","-jar","/app/app.jar"]
