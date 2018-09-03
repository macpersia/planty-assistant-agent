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

        final Object response;
        final StompHeaders newHeaders = createStompHeaders(headers);
        if (payload.equals("Ping!")) {
            response = "Pong!";

        } else if (payload instanceof ActionRequest
                    && ((ActionRequest)payload).action.equals("Ping!")) {
            response = new ActionResponse(0);
            newHeaders.set(PAYLOAD_TYPE_KEY, response.getClass().getTypeName());
        } else
            return;
        
        logger.info("Sending: " + response);
        session.send(newHeaders, response);
    }

    protected StompHeaders createStompHeaders(StompHeaders headers) {
        final StompHeaders newHeaders = new StompHeaders();
        headers.forEach(newHeaders::put);
        newHeaders.setDestination("/topic/action-responses");
        newHeaders.set("correlation-id", headers.getMessageId());
        return newHeaders;
    }
}
