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
      '      <id>custom-mirror</id>' \
      '      <mirrorOf>*</mirrorOf>' \
      "      <url>${MAVEN_MIRROR_URL}</url>" \
      '    </mirror>' \
      '  </mirrors>' \
      '</settings>' \
      > /tmp/settings.xml; \
      SETTINGS_ARGS="--settings /tmp/settings.xml"; \
    else \
      SETTINGS_ARGS=""; \
  fi; \
  for i in $(seq 1 "$MAVEN_RETRY_COUNT"); do \
  mvn -B -U -ntp -DskipTests -Dmaven.wagon.http.retryHandler.count=5 $SETTINGS_ARGS dependency:go-offline && break; \
  if [ "$i" -eq "$MAVEN_RETRY_COUNT" ]; then exit 1; fi; \
  sleep 5; \
  done

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
  set -eux; \
  if [ -n "$MAVEN_MIRROR_URL" ] && [ -f /tmp/settings.xml ]; then \
  SETTINGS_ARGS="--settings /tmp/settings.xml"; \
  else \
  SETTINGS_ARGS=""; \
  fi; \
  for i in $(seq 1 "$MAVEN_RETRY_COUNT"); do \
  mvn -B -U -ntp -DskipTests -Dmaven.wagon.http.retryHandler.count=5 $SETTINGS_ARGS package && break; \
  if [ "$i" -eq "$MAVEN_RETRY_COUNT" ]; then exit 1; fi; \
  sleep 5; \
  done

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 19090
ENTRYPOINT ["java","-jar","/app/app.jar"]
