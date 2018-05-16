FROM java:8

MAINTAINER Mohsen yazdani

ADD build/libs/shopping-1.0-all.jar application.jar

RUN mkdir -p /uploads
    && mkdir -p /pages
    && chmod -R 777 /uploads
    && chmod -R 777 /pages

EXPOSE 80

CMD java -jar application.jar 80