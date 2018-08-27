package edu.uw.edm.eventpublisher;

import edu.uw.edm.eventpublisher.sns.model.DocumentChangedEvent;

/**
 * @author Maxime Deravet Date: 8/23/18
 */
public interface EventEmitter {
    void sendEvent(DocumentChangedEvent event);
}
