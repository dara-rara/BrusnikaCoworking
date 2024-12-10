FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY libs/ .
ENTRYPOINT ["java","-jar","/app.jar"]