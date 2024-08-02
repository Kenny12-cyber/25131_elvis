#Use an official Tomcat runtime as a parent image
FROM tomcat:8.5-jdk11 

WORKDIR /usr/local/tomcat

RUN rm -rf webapps/*

#Copy the WAR file to the webapps directory of Tomcat
COPY build/best_programming.war webapps/best_programming.war

#Expose port 8080
EXPOSE 8080

#Run Tomcat
CMD ["catalina.sh", "run"]
#docker build-t my-test .
#docker images
#docker run -d -p 6090:8080 --name test-run my-test