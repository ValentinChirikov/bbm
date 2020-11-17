package bbm.web.views.components;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface TimerHandler extends JSObject {
    void onTimer();
}