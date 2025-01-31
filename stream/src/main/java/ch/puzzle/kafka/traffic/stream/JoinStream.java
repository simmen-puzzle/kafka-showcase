package ch.puzzle.kafka.traffic.stream;

import avro.Vbv;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
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
        final KStream<String, String> swiss10Keystream = builder.stream("traffic.swiss_10", Consumed.with(Serdes.String(),gs))
                .map((k,v)->new KeyValue<>(v.get("abrev").toString(),v.get("full_text").toString()));
        swiss10Keystream.to("traffic-swiss10-table", Produced.with(Serdes.String(),Serdes.String()));

        final GlobalKTable<String,String> swiss10Table = builder.globalTable("traffic-swiss10-table", Consumed.with(Serdes.String(), Serdes.String()));

        final ValueJoiner<Vbv, String, Vbv> vj = (a,b)->{a.setSwiss10Class(b); return a;};
        final KeyValueMapper<String, Vbv, String> kvm = (k,v) -> v.getSwiss10Class();

        final KStream<String, Vbv> traffic_joined = builder.stream("traffic", Consumed.with(Serdes.String(), runner.getVbvSerde())).join(swiss10Table, kvm, vj);
        traffic_joined.to("traffic_replaced");
        runner.run(builder);
    }
}
