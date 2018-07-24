package be.planty.agents.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

@SpringBootApplication
public class AssistantAgentApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AssistantAgentApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AssistantAgentApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final var agent = AssistantAgent.getInstance();
        newSingleThreadExecutor().submit(agent);
    }
}

