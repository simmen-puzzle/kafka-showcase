package ch.puzzle.kafka.traffic.stream;

import static ch.puzzle.Topics.TRAFFIC;
import static ch.puzzle.Topics.TRAFFIC_REPLACED;
import static ch.puzzle.Topics.TRAFFIC_SWISS_10;
import static ch.puzzle.Topics.TRAFFIC_SWISS_10_TABLE;

import avro.Vbv;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueJoiner;

public class JoinStream {

    public JoinStream() {
        super();
    }

    public static void main(String[] args) {

        TrafficStreamRunner runner = new TrafficStreamRunner("traffic-dispatch-stream");


        final GenericAvroSerde gs = new GenericAvroSerde();
        gs.configure(runner.getSerdeConfig(), false);
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> swiss10Keystream = builder.stream(TRAFFIC_SWISS_10, Consumed.with(Serdes.String(),gs))
                .map((k,v)->new KeyValue<>(v.get("abrev").toString(),v.get("full_text").toString()));
        swiss10Keystream.to(TRAFFIC_SWISS_10_TABLE, Produced.with(Serdes.String(),Serdes.String()));

        final GlobalKTable<String,String> swiss10Table = builder.globalTable(TRAFFIC_SWISS_10_TABLE, Consumed.with(Serdes.String(), Serdes.String()));

        final ValueJoiner<Vbv, String, Vbv> vj = (a,b)->{a.setSwiss10Class(b); return a;};
        final KeyValueMapper<String, Vbv, String> kvm = (k,v) -> v.getSwiss10Class();

        final KStream<String, Vbv> traffic_joined = builder.stream(TRAFFIC, Consumed.with(Serdes.String(), runner.getVbvSerde())).join(swiss10Table, kvm, vj);
        traffic_joined.to(TRAFFIC_REPLACED);
        runner.run(builder);
    }
}
