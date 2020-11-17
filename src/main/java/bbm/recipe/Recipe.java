package bbm.recipe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.teavm.flavour.json.JsonPersistable;

import java.io.Serializable;
import java.util.List;

/**
 * Beer brewing recipe.
 * @author Valentin
 */
@Data
@JsonPersistable
public class Recipe implements Serializable {

    private static final long serialVersionUID = -4424288949968273452L;
    /**
     * Brew stages.
     */
    @Getter @Setter
    private List<Stage> stages;
    /**
     * Recipe name.
     */
    @Getter @Setter
    private String name;

    /**
     * Create recipe with given name.
     * @param pName recipe name
     */
    @JsonCreator
    public Recipe(@JsonProperty("name") final String pName) {
        this.name = pName;
    }

}
