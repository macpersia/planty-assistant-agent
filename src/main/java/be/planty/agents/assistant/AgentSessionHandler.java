package be.planty.agents.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

public class AgentSessionHandler extends MyStompSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(PairingSessionHandler.class);
    private StompSession session;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.session = session;
        logger.info("Connected!");
        logger.info("Subscribing to /topic/action.req...");
        session.subscribe("/topic/action.req", this);
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        logger.info("Received headers: " + headers);
        logger.info("Received payload: " + payload);
        if (headers.getDestination().equals("/topic/action.req")) {
            session.send("/topic/action.res", "Thank you for choosing me!");
        }
    }

}
