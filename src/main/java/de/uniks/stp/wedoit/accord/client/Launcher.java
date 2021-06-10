package de.uniks.stp.wedoit.accord.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import de.uniks.stp.wedoit.accord.client.db.ApplicationConfig;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "de.uniks.stp.wedoit.accord.client")
public class Launcher {
    public static void main(String[] args) {
        Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel(Level.ERROR_INT));
        StageManager.launch(StageManager.class, args);
        SpringApplication.run(ApplicationConfig.class, args);



    }
}