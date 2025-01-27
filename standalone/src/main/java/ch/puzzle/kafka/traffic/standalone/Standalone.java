package ch.puzzle.kafka.traffic.standalone;


import avro.Vbv;
import ch.puzzle.kafka.traffic.source.TrafficSource;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class Standalone {

    public static void main(String[] args) {

        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "http://localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        final TrafficSource source = new TrafficSource("M3621");
        Thread thread = new Thread(source::run);
        thread.start();
        try(Producer<String, Vbv> producer = new KafkaProducer<>(properties)){
        while (thread.isAlive()) {
            source.getVbvData().stream().map(vbv->new ProducerRecord<String, Vbv>("traffic","M3621", vbv)).forEach(producer::send);
            producer.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
        }
        }
    }
}
