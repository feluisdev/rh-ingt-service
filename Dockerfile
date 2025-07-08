# Use an official Maven image with JDK 21 to build the application
FROM maven:3.9.10-eclipse-temurin-21-alpine AS build

# Define build-time argument
ARG SERVICE_NAME

# Set the working directory
ENV APP_HOME /app
ENV SPRING_PROFILES_ACTIVE=${SPRING_ACTIVE_PROFILE}

# Copy the pom.xml and source code
COPY src $APP_HOME/src
COPY pom.xml $APP_HOME/pom.xml

WORKDIR $APP_HOME

# Package the application
RUN mvn package -DskipTests

# Runtime image
FROM eclipse-temurin:21-jre-alpine

# Define runtime environment variable
ENV SERVICE_NAME=${SERVICE_NAME}
ENV SPRING_PROFILES_ACTIVE=${SPRING_ACTIVE_PROFILE}

# Set the working directory
WORKDIR /app

RUN mkdir /config

COPY --from=build /app/target/*.jar /app/app.jar

# Expose the port that the application will run on
EXPOSE ${SERVICE_PORT}

# Command to run the application
CMD ["sh", "-c", "java -jar /app/app.jar"]