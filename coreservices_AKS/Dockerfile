# Build stage
FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the war file
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the built war file
COPY --from=build /app/target/coreservices-0.0.1-SNAPSHOT.war app.war

# Run the app
ENTRYPOINT ["java", "-jar", "app.war"]
