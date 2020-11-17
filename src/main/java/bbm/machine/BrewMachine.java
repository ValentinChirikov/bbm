package bbm.machine;

import bbm.comm.MessageTransceiver;
import bbm.recipe.Recipe;
import bbm.recipe.Stage;
import bbm.recipe.StageState;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Brewery.
 *
 * @author Valentin
 */
@Component
public class BrewMachine {

    public static final String ALL_DONE = "All done !";
    public static final String HEATING = "Heating...";
    public static final String AWAITING_FOR_COMMAND = "Awaiting for command...";
    public static final String STAGE_DONE_AWAITING_FOR_COMMAND = "Stage done ! " + AWAITING_FOR_COMMAND;
    public static final String STAGE_START_CONDITIONS_ARE_MET_AWAITING_FOR_COMMAND = "Stage start conditions are met. " + AWAITING_FOR_COMMAND;
    public static final String STAGE_STARTED = "Stage started...";
    public static final String SWITCHED_TO_NEXT_STAGE = "Switched to next stage...";
    public static final String NO_SENSOR_TEMPERATURE_REPORTED_CHECK_SENSOR = "No sensor Temperature reported! Check sensor!";
    public static final String CAN_T_START_BREWING_WITHOUT_RECIPE = "Can't start brewing without recipe !";
    public static final int WORT_SENSOR_MAX_LAG = 20;
    public static final Logger LOG = LoggerFactory.getLogger(BrewMachine.class);

    /**
     * Wort temperature exchange Lock.
     */
    private final Object wortTempLock = new Object();
    /**
     * Wort temperature.
     */
    private Float wortTemp;
    /**
     * Machine time.
     * Container updates time with specified period
     *
     * @link bbm.web.controllers.WebControllersConfig
     */
    @Getter
    @Setter(onMethod = @__( {@Synchronized}))
    private LocalDateTime time;
    /**
     * Last sensor seen timestamp.
     */
    @Getter
    @Setter(onMethod = @__( {@Synchronized}))
    private LocalDateTime sensorLastSeen;
    /**
     * Recipe.
     */
    @Getter
    private Recipe recipe;
    /**
     * Machine state.
     */
    @Getter
    @Setter
    private MachineState state;
    /**
     * Current recipe stage.
     */
    @Getter
    private Stage stage;
    /**
     * Current stage state.
     */
    @Getter
    @Setter(onMethod = @__( {@Synchronized}))
    private StageState stageState;
    /**
     * Recipe stage start time.
     */
    @Getter
    @Setter(onMethod = @__( {@Synchronized}))
    private LocalDateTime stageStartTime;
    /**
     * Recipe stage start time.
     */
    @Getter
    @Setter(onMethod = @__( {@Synchronized}))
    private LocalDateTime stagePlannedEndTime;
    /**
     * Recipe stage start temperature.
     */
    @Getter
    @Setter(onMethod = @__( {@Synchronized}))
    private Float stageStartTemp;
    /**
     * Recipe stage iterator.
     */
    private ListIterator<Stage> stageIterator;
    /**
     * Message transceiver.
     */
    private MessageTransceiver messageTransceiver;

    {
        // Default recipe for development purposes
        Recipe r = new Recipe("Пилзнер в чешском стиле");
        r.setStages(new ArrayList<>(3));
        r.getStages().add(new Stage("1", 50.5f, 600));
        r.getStages().add(new Stage("2", 61.5f, 1800));
        r.getStages().add(new Stage("3", 70.0f, 1800));
        r.getStages().add(new Stage("Hop 1", 90.0f, 1800));
        r.getStages().add(new Stage("Hop 2", 90.0f, 1500));
        r.getStages().add(new Stage("Hop 3", 90.0f, 300));
        setRecipe(r);
    }

    /**
     * Init Brew Machine.
     *
     * @param pMessageTransceiver Message transceiver
     */
    @Autowired
    public BrewMachine(final MessageTransceiver pMessageTransceiver) {
        this.messageTransceiver = pMessageTransceiver;
        setState(MachineState.IDLE);
        setWortTemp(0.0f);
        setTime(LocalDateTime.now());
    }

    /**
     * @return wort sensor liveness.
     */
    public boolean isSensorActive() {
        if (sensorLastSeen == null) {
            return false;
        }
        if (sensorLastSeen.plusSeconds(WORT_SENSOR_MAX_LAG).isBefore(getTime())) {
            return false;
        }
        return true;
    }

    /**
     * Run Machine.
     *
     * @return the boolean
     */
    public boolean run() {
        return execute(MachineCommand.RUN);
    }

    /**
     * Next stage boolean.
     *
     * @return the boolean
     */
    public boolean nextStage() {
        return execute(MachineCommand.SWITCH_TO_NEXT_STAGE);
    }

    /**
     * Check wort temperature - fail if null.
     */
    private void check() {
        Assert.notNull(getWortTemp(), NO_SENSOR_TEMPERATURE_REPORTED_CHECK_SENSOR);
    }

    /**
     * Check recipe - fail if null.
     */
    private void checkRecipe() {
        Assert.notNull(recipe, CAN_T_START_BREWING_WITHOUT_RECIPE);
    }

    /**
     * Execute commands.
     *
     * @param commands the commands
     * @return the boolean
     */
    public boolean execute(MachineCommand... commands) {
        check();
        checkRecipe();

        for (MachineCommand command : commands) {
            switch (command) {
                case SWITCH_TO_NEXT_STAGE:
                    if (stageIterator.hasNext()) {
                        setStage(stageIterator.next());
                        alert(SWITCHED_TO_NEXT_STAGE);
                    } else {
                        setStage(null);
                        setState(MachineState.DONE);
                        alert(ALL_DONE);
                    }
                    break;
                case RUN:
                    if (getStage() == null) {
                        execute(MachineCommand.SWITCH_TO_NEXT_STAGE);
                    } else if (getStageState() == StageState.DONE && getState() != MachineState.DONE) {
                        execute(MachineCommand.SWITCH_TO_NEXT_STAGE);
                    }
                    if (getStage().getTemp() > getWortTemp()) {
                        setState(MachineState.HEATING);
                        setStageState(StageState.AWAITING);
                        setStageStartTemp(getWortTemp());
                        alert(HEATING);
                    } else {
                        setState(MachineState.BREWING);
                        setStageState(StageState.RUNNING);
                        LocalDateTime now = getTime();
                        setStageStartTime(now);
                        setStageStartTemp(getWortTemp());
                        setStagePlannedEndTime(now.plusSeconds(getStage().getDuration()));
                        alert(STAGE_STARTED);
                    }
                    break;
                default:
                    break;
            }
        }

        return true;
    }

    /**
     * Send alert to clients.
     *
     * @param message alert message
     */
    private void alert(String message) {
        try {
            messageTransceiver.sendMessage(message);
        } catch (IOException e) {
            LOG.error("Failed to send alert message to clients, check connectivity", e);
        }
    }

    /**
     * Machine cycle - polled by {@link bbm.schedule.Tasks} every 1 second.
     * start
     * heat up
     * wait
     * done
     */
    public void poll() {
        check();

        if (getStageState() == StageState.AWAITING) {
            if (getWortTemp() < getStage().getTemp()) {
                setState(MachineState.HEATING);
            } else if (getState() != MachineState.AWAITING) {
                setState(MachineState.AWAITING);
                alert(STAGE_START_CONDITIONS_ARE_MET_AWAITING_FOR_COMMAND);
            }
            return;
        }

        if (getStageState() == StageState.RUNNING) {
            if (getTime().isAfter(getStageStartTime().plusSeconds(getStage().getDuration()))) {
                setStageState(StageState.DONE);
                if (!stageIterator.hasNext()) {
                    setState(MachineState.DONE);
                    alert(ALL_DONE);
                } else {
                    setState(MachineState.AWAITING);
                    alert(STAGE_DONE_AWAITING_FOR_COMMAND);
                }
            }
        }
    }

    /**
     * Sets brewing stage.
     * Initial stage state {@link StageState} AWAITING, stageStartTime = null.
     *
     * @param pStage recipe stage
     */
    private void setStage(Stage pStage) {
        this.stage = pStage;
        setStageState(StageState.AWAITING);
        setStageStartTime(null);
    }

    /**
     * @param pRecipe brewing recipe
     */
    public synchronized void setRecipe(Recipe pRecipe) {
        this.recipe = pRecipe;
        this.stageIterator = pRecipe.getStages().listIterator();
    }

    /**
     * Gets wort sensor temperature.
     *
     * @return temp
     */
    public Float getWortTemp() {
        synchronized (wortTempLock) {
            return wortTemp;
        }
    }

    /**
     * Sets current wort sensor temperature.
     *
     * @param pCurrentTemp wort sensor temperature
     */
    public void setWortTemp(Float pCurrentTemp) {
        synchronized (wortTempLock) {
            this.wortTemp = pCurrentTemp;
        }
    }

}
