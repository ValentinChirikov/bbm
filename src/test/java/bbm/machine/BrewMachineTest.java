package bbm.machine;

import bbm.comm.MessageTransceiver;
import bbm.recipe.Recipe;
import bbm.recipe.Stage;
import bbm.recipe.StageState;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class BrewMachineTest {

    private BrewMachine brewMachine;

    @Before
    public void init() {
        brewMachine = new BrewMachine(new MessageTransceiver() {
            @Override
            public void sendMessage(String message) throws IOException {

            }

            @Override
            public void receiveMessage(Consumer<String> stringConsumer) {

            }
        });
    }

    @Test
    public void brew() {
        LocalDateTime now;
        //Create Recipe
        Recipe recipe = new Recipe("Pilsner Czech");
        recipe.setStages(new ArrayList<>(3));
        //Set recipe stages
        Stage stage1 = new Stage("1", 50.5f, 600);
        recipe.getStages().add(stage1);
        Stage stage2 = new Stage("2", 61.5f, 1800);
        recipe.getStages().add(stage2);
        Stage stage3 = new Stage("3", 70.0f, 1800);
        recipe.getStages().add(stage3);
        //Set recipe
        brewMachine.setRecipe(recipe);
        //Set temp
        brewMachine.setWortTemp(22.6f);

        //Run machine
        brewMachine.run();

        //Heating up
        brewMachine.poll();
        assertEquals(stage1, brewMachine.getStage());
        assertEquals(MachineState.HEATING, brewMachine.getState());
        assertEquals(StageState.AWAITING, brewMachine.getStageState());
        //Heated up - wait for command to start stage
        brewMachine.setWortTemp(50.5f);
        brewMachine.poll();
        assertEquals(MachineState.AWAITING, brewMachine.getState());
        assertEquals(StageState.AWAITING, brewMachine.getStageState());
        //Send command to start stage
        now = LocalDateTime.now();
        brewMachine.setTime(now);
        brewMachine.run();
        brewMachine.poll();
        assertEquals(MachineState.BREWING, brewMachine.getState());
        assertEquals(StageState.RUNNING, brewMachine.getStageState());
        //Stage done, we waited for specified time, stop and wait for command to go to next stage
        brewMachine.setTime(now.plusSeconds(601));
        brewMachine.poll();
        assertEquals(MachineState.AWAITING, brewMachine.getState());
        assertEquals(StageState.DONE, brewMachine.getStageState());

        //Second stage
        brewMachine.run();
        assertEquals(stage2, brewMachine.getStage());
        //Heating up
        brewMachine.poll();
        assertEquals(MachineState.HEATING, brewMachine.getState());
        assertEquals(StageState.AWAITING, brewMachine.getStageState());
        //Heated up - wait for command to start stage
        brewMachine.setWortTemp(61.5f);
        brewMachine.poll();
        assertEquals(MachineState.AWAITING, brewMachine.getState());
        assertEquals(StageState.AWAITING, brewMachine.getStageState());
        //Send command to start stage
        now = LocalDateTime.now();
        brewMachine.setTime(now);
        brewMachine.run();
        brewMachine.poll();
        assertEquals(MachineState.BREWING, brewMachine.getState());
        assertEquals(StageState.RUNNING, brewMachine.getStageState());
        //Stage done, we waited for specified time, stop and wait for command to go to next stage
        brewMachine.setTime(now.plusSeconds(1801));
        brewMachine.poll();
        assertEquals(MachineState.AWAITING, brewMachine.getState());
        assertEquals(StageState.DONE, brewMachine.getStageState());

        //Third and final stage
        brewMachine.run();
        assertEquals(stage3, brewMachine.getStage());
        //Heating up
        brewMachine.poll();
        assertEquals(MachineState.HEATING, brewMachine.getState());
        assertEquals(StageState.AWAITING, brewMachine.getStageState());
        //Heated up - wait for command to start stage
        brewMachine.setWortTemp(70.0f);
        brewMachine.poll();
        assertEquals(MachineState.AWAITING, brewMachine.getState());
        assertEquals(StageState.AWAITING, brewMachine.getStageState());
        //Send command to start stage
        now = LocalDateTime.now();
        brewMachine.setTime(now);
        brewMachine.run();
        brewMachine.poll();
        assertEquals(MachineState.BREWING, brewMachine.getState());
        assertEquals(StageState.RUNNING, brewMachine.getStageState());
        //Stage done, we waited for specified time, stop and wait for command to go to next stage
        brewMachine.setTime(now.plusSeconds(1801));
        brewMachine.poll();
        assertEquals(MachineState.DONE, brewMachine.getState());
        assertEquals(StageState.DONE, brewMachine.getStageState());

    }

}