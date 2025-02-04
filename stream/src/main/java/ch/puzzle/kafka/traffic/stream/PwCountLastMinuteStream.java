package ch.puzzle.kafka.traffic.stream;

import static ch.puzzle.Topics.TRAFFIC_SWISS_10_PW;
import static ch.puzzle.Topics.TRAFFIC_SWISS_10_PW_MINUTE_COUNT;
import static java.lang.String.format;
import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded;
import static org.apache.kafka.streams.kstream.Suppressed.untilWindowCloses;

import java.time.Duration;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;

public class PwCountLastMinuteStream {

    public static void main(String[] args) {

        TrafficStreamRunner runner = new TrafficStreamRunner("pw-count-last-minute");

        //dispatch to different topics based on swiss10Class
        StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> countStream = builder.stream(TRAFFIC_SWISS_10_PW, Consumed.with(Serdes.String(), runner.getVbvSerde()))
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1),Duration.ofSeconds(1)))
                .count()
                .suppress(untilWindowCloses(unbounded()))
                .toStream()
                .map((k, v)-> KeyValue.pair(k.key(),format("counted %d objects in window %s for collector %s",v,k.window(),k.key())));
        countStream.to(TRAFFIC_SWISS_10_PW_MINUTE_COUNT, Produced.with(Serdes.String(), Serdes.String()));
        runner.run(builder);
    }

}
