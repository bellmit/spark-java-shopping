FROM java:8

MAINTAINER Mohsen yazdani

ADD build/libs/shopping-1.0-all.jar application.jar

EXPOSE 80

CMD java -jar application.jar 80