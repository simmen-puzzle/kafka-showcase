package ch.puzzle;

import static ch.puzzle.Topics.TRAFFIC;
import static ch.puzzle.Topics.TRAFFIC_REPLACED;
import static ch.puzzle.Topics.TRAFFIC_SWISS_10;
import static ch.puzzle.Topics.TRAFFIC_SWISS_10_PW;
import static ch.puzzle.Topics.TRAFFIC_SWISS_10_PW_MINUTE_COUNT;
import static ch.puzzle.Topics.TRAFFIC_SWISS_10_TABLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;

public class TopicCreator {

    public static void main(String[] args) {

        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        try(AdminClient client = AdminClient.create(props)) {
            List<NewTopic> topics = new ArrayList<>();
            topics.add(new NewTopic(TRAFFIC, 1, (short) 1));
            topics.add(new NewTopic(TRAFFIC_SWISS_10, 1, (short) 1));
            topics.add(new NewTopic(TRAFFIC_SWISS_10_TABLE, 1, (short) 1));
            topics.add(new NewTopic(TRAFFIC_REPLACED, 1, (short) 1));
            topics.add(new NewTopic(TRAFFIC_SWISS_10_PW, 1, (short) 1));
            topics.add(new NewTopic(TRAFFIC_SWISS_10_PW_MINUTE_COUNT, 1, (short) 1));
            client.createTopics(topics);
        }

    }

}
