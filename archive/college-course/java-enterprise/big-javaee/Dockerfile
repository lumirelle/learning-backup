FROM tomcat:10-jdk17-temurin
LABEL authors="Oddy"

ADD ./target/*.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]
