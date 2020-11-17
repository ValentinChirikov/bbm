package bbm.web.controllers;

import bbm.sensor.SensorDataHandler;
import bbm.web.route.Routing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;

/**
 * Global application settings.
 * Time format, WS endpoints, Scheduler, Executor, etc.
 */
@Configuration
@EnableWebSocket
@EnableScheduling
@EnableAsync
public class WebControllersConfig implements WebSocketConfigurer {

    /**
     * Time format HH:mm:ss.
     */
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * WS Sensor reporting handler.
     */
    private SensorDataHandler sensorHandler;

    /**
     * WS Web Client Handler.
     */
    private WebSocketClientHandler clientHandler;

    /**
     * Init Bean with handlers.
     * @param sensorDataHandler Sensor handler
     * @param webSocketClientHandler Client handler
     */
    @Autowired
    public WebControllersConfig(final SensorDataHandler sensorDataHandler,
                                final WebSocketClientHandler webSocketClientHandler) {
        this.sensorHandler = sensorDataHandler;
        this.clientHandler = webSocketClientHandler;
    }

    @Bean
    public HttpMessageConverter<BufferedImage> bufferedImageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }

    /**
     * Register WebSocket message handlers.
     *
     * @param registry Application WS request mappings
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sensorHandler, Routing.SENSOR_WS_ENDPOINT)
                .setAllowedOrigins("*");
        registry.addHandler(clientHandler, Routing.CLIENT_WS_ENDPOINT)
                .setAllowedOrigins("*");
    }

}
