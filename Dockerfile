FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# (선택) 의존성 캐시를 위해 먼저 다운로드
RUN mvn -q -e -DskipTests dependency:go-offline
COPY . .
RUN mvn -q -DskipTests package

# 2) run
FROM eclipse-temurin:17-jre
WORKDIR /app

# Spring Boot jar 이름이 프로젝트마다 다르므로 target/*.jar를 복사
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 19090
ENTRYPOINT ["java","-jar","/app/app.jar"]