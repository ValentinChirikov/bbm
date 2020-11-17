package bbm.web.views.components;

import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.dom.html.HTMLInputElement;

public interface HTMLInputFileElement extends HTMLInputElement {
    @JSProperty
    JSArray<JSFile> getFiles();
}
