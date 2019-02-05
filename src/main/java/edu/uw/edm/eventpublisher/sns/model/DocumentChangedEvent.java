package edu.uw.edm.eventpublisher.sns.model;

/**
 * @author Maxime Deravet Date: 8/23/18
 */
public class DocumentChangedEvent {
    private final DocumentChangedType type;
    private final String documentId;
    private final String profile;
    private final long lastModifiedDate;
    private final String wccId;

    public DocumentChangedEvent(DocumentChangedType type, String documentId, String profile, long lastModifiedDate, String wccId) {
        this.type = type;
        this.documentId = documentId;
        this.profile = profile;
        this.lastModifiedDate = lastModifiedDate;
        this.wccId = wccId;
    }


    public DocumentChangedType getType() {
        return type;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getProfile() {
        return profile;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getWccId() {
        return wccId;
    }
}
