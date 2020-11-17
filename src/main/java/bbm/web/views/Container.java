package bbm.web.views;

import bbm.web.route.Routing;
import org.teavm.flavour.templates.BindTemplate;
import org.teavm.flavour.widgets.ApplicationTemplate;
import org.teavm.flavour.widgets.RouteBinder;

/***
 * This class is a "container" view.
 * View template is a place where we create navigation bar, header, footer, etc.
 */
@BindTemplate("templates/container.html")
public class Container extends ApplicationTemplate implements Routing {

    /**
     * View entry point.
     * @param args null
     */
    public static void main(String[] args) {
        Container container = new Container();
        new RouteBinder()
                .withDefault(Routing.class, Routing::bbm)
                .add(container)
                .update();
        container.bind("application-content");
    }

    @Override
    public void bbm() {
        setView(new Bbm());
    }
}
