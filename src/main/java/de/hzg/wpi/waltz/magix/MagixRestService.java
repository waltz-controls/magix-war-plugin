package de.hzg.wpi.waltz.magix;

import co.elastic.apm.api.Span;
import co.elastic.apm.api.Transaction;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import co.elastic.apm.api.ElasticApm;


/**
 * @author ingvord
 * @since 18.06.2020
 */
@Path("/")
public class MagixRestService {
    public static final String TRANSACTION_NAME = "magix";
    private final Logger logger = LoggerFactory.getLogger(MagixRestService.class);

    private volatile SseBroadcaster broadcaster;

    private final ConcurrentMap<String, TransactionHolder> transactions = new ConcurrentHashMap<>();

    @Context
    public void setSse(Sse sse) {
        broadcaster = sse.newBroadcaster();
    }

    @GET
    public String get(){
        return "OK!";
    }

    private TransactionHolder beginTransaction(JSONObject jsonMessage){
        Transaction transaction = ElasticApm.startTransaction();
        transaction.setName(TRANSACTION_NAME);
        Span span = transaction.startSpan();
        span.setName("magix-broadcast");

        span.injectTraceHeaders((key, value) -> {
//                System.out.println(key);
//                System.out.println(value);
            if("traceparent".equals(key))
                ((JSONObject)jsonMessage.get("payload")).put("traceparent", value);
        });

        return new TransactionHolder(transaction, span);
    }

    private void endTransaction(TransactionHolder holder){
        holder.span.end();
        holder.transaction.end();
    }

    @POST
    @Path("/api/broadcast")
    public CompletionStage<?> post(String message, @QueryParam("channel") @DefaultValue("message") String channel, @Context Sse sse) {
        logger.debug("broadcasting message {} into channel {}", message, channel);

        //TODO use provider
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonMessage;
        try{
            jsonMessage = (JSONObject) jsonParser.parse(message);
        } catch (ParseException e){
            logger.warn(String.format("Failed to parse JSON string %s", message), e);
            CompletableFuture<?> rv = new CompletableFuture<>();
            rv.completeExceptionally(e);
            return rv;
        }


        Transaction txn = ElasticApm.currentTransaction();

        if(jsonMessage.get("origin").equals("axsis-gui")){
            txn = ElasticApm.startTransaction();
            txn.setName("move-from-gui-broadcast");
            txn.injectTraceHeaders((key, value) -> {
                if("traceparent".equals(key))
                    ((JSONObject)jsonMessage.get("payload")).put("traceparent", value);
            });
        }
        else if(jsonMessage.get("origin").equals("axsis-tango")){
            txn = ElasticApm.startTransactionWithRemoteParent(headerName -> String.valueOf(((JSONObject)jsonMessage.get("payload")).get(headerName)));
            txn.setName("move-from-tango-broadcast");
        }
//        else if(Optional.ofNullable(jsonMessage.get("action")).orElse("").equals("done"))
//            endTransaction(transactions.remove(String.valueOf(jsonMessage.get("parentId"))));
        else
            logger.debug("Skipping transaction handler for message {}", message);



        Span span = txn.startSpan();
        try {
            OutboundSseEvent event = sse.newEventBuilder()
                    .name(channel)
                    .data(jsonMessage.toJSONString())
                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .build();

            return broadcaster.broadcast(event);
        } finally {
            span.end();
            txn.end();
        }
    }

    @GET
    @Path("/api/subscribe")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getStream(@Context SseEventSink sink) {
        logger.debug("Subscribing new client...");
        broadcaster.register(sink);
    }

    private static class TransactionHolder {
        final Transaction transaction;
        final Span span;

        private TransactionHolder(Transaction transaction, Span span) {
            this.transaction = transaction;
            this.span = span;
        }
    }
}
