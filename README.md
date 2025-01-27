# Kafka Showcase


This project is a showcase with different aspects of Kafka.
At the moment it is collecting data of the following resource
https://opendata.swiss/de/dataset/verkehrszahldaten-motorisierter-individualverkehr-miv-im-kanton-zurich
https://vdp.zh.ch/webjars/swagger-ui/index.html

At the traffic counting station 3621
https://vdp.zh.ch/spa/gis/verkehrsdaten-online/M3621

The default setup uses a Kafka connect instance which collects the data and publishes
it on the `traffic` topic. 
Additionally two kafka stream application are present; one dispatches the events in order to the vehicle
classification for example the `SWISS10_PW` class to the `traffic-SWISS10_PW` topic;
the second streams counts the number of cars in the `traffic-SWISS10_PW` topic per minute and publishes
it to the `traffic-SWISS10_PW-minute-count` topic.

## Build & Run

CAUTION you have to build the connector plugin using Java 17 otherwise it will not be loaded!

The following commands lets you build the container images
```
 ./gradlew clean build shadowJar
 docker build  -t traffic-connector connector/.
 docker build  -t traffic-standalone standalone/.
 docker build  -t traffic-stream stream/.
```

now you  launch the  containers with the following command
```
docker compose up -d 
```
given you have docker and docker compose installed on your machine

Once the plugins in kafka connect are loaded you can call
```
curl http://localhost:8083/connector-plugins
```
and should see in the response the following class
```
ch.puzzle.kafka.traffic.connector.TrafficSourceConnector
```
now you can start the connector with the following statement
```
curl -X POST -H "Content-Type:application/json" -d @connector-M3621.json http://localhost:8083/connectors
curl -X POST -H "Content-Type:application/json" -d @connector-M1118.json http://localhost:8083/connectors
```
zf you have the kafka binaries on your system you should be able to connect to all the topics
including `traffic` with
```
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic traffic
```
and check if the connector is producing record

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
- [ ] more complex stream including a join with a KTable
- [ ] some Ksqldb examples
- [ ] Testing, Testing Testing