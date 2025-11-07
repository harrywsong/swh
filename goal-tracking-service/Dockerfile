# ------
# Build Stage
# ------
FROM gradle:8-jdk21 AS builder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN ./gradlew build -x test

# ------
# Package Stage
# ------
FROM eclipse-temurin:21-jdk

RUN mkdir /app

COPY --from=builder /home/gradle/src/build/libs/*.jar /app/goal-tracking-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/goal-tracking-service.jar"]