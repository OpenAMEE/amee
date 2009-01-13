package com.jellymold.utils.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Router;
import org.springframework.stereotype.Service;

@Service
public class ObserveEventService {

    @Autowired
    ObserveEventGateway observeEventGateway;

    /**
     * Raise an Event
     *
     * @param channel event channel
     * @param payload the object associated with the event
     */
    public void raiseEvent(String channel, Object payload) {
        ObservedEvent oe = new ObservedEvent(channel, payload);
        observeEventGateway.observe(oe);
    }

    /**
     * Routes events to the relevant channel
     *
     * @param oe ObservedEvent
     * @return returns the derived channel
     */
    @Router(inputChannel = "observe")
    public Object resolveOrderItemChannel(ObservedEvent oe) {
        System.out.println("2 " + oe.getChannel() + "> " + oe.getPayload());
        return oe.getChannel();
    }

}
