# ============================
# 1. BUILD STAGE (MAVEN JAVA 26)
# ============================
FROM maven:3-eclipse-temurin-26 AS build
WORKDIR /app

# Copiar pom.xml primeiro para resolver contexto
COPY pom.xml .

# Copiar o código fonte
COPY src ./src

# Construir o jar executável (com spring-boot:repackage)
RUN mvn -B clean package -DskipTests

# ============================
# 2. RUNTIME STAGE (JRE JAVA 26)
# ============================
FROM eclipse-temurin:26-jre
WORKDIR /app

# Copiar o jar executável da etapa de build
COPY --from=build /app/target/RH-Service-*.jar /app/app.jar

# Expor a porta padrão do Spring Boot
EXPOSE 8080

# ENTRYPOINT padrão para apps Spring Boot
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
