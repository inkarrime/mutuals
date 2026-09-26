FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home mutuals \
    && mkdir -p /app/uploads \
    && chown -R mutuals:mutuals /app
COPY --from=build --chown=mutuals:mutuals /app/target/mutuals-backend-*.jar app.jar
USER mutuals
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
