package de.hzg.wpi.waltz.magix;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

/**
 * @author ingvord
 * @since 18.06.2020
 */
@Path("/")
public class MagixRestService {
    private volatile SseBroadcaster broadcaster;

    @Context
    public void setSse(Sse sse) {
        broadcaster = sse.newBroadcaster();
    }

    @POST
    @Path("/broadcast")
    public void post(String message, @Context Sse sse) {
        broadcaster.broadcast(sse.newEvent(message));
    }

    @GET
    @Path("/subscribe")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getStream(@Context SseEventSink sink) {
        broadcaster.register(sink);
    }
}
