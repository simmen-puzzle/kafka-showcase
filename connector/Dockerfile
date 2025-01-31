FROM confluentinc/cp-server-connect-base:7.8.0

COPY --chmod=777 build/libs/connector-1.0-SNAPSHOT-all.jar  /usr/share/java/traffic/traffic.jar
COPY --chmod=777 swiss10.db /home/appuser/swiss10.db

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest



