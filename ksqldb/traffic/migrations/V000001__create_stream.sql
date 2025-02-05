CREATE STREAM traffic_pw (collector VARCHAR KEY, headway INT, gap INT, length INT)
  WITH (KAFKA_TOPIC = 'traffic-SWISS10_PW',
        VALUE_FORMAT = 'AVRO');