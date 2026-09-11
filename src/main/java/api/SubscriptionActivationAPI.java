package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.SubscriptionActivationRequest;
import dto.SubscriptionActivationResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class SubscriptionActivationAPI {

    private final ApiClient apiClient;

    public SubscriptionActivationAPI(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public SubscriptionActivationResponse changeStatus(
            SubscriptionActivationRequest request
    ) throws IOException, InterruptedException {

        return apiClient.post(
                ApiEndpoints.SUBSCRIPTION_ACTIVATIONS
                        + "/change-status",
                request,
                SubscriptionActivationResponse.class
        );
    }

    public List<SubscriptionActivationResponse> getHistory(
            Long subscriptionId
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.SUBSCRIPTION_ACTIVATIONS
                                + "/subscription/"
                                + subscriptionId
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<SubscriptionActivationResponse>>() {}
        );
    }

    private void checkResponse(
            HttpResponse<String> response
    ) throws IOException {

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    "فشل طلب تفعيل الاشتراك. HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }
    }
}