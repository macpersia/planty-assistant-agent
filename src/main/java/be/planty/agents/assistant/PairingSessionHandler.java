package be.planty.agents.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import static be.planty.agents.assistant.AssistantAgent.createStompClient;

public class PairingSessionHandler extends MyStompSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(PairingSessionHandler.class);

    private StompSession session;
    private String accessToken;
    private String wsUrl;

    public PairingSessionHandler(String accessToken, String wsUrl){
        this.accessToken = accessToken;
        this.wsUrl = wsUrl;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.session = session;
        logger.info("Connected!");
        session.subscribe("/topic/pairing.res", this);
        final var payload = new PairingRequest("Agent X", "1234", "ASDF");
        logger.info("Sending: " + toPrettyJson(payload));
        session.send("/topic/pairing.req", payload);
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
