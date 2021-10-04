package de.hzg.wpi.waltz.magix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.util.concurrent.CompletionStage;

/**
 * @author ingvord
 * @since 18.06.2020
 */
@Path("/")
public class MagixRestService {
    private final Logger logger = LoggerFactory.getLogger(MagixRestService.class);

    private volatile SseBroadcaster broadcaster;

    @Context
    public void setSse(Sse sse) {
        broadcaster = sse.newBroadcaster();
    }

    @GET
    public String get(){
        return "OK!";
    }

    @POST
    @Path("/api/broadcast")
    public CompletionStage<?> post(String message, @QueryParam("channel") @DefaultValue("message") String channel, @Context Sse sse) {
        logger.debug("broadcasting message {} into channel {}", message, channel);
        OutboundSseEvent event = sse.newEventBuilder()
                .name(channel)
                .data(message)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .build();

        return broadcaster.broadcast(event);
    }

    @GET
    @Path("/api/subscribe")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getStream(@Context SseEventSink sink) {
        logger.debug("Subscribing new client...");
        broadcaster.register(sink);
    }
}
