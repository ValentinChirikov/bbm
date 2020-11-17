package bbm.machine;

import org.teavm.flavour.json.JsonPersistable;

/**
 * Brew machine commands.
 */
@JsonPersistable
public enum MachineCommand {
    /**
     * Start brewery.
     */
    RUN,
    /**
     * Switch to next stage in recipe.
     */
    SWITCH_TO_NEXT_STAGE,
    /**
     * Stop machine.
     */
    STOP,
    /**
     * Pause machine.
     */
    PAUSE
}
