package ch.puzzle.kafka.traffic.connector;

import static ch.puzzle.Topics.TRAFFIC;
import static ch.puzzle.kafka.traffic.connector.TrafficSourceConnector.COLLECTOR_ID;

import avro.Vbv;
import ch.puzzle.kafka.traffic.source.TrafficSource;
import io.confluent.connect.avro.AvroData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

public class TrafficSourceTask extends SourceTask {

    private TrafficSource source;
    private final AvroData avroData = new AvroData(1);
    private Map<String, String> props;
    private String collectorId;

    @Override
    public void start(Map<String, String> map) {
        this.props = map;
        collectorId = map.get(COLLECTOR_ID);
        this.source = new TrafficSource(collectorId);
        Thread thread = new Thread(source::run);
        thread.start();

    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        return source.getVbvData().stream()
                .map(v-> avroData.toConnectData(Vbv.SCHEMA$, v))
                .map(v -> new SourceRecord(
                        Collections.singletonMap("source", collectorId)
                        , Collections.singletonMap("offset", 0)
                        , TRAFFIC
                        , Schema.STRING_SCHEMA
                        , collectorId
                        , v.schema()
                        , v.value()))
                .toList();
    }

    @Override
    public void stop() {
        source.stop();
    }

    @Override
    public String version() {
        return "";
    }
}
