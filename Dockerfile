# ============================
# 1. BUILD STAGE (MAVEN)
# ============================
FROM cgr.dev/chainguard/maven:latest-dev AS build
WORKDIR /app

# Copiar apenas o pom.xml primeiro para aproveitar cache de dependências
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copiar o código fonte
COPY src ./src

# Construir o jar executável (com spring-boot:repackage)
RUN mvn -B -DskipTests clean package

# ============================
# 2. RUNTIME STAGE (JRE)
# ============================
FROM cgr.dev/chainguard/jre:latest
WORKDIR /app

# Copiar ONLY o jar executável, ignorando os -plain.jar e -original.jar
COPY --from=build /app/target/RH-Service-*.jar /app/app.jar

# Expor a porta padrão do Spring Boot
EXPOSE 8080

# ENTRYPOINT padrão para apps Spring Boot
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

