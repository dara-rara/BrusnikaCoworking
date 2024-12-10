FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY app.jar
ENTRYPOINT ["java","-jar","/app.jar"]