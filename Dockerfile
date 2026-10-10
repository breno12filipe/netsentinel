# --- FASE 1: Build do projeto Java ---
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copia os arquivos do Maven e resolve dependencias primeiro (aproveita cache do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o codigo fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests

# --- FASE 2: Imagem final para execucao ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Instala curl para healthcheck do container
RUN apk add --no-cache curl

# Copia o JAR gerado na fase anterior
COPY --from=builder /app/target/*.jar app.jar

# Expoe a porta da API REST do NetSentinel
EXPOSE 8080

# Comando para rodar a aplicacao
ENTRYPOINT ["java", "-jar", "app.jar"]