package bbm.web.models;

import bbm.machine.MachineState;
import bbm.recipe.Recipe;
import bbm.recipe.Stage;
import bbm.recipe.StageState;
import lombok.Data;
import org.teavm.flavour.json.JsonPersistable;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Brew machine DTO.
 */
@JsonPersistable
@Data
public class BrewMachineDTO implements Serializable {
    /**
     * Wort temperature.
     */
    private Float wortTemp;
    /**
     * Machine time.
     **/
    private String time;
    /**
     * Last sensor seen timestamp.
     **/
    private String sensorLastSeen;
    /**
     * Is sensor active.
     */
    private boolean sensorActive;
    /**
     * Recipe.
     */
    private Recipe recipe;
    /**
     * Machine state.
     */
    private MachineState state;
    /**
     * Current recipe stage.
     */
    private Stage stage;
    /**
     * Current stage state.
     */
    private StageState stageState;
    /**
     * Recipe stage start time.
     */
    private String stageStartTime;
    /**
     * Recipe stage start temperature.
     */
    private Float stageStartTemp;
    /**
     * Recipe stage start time.
     */
    private String stagePlannedEndTime;
}
