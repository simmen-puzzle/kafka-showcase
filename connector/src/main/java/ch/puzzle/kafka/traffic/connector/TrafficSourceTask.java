package ch.puzzle.kafka.traffic.connector;

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

    private final TrafficSource source = new TrafficSource();
    private final AvroData avroData = new AvroData(1);
    private Map<String, String> props;

    @Override
    public void start(Map<String, String> map) {
        this.props = map;
        Thread thread = new Thread(source::run);
        thread.start();

    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        return source.getVbvData().stream().map(v -> new SourceRecord(
                        Collections.singletonMap("source", "M3621")
                        , Collections.singletonMap("offset", 0)
                        , "M3621"
                        , Schema.STRING_SCHEMA
                        , "M3621"
                        , avroData.toConnectSchema(Vbv.getClassSchema())
                        , v))
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
