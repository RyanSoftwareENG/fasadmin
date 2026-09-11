package dto;

import java.time.LocalDateTime;

public class DeviceActivationCodeResponse {

    private Long activationCodeId;
    private Long subscriptionId;
    private Long clinicId;

    private String activationCode;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public DeviceActivationCodeResponse() {
    }

    public Long getActivationCodeId() {
        return activationCodeId;
    }

    public void setActivationCodeId(
            Long activationCodeId
    ) {
        this.activationCodeId =
                activationCodeId;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(
            Long subscriptionId
    ) {
        this.subscriptionId =
                subscriptionId;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(
            Long clinicId
    ) {
        this.clinicId =
                clinicId;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(
            String activationCode
    ) {
        this.activationCode =
                activationCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status
    ) {
        this.status =
                status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt =
                createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(
            LocalDateTime expiresAt
    ) {
        this.expiresAt =
                expiresAt;
    }
}
