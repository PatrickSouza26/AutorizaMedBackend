# Etapa 1: Build da aplicação com Maven e Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia dependências primeiro para cache eficiente
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e gera o jar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final leve para execução
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render define a variável PORT dinamicamente (padrão 8080)
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
