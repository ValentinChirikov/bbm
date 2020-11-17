package bbm.schedule;

import bbm.machine.BrewMachine;
import bbm.machine.MachineState;
import bbm.web.controllers.WebControllersConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Brewery status polling.
 *
 * @author Valentin Chirikov
 */
@Component
public final class Tasks {

    private static final Logger LOG = LoggerFactory.getLogger(Tasks.class);
    /**
     * Period in ms to save info to database.
     */
    public static final int PERSIST_RATE = 10000;
    /**
     * Period in ms to check system status.
     */
    public static final int POLLING_RATE = 1000;
    /**
     * @see bbm.machine.BrewMachine
     */
    @Autowired
    private BrewMachine brewMachine;
    /**
     * @see org.springframework.jdbc.core.JdbcTemplate
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Update current time.
     */
    @Scheduled(fixedRate = POLLING_RATE)
    public void setTime() {
        LocalDateTime now = LocalDateTime.now();
        LOG.debug("The time is {}", now.format(WebControllersConfig.TIME_FORMAT));
        brewMachine.setTime(now);
    }

    /**
     * Poll system status.
     */
    @Scheduled(fixedRate = POLLING_RATE)
    public void poll() {
        LOG.debug("Poll...");
        brewMachine.poll();
    }

    /**
     * Persist sensor reading to database.
     */
    @Scheduled(fixedRate = PERSIST_RATE)
    public void saveDataToDB() {
        LOG.debug("Saving data to DB...");
        if (brewMachine.getState() != null && !brewMachine.getState().equals(MachineState.IDLE)) {
            jdbcTemplate.update("insert into SENSOR_READINGS (temp, time) " + "values(?, ?)",
                    brewMachine.getWortTemp(), brewMachine.getTime());

        }
    }

}
