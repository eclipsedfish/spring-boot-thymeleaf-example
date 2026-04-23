FROM eclipse-temurin:8-jdk AS base

WORKDIR /app

COPY .mvn ./.mvn
COPY mvnw ./
COPY pom.xml ./

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src

FROM base AS test
RUN ./mvnw --batch-mode --no-transfer-progress clean test

FROM base AS package
RUN ./mvnw --batch-mode --no-transfer-progress clean package -DskipTests

FROM eclipse-temurin:8-jre

WORKDIR /app

COPY --from=package /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
