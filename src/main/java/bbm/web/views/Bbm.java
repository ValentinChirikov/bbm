package bbm.web.views;

import bbm.machine.BrewMachine;
import bbm.machine.MachineState;
import bbm.recipe.StageState;
import bbm.schedule.Tasks;
import bbm.web.controllers.REST;
import bbm.web.models.BrewMachineDTO;
import bbm.web.route.Routing;
import bbm.web.views.components.HTMLInputFileElement;
import bbm.web.views.components.JSBlob;
import bbm.web.views.components.JSFile;
import bbm.web.views.components.JSFileReader;
import lombok.Getter;
import lombok.Setter;
import org.teavm.flavour.json.JSON;
import org.teavm.flavour.json.tree.Node;
import org.teavm.flavour.templates.BindTemplate;
import org.teavm.flavour.templates.Templates;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.*;
import org.teavm.jso.websocket.WebSocket;

/**
 * Beer brew machine view.
 */
@BindTemplate("templates/bbm.html")
class Bbm {
    public static final int TICKS_TO_UPDATE_PROGRESS = 10;
    @Getter
    @Setter
    boolean chartInitialized;
    /**
     * Message to client.
     */
    @Getter
    @Setter
    private String message;
    /**
     * System status.
     */
    @Getter
    @Setter
    private String status;
    /**
     * Brew machine.
     */
    @Getter
    @Setter
    private BrewMachineDTO machine;
    /**
     * Bell.
     */
    private HTMLAudioElement bell;
    /**
     * Current doc.
     */
    private HTMLDocument doc;
    /**
     * Timer ticker.
     */
    @Getter
    @Setter
    private int ticks;

    /**
     * Init view.
     */
    public Bbm() {
        message = "";
        status = "";
        machine = new BrewMachineDTO();

        String host = "192.168.1.33:9090";

        doc = Window.current().getDocument();
        bell = (HTMLAudioElement) doc.getElementById("audioBell");

        WebSocket sock = WebSocket.create("ws://" + host + Routing.CLIENT_WS_ENDPOINT);
        sock.onOpen(event -> {
            setStatus("WS Connected");
            Templates.update();
            Utils.log("WS Connected");
        });
        sock.onClose(event -> {
            setStatus("WS Disconnected");
            Templates.update();
            Utils.log("WS Disconnected");
        });
        sock.onMessage(event -> {
            setMessage(event.getDataAsString());
            if (event.getDataAsString().contains(BrewMachine.AWAITING_FOR_COMMAND)) {
                Utils.addClass(doc.getElementById("message"), "warning");
            } else {
                Utils.removeClass(doc.getElementById("message"), "warning");
            }
            bell.play();
        });

        Utils.setInterval(this::updateMachine, Tasks.POLLING_RATE);
    }

    @JSBody(script = "initChart();")
    public static native void initChart();

    @JSBody(params = {"time", "temp"}, script = "addChartData(time, temp);")
    public static native void addChartData(String time, float temp);

    /**
     * Update machine state.
     */
    public void updateMachine() {
        XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.open("get", REST.MACHINE_GET);
        xhr.setOnReadyStateChange(() -> {
            if (xhr.getReadyState() != XMLHttpRequest.DONE) {
                return;
            }

            machine = JSON.deserialize(Node.parse(xhr.getResponseText()), BrewMachineDTO.class);

            if (ticks >= TICKS_TO_UPDATE_PROGRESS) {
                ticks = 0;
                updateProgress();
                addChartData(machine.getTime(), machine.getWortTemp());
            } else {
                ticks++;
            }

            Templates.update();
        });
        xhr.send();

        if (!chartInitialized) {
            chartInitialized = true;
            initChart();
        }
    }

    /**
     * Update stage progressbar.
     */
    public void updateProgress() {
        if (machine.getState() == MachineState.HEATING && machine.getStageState() == StageState.AWAITING) {
            int percent = Math.round(machine.getWortTemp() * 100 / machine.getStage().getTemp());
            moveStageProgressBar(percent);
            return;
        }

        if (machine.getState() == MachineState.BREWING && machine.getStageState() == StageState.RUNNING) {
            int timeLeft = Utils.timeDiff(machine.getTime(), machine.getStageStartTime());

            int percent = timeLeft
                    * 100
                    / machine.getStage().getDuration();

            updateTimeLeft(machine.getStage().getDuration() - timeLeft);

            moveStageProgressBar(percent);
            return;
        }

        moveStageProgressBar(0);
    }


    /**
     * Move stage progress bar.
     *
     * @param percent Percent
     */
    private void moveStageProgressBar(int percent) {
        HTMLElement elem = doc.getElementById("stageProgressBar");

        elem.getStyle().setProperty("width", percent + "%");
        elem.setInnerHTML(percent + "%");
    }

    /**
     * Update time left.
     *
     * @param timeLeft Time left for stage
     */
    private void updateTimeLeft(int timeLeft) {
        HTMLElement elem = doc.getElementById("timeLeft");

        elem.setInnerHTML(""+timeLeft + "sec");
    }

    /**
     * Start machine.
     */
    public void runMachine() {
        XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.open("post", REST.MACHINE_EXECUTE_RUN);
        xhr.setOnReadyStateChange(() -> {
            if (xhr.getReadyState() != XMLHttpRequest.DONE) {
                return;
            }
            setStatus(xhr.getStatusText());
            setMessage(xhr.getResponseText());

            ((HTMLButtonElement) doc.getElementById("loadFile")).setHidden(true);
            ((HTMLButtonElement) doc.getElementById("saveFile")).setHidden(true);
            ((HTMLInputFileElement) doc.getElementById("recipeFile")).setHidden(true);

            Templates.update();
        });
        xhr.send();
    }

    /**
     * Save recipe to JSON file.
     */
    public void saveRecipe() {
        HTMLAnchorElement a = (HTMLAnchorElement) doc.createElement("a");
        String ser = JSON.serialize(machine.getRecipe()).stringify();
        JSObject file = JSBlob.create(ser, "text/plain");
        a.setHref(Utils.createURL(file));
        a.setDownload("recipe.json");
        a.click();
    }

    /**
     * Load recipe from File.
     */
    public void loadRecipe() {
        JSFile file;

        HTMLInputFileElement recipeFile = (HTMLInputFileElement) doc.getElementById("recipeFile");
        file = recipeFile.getFiles().get(0);

        JSFileReader fr = JSFileReader.create();
        fr.setOnLoad(evt -> {
            JSFileReader fileReader = (JSFileReader) evt.getTarget();
            JSObject fl = fileReader.getResult();
            XMLHttpRequest xhr = XMLHttpRequest.create();
            xhr.open("post", REST.MACHINE_SET_RECIPE);
            xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
            xhr.send(fl);
        });
        fr.readAsText(file);
    }
}
