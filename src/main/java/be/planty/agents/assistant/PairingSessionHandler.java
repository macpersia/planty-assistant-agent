package be.planty.agents.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.Random;

import static be.planty.agents.assistant.AssistantAgent.createStompClient;

public class PairingSessionHandler extends MyStompSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(PairingSessionHandler.class);

    private final String accessToken;
    private final String wsUrl;
    private final String agentName;

    public PairingSessionHandler(String accessToken, String wsUrl, String agentName){
        this.accessToken = accessToken;
        this.wsUrl = wsUrl;
        this.agentName = agentName;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("Connected!");
        session.subscribe("/user/queue/pairing-responses", this);
        final Integer verifiCode =  100000 + new Random().nextInt(899999);
        final var payload = new PairingRequest(agentName, verifiCode.toString(), null);
        logger.info("Sending: " + toPrettyJson(payload));
        session.send("/topic/pairing-requests", payload);
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
//			TextMessage msg = (TextMessage) payload;
//			logger.info("Received: " + msg.getPayload() + ", from: " + msg.getFrom());
        logger.info("Received headers: " + headers);
        logger.info("Received payload: " + payload);
        if (payload.toString().equals("accepted")) {
            subscribeToAgentRequests(wsUrl, accessToken);
        }
    }

    static void subscribeToAgentRequests(String wsUrl, String accessToken) {
        final var url = wsUrl + "/action?access_token=" + accessToken;
        final var stompClient = createStompClient();
        final var handler = new AgentSessionHandler();
        logger.info("Connecting to: " + url + " ...");
        stompClient.connect(url, handler);
    }

}
