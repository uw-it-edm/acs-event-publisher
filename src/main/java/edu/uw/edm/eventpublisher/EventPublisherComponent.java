/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.uw.edm.eventpublisher;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uw.edm.eventpublisher.sns.model.DocumentChangedEvent;
import edu.uw.edm.eventpublisher.sns.model.DocumentChangedType;

/**
 * A basic component that will be started for this module. Uses the NodeLocatorService to easily
 * find nodes and the NodeService to display them
 *
 * @author Gabriele Columbro
 * @author Maurizio Pillitu
 */
public class EventPublisherComponent {


    static final QName UW_CONTENT = QName.createQName("http://www.uw.edu/model/content/1.0", "content");

    private static Pattern SITE_FINDER_REGEX = Pattern.compile("^/\\{.*\\}company_home/\\{.*\\}sites\\/\\{.*\\}(\\w+)/\\{.*\\}documentLibrary");

    private static Logger logger = LoggerFactory.getLogger(EventPublisherComponent.class);

    private PolicyComponent eventManager;
    private ServiceRegistry serviceRegistry;
    private EventEmitter eventEmitter;
    private boolean enabled;

    public EventPublisherComponent(PolicyComponent eventManager, ServiceRegistry serviceRegistry, EventEmitter eventEmitter, boolean enabled) {
        this.eventManager = eventManager;
        this.serviceRegistry = serviceRegistry;
        this.eventEmitter = eventEmitter;
        this.enabled = enabled;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.eventManager = policyComponent;
    }

    public void registerEventHandlers() {
        if (enabled) {
            eventManager.bindClassBehaviour(
                    NodeServicePolicies.OnCreateNodePolicy.QNAME,
                    UW_CONTENT,
                    new JavaBehaviour(this, "onAddDocument",
                            Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

            eventManager.bindClassBehaviour(
                    NodeServicePolicies.OnUpdateNodePolicy.QNAME,
                    UW_CONTENT,
                    new JavaBehaviour(this, "onUpdateDocument",
                            Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

            eventManager.bindClassBehaviour(
                    NodeServicePolicies.OnDeleteNodePolicy.QNAME,
                    ContentModel.TYPE_CONTENT,
                    new JavaBehaviour(this, "onDeleteDocument",
                            Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
        }
    }

    public void onAddDocument(ChildAssociationRef parentChildAssocRef) {
        try {
            NodeRef parentFolderRef = parentChildAssocRef.getParentRef();
            NodeRef docRef = parentChildAssocRef.getChildRef();

            // Check if node exists, might be moved, or created and deleted in same transaction.
            if (docRef == null || !serviceRegistry.getNodeService().exists(docRef)) {
                // Does not exist, nothing to do
                logger.warn("onAddDocument: A new document was added but removed in same transaction");
                return;
            } else {

                String profile = getProfileFromPath(docRef);

                Date modifiedAt = (Date) serviceRegistry.getNodeService().getProperty(docRef, ContentModel.PROP_MODIFIED);
                eventEmitter.sendEvent(new DocumentChangedEvent(DocumentChangedType.create, docRef.getId(), profile, modifiedAt.getTime()));
                logger.info("onAddDocument: A new document with ref ({}) was just created in folder ({})",
                        docRef, parentFolderRef);
            }
        } catch (Exception e) {
            logger.error("failed to execute 'onAddDocument'for {}", parentChildAssocRef);
        }
    }


    public void onUpdateDocument(NodeRef docNodeRef) {
        try {
            // Check if node exists, might be moved, or created and deleted in same transaction.
            if (docNodeRef == null || !serviceRegistry.getNodeService().exists(docNodeRef)) {
                // Does not exist, nothing to do
                logger.warn("onUpdateDocument: A document was updated but removed in same transaction");
                return;
            } else {
                NodeRef parentFolderRef = serviceRegistry.getNodeService().getPrimaryParent(docNodeRef).getParentRef();
                String profile = getProfileFromPath(docNodeRef);

                Date modifiedAt = (Date) serviceRegistry.getNodeService().getProperty(docNodeRef, ContentModel.PROP_MODIFIED);
                eventEmitter.sendEvent(new DocumentChangedEvent(DocumentChangedType.update, docNodeRef.getId(), profile, modifiedAt.getTime()));

                logger.info("onUpdateDocument: A document with ref ({}) was just updated in folder ({})",
                        docNodeRef, parentFolderRef);
            }
        } catch (Exception e) {
            logger.error("failed to execute 'onUpdateDocument'for {}", docNodeRef);
        }
    }

    public void onDeleteDocument(ChildAssociationRef parentChildAssocRef, boolean isNodeArchived) {
        try {
            NodeRef parentFolderRef = parentChildAssocRef.getParentRef();
            NodeRef docRef = parentChildAssocRef.getChildRef();
            String profile = getProfileFromPath(parentFolderRef);

            eventEmitter.sendEvent(new DocumentChangedEvent(DocumentChangedType.delete, docRef.getId(), profile, new Date().getTime()));

            logger.info("onDeleteDocument: A document with ref ({}) was just deleted in folder ({})",
                    docRef, parentFolderRef);
        } catch (Exception e) {
            logger.error("failed to execute 'onDeleteDocument'for " + parentChildAssocRef, e);
        }
    }

    private String getProfileFromPath(NodeRef docRef) {
        Path path = serviceRegistry.getNodeService().getPath(docRef);
        Matcher matcher = SITE_FINDER_REGEX.matcher(path.toString());

        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("Cannot find site for " + path.toString());
    }

    public void setEventEmitter(EventEmitter eventEmitter) {
        this.eventEmitter = eventEmitter;
    }
}
