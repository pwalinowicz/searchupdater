FROM openjdk:21
WORKDIR app
COPY build/libs/searchupdater-0.0.1-SNAPSHOT.jar searchupdater.jar
EXPOSE 8090
CMD java -jar searchupdater.jar