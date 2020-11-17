package bbm.sensor;

import bbm.machine.BrewMachine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Class provide incoming sensor data handling with filtering.
 *
 * @see OneEuroFilter
 */
@Component
public class SensorDataHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SensorDataHandler.class);

    /**
     * @see OneEuroFilter#freq
     */
    private static final int FREQ = 1;
    /**
     * @see OneEuroFilter#mincutoff
     */
    private static final float MINCUTOFF = 0.005f;
    /**
     * @see OneEuroFilter#beta_
     */
    private static final float BETA = 0.0001f;
    /**
     * @see OneEuroFilter#dcutoff
     */
    private static final float DCUTOFF = 0.02f;
    /**
     * De/Serializer.
     */
    private final ObjectMapper objectMapper;
    /**
     * Filter.
     */
    private OneEuroFilter filter;
    /**
     * Machine instance.
     */
    private BrewMachine brewMachine;

    /**
     * Init handler with default ObjectMapper and OneEuroFilter.
     * Filter parameters must set set with caution, they can cause Exception when set incorrect.
     * @param pBrewMachine Brew machine
     * @throws Exception Filter instantiation exception
     */
    @Autowired
    public SensorDataHandler(final BrewMachine pBrewMachine) throws Exception {
        this.brewMachine = pBrewMachine;
        this.objectMapper = new ObjectMapper();
        this.filter = new OneEuroFilter(FREQ, MINCUTOFF, BETA, DCUTOFF);
    }

    private static double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        SensorData sensorData = objectMapper.readValue(msg.getPayload(), SensorData.class);

        try {
            Float temp = sensorData.getTemp();
            LOG.debug("Source temp = {}", temp);
            if (filter != null) {
                temp = (float) filter.filter(temp.doubleValue());
            }
            LOG.debug("Filtered temp = {}", temp);
            brewMachine.setWortTemp((float) roundAvoid(temp.doubleValue(), 1));
            brewMachine.setSensorLastSeen(brewMachine.getTime());
            LOG.debug("Floor temp = {}", brewMachine.getWortTemp());
        } catch (Exception e) {
            LOG.error("Failed to set sensor data", e);
        }

        LOG.info("received sensor readings : {}", sensorData);
    }
}
