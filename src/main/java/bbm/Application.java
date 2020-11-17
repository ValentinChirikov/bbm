package bbm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Beer Brewing Machine.
 */
@SpringBootApplication
public class Application {

    /**
     * Run Spring application.
     *
     * @param args command-line params
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
