package bbm.web.controllers;

import bbm.label.Labeler;
import bbm.machine.BrewMachine;
import bbm.machine.MachineCommand;
import bbm.recipe.Recipe;
import bbm.recipe.StageState;
import bbm.web.models.BrewMachineDTO;
import com.google.zxing.WriterException;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Web endpoints handler.
 */
@RestController
public class REST {

    /**
     * Run machine URL.
     */
    public static final String MACHINE_EXECUTE_RUN = "/machine/execute/run";
    /**
     * Gets machine URL.
     */
    public static final String MACHINE_GET = "/machine/get";
    /**
     * Sets recipe URL.
     */
    public static final String MACHINE_SET_RECIPE = "/machine/set/recipe";
    /**
     * Date & time pattern.
     */
    public static final String TIME_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * Model to DTO mapper.
     */
    private final ModelMapper mm = new ModelMapper();
    /**
     * Brew machine.
     */
    @Autowired
    private BrewMachine brewMachine;
    /**
     * Bottle labeler.
     */
    @Autowired
    private Labeler labeler;

    {
        /*
         * Add LocalDateTime to String converter.
         */
        mm.addConverter(new AbstractConverter<LocalDateTime, String>() {
            @Override
            protected String convert(LocalDateTime source) {
                if (source != null) {
                    return DateTimeFormatter.ofPattern(TIME_DATE_PATTERN).format(source);
                }
                return "";
            }
        });
    }

    /**
     * @return BrewMachine DTO
     */
    @RequestMapping(value = MACHINE_GET, method = RequestMethod.GET)
    public BrewMachineDTO getBrewMachine() {
        return mm.map(brewMachine, BrewMachineDTO.class);
    }

    /**
     * @return current recipe
     */
    @RequestMapping(value = "/machine/get/recipe", method = RequestMethod.GET)
    public Recipe getRecipe() {
        return brewMachine.getRecipe();
    }

    /**
     * Sets recipe.
     *
     * @param recipe recipe
     */
    @RequestMapping(value = MACHINE_SET_RECIPE, method = RequestMethod.POST)
    public void setRecipe(@RequestBody Recipe recipe) {
        brewMachine.setRecipe(recipe);
    }

    /**
     * @return current Stage State
     */
    @RequestMapping(value = "/machine/get/stage_state", method = RequestMethod.GET)
    public StageState getStageStatus() {
        return brewMachine.getStageState();
    }

    /**
     * @return current wort Temp
     */
    @RequestMapping(value = "/machine/get/temp", method = RequestMethod.GET)
    public float getTemp() {
        return brewMachine.getWortTemp();
    }

    /**
     * @return current machine time
     */
    @RequestMapping(value = "/machine/get/time", method = RequestMethod.GET)
    public LocalDateTime getTime() {
        return brewMachine.getTime();
    }

    /**
     * Run machine.
     *
     * @return OK or INTERNAL_SERVER_ERROR
     */
    @RequestMapping(value = MACHINE_EXECUTE_RUN, method = RequestMethod.POST)
    public ResponseEntity<String> runMachine() {
        try {
            brewMachine.execute(MachineCommand.RUN);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to run machine. " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Machine started", HttpStatus.OK);

    }

    /**
     * Return generated PDF label with QRCode, name & description.
     *
     * @param code code content
     * @param name name
     * @param desc description
     * @return Resource with pdf label
     * @throws IOException     PDF label template load exception
     * @throws WriterException PDF label write exception
     */
    @RequestMapping(value = "/get/label", method = RequestMethod.GET, produces = "application/pdf")
    public ResponseEntity<Resource> getLabel(@RequestParam String code,
                                      @RequestParam String name,
                                      @RequestParam(required = false) String desc)
            throws IOException, WriterException {

        BufferedImage qrCode = labeler.getQRCode(code);
        ByteArrayOutputStream os = labeler.generateLabel(qrCode, name, desc);
        ByteArrayResource resource = new ByteArrayResource(os.toByteArray());

        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }


}
