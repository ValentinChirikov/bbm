package bbm.machine;

import org.teavm.flavour.json.JsonPersistable;

/**
 * Brewery status.
 *
 * @author Valentin
 */
@JsonPersistable
public enum MachineState {
    /**
     * Awaiting to start brewing. Default machine state.
     */
    IDLE,
    /**
     * Heating.
     */
    HEATING,
    /**
     * Brewing.
     */
    BREWING,
    /**
     * Recipe competed.
     */
    DONE,
    /**
     * Awaiting for command.
     */
    AWAITING
}
