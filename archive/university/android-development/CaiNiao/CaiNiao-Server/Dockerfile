FROM eclipse-temurin:21-jdk
LABEL authors="Oddy"

ADD ./target/CaiNiao-Server-*.jar /home/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/home/app.jar"]
