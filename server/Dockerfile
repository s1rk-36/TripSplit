FROM maven:3.8-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/target/server-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
# Render's free tier caps the instance at 512MB. Constrain the JVM so it stays under
# that limit: SerialGC has the smallest footprint, cap the heap well below the total,
# bound metaspace, and shrink thread stacks. Without these the JVM sizes itself too
# greedily and the container is OOM-killed on startup.
ENTRYPOINT ["java", \
  "-XX:+UseSerialGC", \
  "-XX:MaxRAMPercentage=55.0", \
  "-XX:MaxMetaspaceSize=128m", \
  "-Xss512k", \
  "-jar", "app.jar"]
