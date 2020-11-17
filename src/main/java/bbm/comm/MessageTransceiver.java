package bbm.comm;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * A message sender/receiver.
 */
public interface MessageTransceiver {

    /**
     * Send text message.
     *
     * @param message text message
     * @throws IOException if fail to send message
     */
    void sendMessage(String message) throws IOException;

    /**
     * Receive text message.
     *
     * @param stringConsumer callback to user method
     */
    void receiveMessage(Consumer<String> stringConsumer);
}
