# Use an official openjdk image as a parent image
FROM openjdk:17-slim-buster

# Set the working directory in the container to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Make port 80 available to the world outside this container
EXPOSE 80

# Run the application
CMD ["java", "-jar", "/app/app.jar"]