# ===== Stage 1: Build =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests package

# ===== Stage 2: Runtime =====
FROM eclipse-temurin:17-jre-alpine
ENV JAVA_OPTS="-Xms256m -Xmx512m"
WORKDIR /app

# Create non-root user
RUN addgroup -S app && adduser -S app -G app

# Copy jar
COPY --chown=app:app --from=build /workspace/target/*.jar /app/app.jar

RUN mkdir -p /app/uploads/videos /app/uploads/images /app/uploads/temp \
  && chown -R app:app /app/uploads

USER app
EXPOSE 8080

ENTRYPOINT ["/bin/sh","-c","java ${JAVA_OPTS} -jar /app/app.jar"]
