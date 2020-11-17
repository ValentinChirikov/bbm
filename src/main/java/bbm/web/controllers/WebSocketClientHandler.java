package bbm.web.controllers;

import bbm.comm.MessageTransceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Web socket message exchange.
 */
@Component
public final class WebSocketClientHandler
        extends TextWebSocketHandler implements MessageTransceiver {

    public static final Logger LOG = LoggerFactory.getLogger(WebSocketClientHandler.class);
    /**
     * User supplied consumer that is called when incoming message received.
     */
    private Consumer<String> incomingMessageConsumer;
    /**
     * WebSocket sessions.
     */
    private Set<WebSocketSession> sessionSet;

    /**
     * Init bean.
     */
    public WebSocketClientHandler() {
        super();
        sessionSet = new HashSet<>();
        this.incomingMessageConsumer = nop -> { };
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wss) throws Exception {
        sessionSet.add(wss);
        super.afterConnectionEstablished(wss);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wss, CloseStatus status) throws Exception {
        sessionSet.remove(wss);
        super.afterConnectionClosed(wss, status);
    }

    @Override
    public void sendMessage(String message) throws IOException {
        for (WebSocketSession webSocketSession : sessionSet) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(message));
            }
        }
    }

    @Override
    public void receiveMessage(Consumer<String> stringConsumer) {
        incomingMessageConsumer = stringConsumer;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage msg)  {
        incomingMessageConsumer.accept(msg.getPayload());
        LOG.debug("Message received {}", msg.getPayload());
    }

}
