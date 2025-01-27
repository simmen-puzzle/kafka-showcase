package ch.puzzle.kafka.traffic.source;

import avro.Vbv;
import ch.puzzle.kafka.connector.traffic.model.VbvData;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficSource {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficSource.class);

    private final Queue<Vbv> vbvQueue = new ArrayDeque<>();
    private final String collectorId;
    private boolean alive = true;

    public TrafficSource(String collectorId) {
        this.collectorId = collectorId;
    }

    public void run() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            URL trafficUrl = new URL("https://vdp.zh.ch/pws/public-service/readOnlineVbvData/"+collectorId+"?sampleOnly=false");
            URLConnection trafficConnection = trafficUrl.openConnection();
            trafficConnection.setRequestProperty("accept", "application/stream+json");
            trafficConnection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(trafficConnection.getInputStream()));
            while (alive) {
                try {
                    String line = reader.readLine();
                    if (!line.contains("alive")) {
                        final VbvData data = objectMapper.readValue(line, VbvData.class);
                        LOG.info("Received VBV data {}", data);
                        final Vbv parsedData = new Vbv(
                                data.getHeadway()
                                , data.getGap()
                                , data.getLength()
                                , data.getSwiss10Class());
                        vbvQueue.add(parsedData);
                    }
                } catch (Exception e) {
                    //skip
                }
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop(){
        alive = false;
    }

    public List<Vbv> getVbvData() {
        List<Vbv> vbvList = new ArrayList<>();
        while (!vbvQueue.isEmpty()) {
            vbvList.add(vbvQueue.poll());
        }
        return vbvList;
    }
}