package bbm.recipe;

import org.teavm.flavour.json.JsonPersistable;

/**
 * Recipe stage states.
 * @author Valentin
 */
@JsonPersistable
public enum StageState {
    /**
     * Stage awaits for trigger.
     */
    AWAITING,
    /**
     * Stage running.
     */
    RUNNING,
    /**
     * Stage completed.
     */
    DONE
}
