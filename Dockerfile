# ============================
# 1. BUILD STAGE (MAVEN)
# ============================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar pom.xml primeiro para resolver contexto
COPY pom.xml .

# Copiar o código fonte
COPY src ./src

# Construir o jar executável (com spring-boot:repackage)
RUN mvn -B clean package -DskipTests

# ============================
# 2. RUNTIME STAGE (JRE)
# ============================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar o jar executável da etapa de build
COPY --from=build /app/target/RH-Service-*.jar /app/app.jar

# Expor a porta padrão do Spring Boot
EXPOSE 8080

# ENTRYPOINT padrão para apps Spring Boot
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
