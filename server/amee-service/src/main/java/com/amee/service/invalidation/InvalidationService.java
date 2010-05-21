package com.amee.service.invalidation;

import com.amee.domain.AMEEEntityReference;
import com.amee.domain.IAMEEEntityReference;
import com.amee.service.messaging.MessageService;
import com.amee.service.messaging.config.ExchangeConfig;
import com.amee.service.messaging.config.PublishConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class InvalidationService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private MessageService messageService;

    @Autowired
    @Qualifier("invalidationExchange")
    private ExchangeConfig exchangeConfig;

    @Autowired
    @Qualifier("invalidationPublish")
    private PublishConfig publishConfig;

    private ThreadLocal<Set<IAMEEEntityReference>> entities = new ThreadLocal<Set<IAMEEEntityReference>>() {
        protected Set<IAMEEEntityReference> initialValue() {
            return new HashSet<IAMEEEntityReference>();
        }
    };

    /**
     * Clears the entities Set. Called before each request is handled.
     * <p/>
     * We do this as the Thread may have been pooled before this execution.
     */
    public synchronized void beforeHandle() {
        log.debug("beforeHandle()");
        entities.get().clear();
    }

    /**
     * Adds an InvalidationMessage to the entities Set for the supplied entity. This will later be sent out
     * on the invalidation topic.
     *
     * @param entity to invalidate caches for
     */
    public synchronized void add(IAMEEEntityReference entity) {
        log.debug("add()");
        entities.get().add(new AMEEEntityReference(entity));
    }

    /**
     * Sends InvalidationMessages into the invalidation topic for the previously added in entities. Called after
     * each request has been handled.
     */
    public synchronized void afterHandle() {
        log.debug("afterHandle()");
        for (IAMEEEntityReference entity : entities.get()) {
            messageService.publish(
                    exchangeConfig,
                    publishConfig,
                    "platform." + publishConfig.getScope() + ".invalidation",
                    new InvalidationMessage(this, entity));
        }
        entities.get().clear();
    }
}
