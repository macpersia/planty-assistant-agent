package be.planty.agents.assistant;

import be.planty.models.assistant.ActionRequest;
import be.planty.models.assistant.ActionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import java.lang.reflect.Type;

import static be.planty.models.assistant.Constants.PAYLOAD_TYPE_KEY;

public class AgentSessionHandler extends MyStompSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AgentSessionHandler.class);

    protected StompSession session;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.session = session;
        logger.info("Connected!");
        final String dest = "/user/queue/action-requests";
        logger.info("Subscribing to " + dest + "...");
        session.subscribe(dest, this);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        final String typeName = headers.getFirst(PAYLOAD_TYPE_KEY);
        if (typeName == null)
            return super.getPayloadType(headers);
        try {
            return Class.forName(typeName);

        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            return ActionRequest.class;
        }
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        logger.info("Received headers: " + headers);
        logger.info("Received payload: " +
                (payload instanceof String ?
                        payload : toPrettyJson(payload)));

        final String dest = "/topic/action-responses";
        final StompHeaders newHeaders = new StompHeaders();
        headers.forEach(newHeaders::put);
        newHeaders.setDestination(dest);
        newHeaders.set("correlation-id", headers.getMessageId());
        if (payload.equals("Ping!")) {
            final String response = "Pong!";
            logger.info("Sending: " + response);
            session.send(newHeaders, response);

        } else if (payload instanceof ActionRequest
                    && ((ActionRequest)payload).action.equals("Ping!")) {

            final ActionResponse response = new ActionResponse(0);
            logger.info("Sending: " + response);
            newHeaders.set(PAYLOAD_TYPE_KEY, response.getClass().getTypeName());
            session.send(newHeaders, response);
        }
//        if (headers.getDestination().startsWith("/user/queue/action-requests")) {
//            session.send("/topic/action-responses", "Thank you for choosing us!");
//        }
    }
}
