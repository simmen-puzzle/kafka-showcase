# Kafka Showcase


This project is a showcase with different aspects of Kafka.
At the moment it is collecting data of the following resource
https://opendata.swiss/de/dataset/verkehrszahldaten-motorisierter-individualverkehr-miv-im-kanton-zurich
https://vdp.zh.ch/webjars/swagger-ui/index.html

At the traffic counting station 3621
https://vdp.zh.ch/spa/gis/verkehrsdaten-online/M3621
and at the traffic counting station 1118
https://vdp.zh.ch/spa/gis/verkehrsdaten-online/M1118

The default setup uses a Kafka connect instance which collects the data and publishes
it on the `traffic` topic. 
Additionally two kafka stream application are present; one dispatches the events in order to the vehicle
classification for example the `SWISS10_PW` class to the `traffic-SWISS10_PW` topic;
the second streams counts the number of cars in the `traffic-SWISS10_PW` topic per minute and publishes
it to the `traffic-SWISS10_PW-minute-count` topic.

## Build

CAUTION you have to build the connector plugin using Java 17 otherwise it will not be loaded!

The following commands lets you build the container images
```
 ./gradlew clean build shadowJar
 docker build  -t traffic-connector connector/.
 docker build  -t traffic-standalone standalone/.
 docker build  -t traffic-stream stream/.
```

## Run Cluster
now you  launch the  kafka related containers with the following command
```
docker compose up -d  traffic-connector zookeeper kafka schema-registry ksqldb-server kafka-ui
```
given you have docker and docker compose installed on your machine

this will run a broker, schema registiry, connect and a ksqldb server and also a kafka ui which you can access at http://localhost:8080

## Initialisation Topis and connector
Now create the topics by running the following class
```
ch.puzzle.TopicCreator
```
Once the plugins in kafka connect are loaded you can call
```
curl http://localhost:8083/connector-plugins
```
and should see in the response the following class
```
ch.puzzle.kafka.traffic.connector.TrafficSourceConnector
```
At that point you can start the connectors with the following statements
```
curl -X POST -H "Content-Type:application/json" -d @connector-M3621.json http://localhost:8083/connectors
curl -X POST -H "Content-Type:application/json" -d @connector-M1118.json http://localhost:8083/connectors
curl -X POST -H "Content-Type:application/json" -d @connector-jdbc.json http://localhost:8083/connectors
```
If you have the kafka binaries on your system you should be able to connect to all the topics
including `traffic` with
```
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic traffic
```
and check if the connector is producing records.
Alternatively you can use the kafka ui 

## Streams
Now you can start the remaining containers
```
docker compose up -d  
```

again if you have the kafka binaries on your system you should be able to connect to all the topics 
including `traffic-SWISS10_PW-minute-count` with
```
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic traffic-SWISS10_PW-minute-count
```
and after some time see something like this
```
counted 1 objects in window Window{startMs=1737981300000, endMs=1737981360000} for collector M1118
counted 12 objects in window Window{startMs=1737981300000, endMs=1737981360000} for collector M3621
```
or simply use the kafka ui.

## KSQLDB
If you want to run the KSQLD stuff you need at least one of the traffic connectors running
and the `traffic-dispatch` container otherwise it will fail
afterwards you can use `ksql-migrations` with the following commands
```
docker exec -it ksqldb-server ksql-migrations -c /share/ksql-migrations/traffic/ksql-migrations.properties initialize-metadata
docker exec -it ksqldb-server ksql-migrations -c /share/ksql-migrations/traffic/ksql-migrations.properties apply -a
```
Now you should see the `traffic-SWISS10_PW_avg` topic with the average values


## Standalone

If you want to use the standalone container instead of the kafka connect connector, 
adapt the `docker.compose.yml` by commenting aout the connector definition and 
commenting in the standalone definition.


## Remarks and TODOS
Being a simple showcase some production hardening like fault tolerance is missing in the code.
Some aspects will be implemented next (suggestions are welcome)

- [x] finish connector
- [x] make connector configurable
- [x] if topic for stream is not present do not shut down
- [x] more complex stream including a join with a KTable
- [x] some Ksqldb examples
- [ ] Testing, Testing Testing