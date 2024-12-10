FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY /build/libs/BrusnikaCoworking-*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]