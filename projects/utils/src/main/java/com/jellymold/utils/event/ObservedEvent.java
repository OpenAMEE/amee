package com.jellymold.utils.event;

/**
 * Observed Event
 */
public class ObservedEvent {
    private Object payload;
    private String channel;

    public ObservedEvent(String channel, Object payload) {
        this.channel = channel;
        this.payload = payload;
    }

    public Object getPayload() {
        return payload;
    }

    public String getChannel() {
        return channel;
    }
}



