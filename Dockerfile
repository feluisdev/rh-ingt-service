FROM maven:3.9.9-eclipse-temurin-21 AS build
ENV APP_HOME=/app

COPY src $APP_HOME/src
COPY pom.xml $APP_HOME/pom.xml
COPY opentelemetry-javaagent.jar $APP_HOME/opentelemetry-javaagent.jar

WORKDIR $APP_HOME
RUN mvn package -DskipTests


FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar
COPY --from=build /app/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

EXPOSE ${SERVICE_PORT}
CMD ["sh", "-c", "if [ \"$OTEL_ENABLED\" != \"false\" ]; then java -javaagent:/app/opentelemetry-javaagent.jar -Dotel.traces.exporter=${OTEL_TRACES_EXPORTER} -Dotel.metrics.exporter=${OTEL_METRICS_EXPORTER} -Dotel.logs.exporter=${OTEL_LOGS_EXPORTER} -Dotel.exporter.otlp.endpoint=${OTEL_COLLECTOR_ENDPOINT} -Dotel.service.name=${OTEL_SERVICE_NAME} -jar /app/app.jar; else java -jar /app/app.jar; fi"]
