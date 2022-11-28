FROM eclipse-temurin:11-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
