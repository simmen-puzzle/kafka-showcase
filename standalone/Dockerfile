FROM eclipse-temurin:17-alpine

COPY build/libs/standalone-1.0-SNAPSHOT-all.jar /home/standalone.jar
CMD ["java","-cp","/home/standalone.jar","ch.puzzle.kafka.traffic.standalone.Standalone"]