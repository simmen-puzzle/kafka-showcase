CREATE TABLE averages
    WITH (KAFKA_TOPIC = 'traffic-SWISS10_PW_avg',
        PARTITIONS = 1,
        VALUE_FORMAT = 'AVRO')
AS SELECT
       collector,
       AVG(headway) AS average_headway,
       AVG(gap) AS average_gap,
       AVG(length) AS average_length
   FROM traffic_pw
   GROUP BY collector
       EMIT CHANGES;