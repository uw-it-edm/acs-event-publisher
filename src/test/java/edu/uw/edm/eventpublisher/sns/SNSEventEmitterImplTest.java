package edu.uw.edm.eventpublisher.sns;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import java.util.UUID;

import edu.uw.edm.eventpublisher.sns.model.DocumentChangedEvent;
import edu.uw.edm.eventpublisher.sns.model.DocumentChangedType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 8/27/18
 */
public class SNSEventEmitterImplTest {
    private static final String SNS_TOPIC_ARN = "my:arn";


    private AmazonSNS amazonSnsMock;
    private SNSClientFactory snsClientFactoryMock;

    private SNSEventEmitterImpl snsEventEmitter;

    @Before
    public void setUp() {
         amazonSnsMock = mock(AmazonSNS.class);

         snsClientFactoryMock = mock(SNSClientFactory.class);
        when(snsClientFactoryMock.getSNSClient()).thenReturn(amazonSnsMock);

        snsEventEmitter = new SNSEventEmitterImpl(snsClientFactoryMock, SNS_TOPIC_ARN);
    }

    @Test
    public void snsClientIsPreservedBetweenCallsTest() {

        PublishResult mockPublishResult = new PublishResult().withMessageId(UUID.randomUUID().toString());

        when(amazonSnsMock.publish(any(PublishRequest.class))).thenReturn(mockPublishResult);

        DocumentChangedEvent documentChangedEvent =
                new DocumentChangedEvent(DocumentChangedType.create, "123", "my-profile", 12345l);
        snsEventEmitter.sendEvent(documentChangedEvent);

        snsEventEmitter.sendEvent(documentChangedEvent);

        verify(snsClientFactoryMock, times(1)).getSNSClient();
    }

    @Test
    public void whenSnsCallFailsClientIsRecreatedTest() {
        when(amazonSnsMock.publish(any(PublishRequest.class))).thenThrow(new AmazonClientException("bla"));

        DocumentChangedEvent documentChangedEvent =
                new DocumentChangedEvent(DocumentChangedType.create, "123", "my-profile", 12345l);
        snsEventEmitter.sendEvent(documentChangedEvent);

        snsEventEmitter.sendEvent(documentChangedEvent);

        verify(snsClientFactoryMock, times(2)).getSNSClient();
    }

    @Test
    public void snsClientIsCalledTest() {

        PublishResult mockPublishResult = new PublishResult().withMessageId(UUID.randomUUID().toString());

        when(amazonSnsMock.publish(any(PublishRequest.class))).thenReturn(mockPublishResult);

        DocumentChangedEvent documentChangedEvent =
                new DocumentChangedEvent(DocumentChangedType.create, "123", "my-profile", 12345l);
        snsEventEmitter.sendEvent(documentChangedEvent);

        ArgumentCaptor<PublishRequest> argumentCaptor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(amazonSnsMock).publish(argumentCaptor.capture());


        PublishRequest capturePublishRequest = argumentCaptor.getValue();

        assertThat(capturePublishRequest.getMessageAttributes().get("profile").getStringValue(), is(equalTo("my-profile")));
        assertThat(capturePublishRequest.getMessageAttributes().get("event-type").getStringValue(), is(equalTo("create")));
    }

}