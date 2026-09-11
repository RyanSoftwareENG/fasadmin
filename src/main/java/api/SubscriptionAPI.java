package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.SubscriptionRequest;
import dto.SubscriptionResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

import static api.ApiEndpoints.BASE_URL;

public class SubscriptionAPI {

    private final ApiClient apiClient;

    public SubscriptionAPI(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public SubscriptionResponse create(
            SubscriptionRequest request
    ) throws IOException, InterruptedException {

        return apiClient.post(
                ApiEndpoints.SUBSCRIPTIONS,
                request,
                SubscriptionResponse.class
        );
    }

    public SubscriptionResponse update(
            Long subscriptionId,
            SubscriptionRequest request
    ) throws IOException, InterruptedException {

        return apiClient.put(
                ApiEndpoints.SUBSCRIPTIONS
                        + "/"
                        + subscriptionId,
                request,
                SubscriptionResponse.class
        );
    }

    public SubscriptionResponse getById(
            Long subscriptionId
    ) throws IOException, InterruptedException {

        return apiClient.get(
                ApiEndpoints.SUBSCRIPTIONS
                        + "/"
                        + subscriptionId,
                SubscriptionResponse.class
        );
    }

    public List<SubscriptionResponse> getByClinic(
            Long clinicId
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.SUBSCRIPTIONS
                                + "/clinic/"
                                + clinicId
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<SubscriptionResponse>>() {}
        );
    }

    public void delete(
            Long subscriptionId
    ) throws IOException, InterruptedException {

        apiClient.delete(
                ApiEndpoints.SUBSCRIPTIONS
                        + "/"
                        + subscriptionId
        );
    }

    private void checkResponse(
            HttpResponse<String> response
    ) throws IOException {

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    "فشل طلب الاشتراك. HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }
    }
    // =====================================================
// Subscription Requests - Admin
// =====================================================

    public static final String SUBSCRIPTION_REQUESTS =
            BASE_URL + "/api/admin/subscription-requests";

    public static final String SUBSCRIPTION_REQUESTS_PENDING =
            SUBSCRIPTION_REQUESTS + "/pending";

    public static String subscriptionRequestApprove(
            Long requestId
    ) {

        return SUBSCRIPTION_REQUESTS
                + "/"
                + requestId
                + "/approve";
    }

    public static String subscriptionRequestReject(
            Long requestId
    ) {

        return SUBSCRIPTION_REQUESTS
                + "/"
                + requestId
                + "/reject";
    }
}