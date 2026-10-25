# ---------- Stage 1: build (Maven se jar banao) ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---------- Stage 2: run (chhota JRE image) ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar

# Free hosting ki chhoti memory ke liye heap limit
ENV JAVA_OPTS="-Xmx300m"
EXPOSE 8080
# PORT env variable ko Spring (server.port=${PORT:8080}) khud padh leta hai
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
