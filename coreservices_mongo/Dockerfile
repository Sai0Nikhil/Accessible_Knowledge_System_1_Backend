# Build stage
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy pom.xml, mvnw script, .mvn folder, and source code
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src

# Give execution permissions to mvnw and build the war
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the built war file
COPY --from=build /app/target/coreservices-0.0.1-SNAPSHOT.war app.war

ENV JAVA_TOOL_OPTIONS="-XX:TieredStopAtLevel=1 -Xmx256m -Xms256m"

# Run the app
ENTRYPOINT ["java", "-jar", "app.war"]
