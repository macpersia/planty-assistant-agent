package be.planty.agents.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.security.sasl.AuthenticationException;
import java.util.HashMap;

import static java.util.Arrays.asList;
import static org.springframework.util.StringUtils.isEmpty;

public class AssistantAgent implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AssistantAgent.class);

    static final String baseUrl = System.getProperty("be.planty.assistant.login.url");
    static final String username = System.getProperty("be.planty.assistant.access.id");
    static final String password = System.getProperty("be.planty.assistant.access.key");
    static final String wsUrl = System.getProperty("be.planty.assistant.ws.url"); // e.g. ws://localhost:8080/websocket

    private final String agentName;
    private final AgentSessionHandler agentSessionHandler;

    public AssistantAgent(String agentName) {
        this(agentName, new AgentSessionHandler());
    }

    public AssistantAgent(String agentName, AgentSessionHandler agentSessionHandler) {
        this.agentName = agentName;
        this.agentSessionHandler = agentSessionHandler;
    }

    public void run() {
        try {
            final var accessToken = login(baseUrl, username, password);
            final var url = wsUrl + "/pairing?access_token=" + accessToken;
            final var stompClient = createStompClient();
            final var handler = getPairingSessionHandler(accessToken);
            logger.info("Connecting to: " + url + " ...");
            stompClient.connect(url, handler);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected PairingSessionHandler getPairingSessionHandler(String accessToken) {
        return new PairingSessionHandler(accessToken, wsUrl, agentName, agentSessionHandler);
    }

    static WebSocketStompClient createStompClient() {
        final var socketClient = new StandardWebSocketClient();

        //WebSocketStompClient stompClient = new WebSocketStompClient(socketClient);
        final var sockJsClient = new SockJsClient(asList(
                new WebSocketTransport(socketClient)
        ));
        final var stompClient = new WebSocketStompClient(sockJsClient);

        stompClient.setMessageConverter(
                new CompositeMessageConverter(asList(
                    new MappingJackson2MessageConverter(),
                    new StringMessageConverter()
        )));
        stompClient.setTaskScheduler(new DefaultManagedTaskScheduler());
        return stompClient;
    }

    private String login(String baseUrl, String username, String password) throws AuthenticationException {
        final var request = new HashMap(){{
            put("username", username);
            put("password", password);
        }};
        final var response = new RestTemplate().postForEntity(baseUrl, request, String.class);

        if (response.getStatusCode().isError()) {
            logger.error(response.toString());
            throw new AuthenticationException(response.toString());
        }
        final var respHeaders = response.getHeaders();
        final var authHeader = respHeaders.getFirst("Authorization");
        if (isEmpty(authHeader)) {
            final var msg = "No 'Authorization header found!";
            logger.error(msg + " : " + response.toString());
            throw new AuthenticationException(msg);
        }
        if (!authHeader.startsWith("Bearer ")) {
            final var msg = "The 'Authorization header does not start with 'Bearer '!";
            logger.error(msg + " : " + authHeader);
            throw new AuthenticationException(msg);
        }
        return authHeader.substring(7);
    }
}

