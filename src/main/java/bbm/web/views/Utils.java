package bbm.web.views;

import bbm.web.views.components.TimerHandler;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLElement;

public class Utils {

    @JSBody(params = {"element", "className"}, script = "element.classList.add(className);")
    public static native void addClass(HTMLElement element, String className);

    @JSBody(params = {"element", "className"}, script = "element.classList.remove(className);")
    public static native void removeClass(HTMLElement element, String className);

    @JSBody(params = { "handler", "delay" }, script = "setTimeout(handler, delay);")
    public static native void setTimeout(TimerHandler handler, int delay);

    @JSBody(params = {"handler", "period"}, script = "setInterval(handler, period);")
    public static native void setInterval(TimerHandler handler, int period);
    /**
     * Log to console.
     *
     * @param msg Message
     */
    @JSBody(params = {"msg"}, script = "console.log(msg);")
    public static native void log(String msg);

    /**
     * Debug.
     *
     * @param msg Message
     * @param obj Object
     */
    @JSBody(params = {"msg", "obj"}, script = "console.debug(msg, obj);")
    public static native void debug(String msg, JSObject obj);

    @JSBody(params = {"file"}, script = "return URL.createObjectURL(file);")
    static native String createURL(JSObject file);

    @JSBody(params = {"timeBegin", "timeEnd"}, script = "return (moment(timeBegin).unix() - moment(timeEnd).unix());")
    public static native int timeDiff(String timeBegin, String timeEnd);

    private Utils() {
    }
}
