package ch.puzzle.kafka.traffic.stream;

import avro.Vbv;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

public class DispatchStream {

    public DispatchStream() {
        super();
    }

    public static void main(String[] args) {

        TrafficStreamRunner runner = new TrafficStreamRunner("traffic-dispatch-stream");

        //dispatch to different topics based on swiss10Class
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, Vbv> vbvStream = builder.stream("traffic", Consumed.with(Serdes.String(), runner.getVbvSerde()));
        vbvStream.to((e, v, r) -> "traffic-" + v.getSwiss10Class(), Produced.with(Serdes.String(), runner.getVbvSerde()));
        runner.run(builder);
    }

}
