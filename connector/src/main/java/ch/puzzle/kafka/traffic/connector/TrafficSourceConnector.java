package ch.puzzle.kafka.traffic.connector;

import java.util.List;
import java.util.Map;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

public class TrafficSourceConnector  extends SourceConnector {


    public static final String COLLECTOR_ID = "collectorId";
    private Map<String, String> props;

    @Override
    public void start(Map<String, String> map) {
        this.props = map;
    }

    @Override
    public Class<? extends Task> taskClass() {
        return TrafficSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int i) {
        return List.of(props);
    }

    @Override
    public void stop() {
        // nothing to do for the moment
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef().define(COLLECTOR_ID,
                ConfigDef.Type.STRING,
                ConfigDef.Importance.HIGH,
                "Collector ID of the vehicle counter to import objects from");
    }

    @Override
    public String version() {
        return "0.1";
    }
}
