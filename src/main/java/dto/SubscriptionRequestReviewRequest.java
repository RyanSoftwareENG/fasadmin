package dto;

public class SubscriptionRequestReviewRequest {

    private String adminNotes;

    public SubscriptionRequestReviewRequest() {
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(
            String adminNotes
    ) {
        this.adminNotes = adminNotes;
    }
}