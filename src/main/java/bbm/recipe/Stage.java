package bbm.recipe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.teavm.flavour.json.JsonPersistable;

import java.io.Serializable;

/**
 * Beer brewing stage.
 */
@JsonPersistable
public class Stage implements Serializable {

    /**
     * Stage name. (Optional, could be empty)
     */
    @Getter
    @Setter
    private String name;

    /**
     * Temperature.
     */
    @Getter
    @Setter
    private Float temp;
    /**
     * Time in seconds.
     */
    @Getter
    @Setter
    private Integer duration;

    /**
     * Instantiate recipe with specified name, temperature and duration in seconds.
     *
     * @param pName     Stage name
     * @param pTemp     Temperature in C
     * @param pDuration Duration in seconds
     */
    @JsonCreator
    public Stage(@JsonProperty("name") final String pName,
                 @JsonProperty("temp") final Float pTemp,
                 @JsonProperty("duration") final Integer pDuration) {
        this.name = pName;
        this.temp = pTemp;
        this.duration = pDuration;
    }

    /**
     * Instantiate recipe with specified temperature and duration in seconds.
     *
     * @param pTemp     Temperature in C
     * @param pDuration Duration in seconds
     */
    public Stage(final Float pTemp, final Integer pDuration) {
        this("", pTemp, pDuration);
    }


}
