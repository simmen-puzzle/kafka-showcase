package ch.puzzle.kafka.traffic.stream;

import avro.Vbv;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficStreamRunner {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficStreamRunner.class);

    private final Properties properties;

    private final Serde<Vbv> vbvSerde;

    public TrafficStreamRunner(String applicationId) {
        properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, StringSerde.class);
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        properties.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",
                "http://localhost:8081");
        vbvSerde = new SpecificAvroSerde<>();
        vbvSerde.configure(serdeConfig, false);
    }


    public Serde<Vbv> getVbvSerde() {
        return vbvSerde;
    }


    public void run(StreamsBuilder builder) {

        final CountDownLatch shutdownLatch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(shutdownLatch::countDown));
        try {
            while (shutdownLatch.getCount() > 0) {
                try (KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), properties)) {
                    kafkaStreams.start();
                    var state = kafkaStreams.state();
                    while (!state.hasCompletedShutdown()
                            && !shutdownLatch.await(10, TimeUnit.SECONDS)) {
                        state = kafkaStreams.state();
                    }
                    kafkaStreams.close(Duration.ofSeconds(2));
                }
            }
        } catch (Throwable e) {
            LOG.error("Error while starting streams", e);
            System.exit(1);
        }

    }
}
