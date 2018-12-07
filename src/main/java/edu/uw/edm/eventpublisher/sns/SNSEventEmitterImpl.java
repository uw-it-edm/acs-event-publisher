package edu.uw.edm.eventpublisher.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.edm.eventpublisher.EventEmitter;
import edu.uw.edm.eventpublisher.sns.model.DocumentChangedEvent;

/**
 * @author Maxime Deravet Date: 8/23/18
 */
public class SNSEventEmitterImpl implements EventEmitter {

    public static final String SNS_PROPERTY_PROFILE = "profile";
    public static final String SNS_PROPERTY_EVENT_TYPE = "event-type";
    public static final String SNS_DATATYPE_STRING = "String";

    private static Logger logger = LoggerFactory.getLogger(SNSEventEmitterImpl.class);

    private SNSClientFactory snsClientFactory;

    private final String snsTopicARN;


    private AmazonSNS snsClient;


    private ObjectMapper objectMapper = new ObjectMapper();


    public SNSEventEmitterImpl(SNSClientFactory snsClientFactory, String snsTopicARN) {
        this.snsClientFactory = snsClientFactory;
        this.snsTopicARN = snsTopicARN;
    }


    @Override
    public void sendEvent(DocumentChangedEvent event) {


        try {
            PublishRequest publishRequest = new PublishRequest(snsTopicARN, objectMapper.writeValueAsString(event));
            publishRequest.addMessageAttributesEntry(SNS_PROPERTY_PROFILE, new MessageAttributeValue().withDataType(SNS_DATATYPE_STRING).withStringValue(event.getProfile()));
            publishRequest.addMessageAttributesEntry(SNS_PROPERTY_EVENT_TYPE, new MessageAttributeValue().withDataType(SNS_DATATYPE_STRING).withStringValue(event.getType().name()));

            PublishResult publishResult = getSnsClient().publish(publishRequest);

            logger.debug("document event sent : {} for {}, messageId : {}", event.getType().name(), event.getDocumentId(), publishResult.getMessageId());
        } catch (Exception e) {
            logger.error("error while publishing event for " + event.getDocumentId(), e);
            resetSNSClient();
        }


    }

    private synchronized void resetSNSClient() {
        snsClient = null;
    }

    private AmazonSNS getSnsClient() {
        if (snsClient == null) {
            synchronized (this) {
                if (snsClient == null) {
                    snsClient = snsClientFactory.getSNSClient();
                }
            }
        }
        return snsClient;
    }


}