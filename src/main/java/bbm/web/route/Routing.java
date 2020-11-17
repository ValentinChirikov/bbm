package bbm.web.route;

import org.teavm.flavour.routing.Path;
import org.teavm.flavour.routing.PathSet;
import org.teavm.flavour.routing.Route;

/**
 * URL's for single page routing.
 */
@PathSet
public interface Routing extends Route {
    /**
     * Sensor WebSocket endpoint.
     */
    String SENSOR_WS_ENDPOINT = "/report";
    /**
     * Web Client WebSocket endpoint.
     */
    String CLIENT_WS_ENDPOINT = "/client";

    /**
     * Beer brew machine URL.
     */
    @Path("/bbm")
    void bbm();
}
