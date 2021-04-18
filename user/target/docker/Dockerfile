# base image
FROM jiridudekusy/docker-jdk8:centos
# volumn
VOLUME /tmp
# from local targer to docker /home/springboot
ADD ./target/user-1.0-SNAPSHOT.jar /home/springboot/user.jar
# server port
EXPOSE 8080
# execute cmd
CMD ["java","-jar","/home/springboot/user.jar"]
# creator
MAINTAINER Mirror tfnick@qq.com
