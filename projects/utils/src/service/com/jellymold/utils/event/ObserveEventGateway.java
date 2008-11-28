package com.jellymold.utils.event;

import org.springframework.integration.annotation.Gateway;

/**
 * Observable Event Gateway
 */
public interface ObserveEventGateway {

    @Gateway(requestChannel = "observe")
    public void observe(ObservedEvent oe);

}
