package api;

import dto.DeviceActivationCodeResponse;

import java.io.IOException;

public class DeviceActivationAPI {

    private final ApiClient apiClient;

    public DeviceActivationAPI(
            ApiClient apiClient
    ) {
        this.apiClient =
                apiClient;
    }

// =====================================================
// إصدار كود تفعيل جهاز
// POST /api/admin/device-activation/{subscriptionId}
// =====================================================

    public DeviceActivationCodeResponse generate(
            Long subscriptionId
    ) throws IOException, InterruptedException {

        if (subscriptionId == null ||
                subscriptionId <= 0) {

            throw new IllegalArgumentException(
                    "معرف الاشتراك غير صالح."
            );
        }

        return apiClient.post(
                ApiEndpoints.deviceActivation(
                        subscriptionId
                ),
                null,
                DeviceActivationCodeResponse.class
        );
    }
}
