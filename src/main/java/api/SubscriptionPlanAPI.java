package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.SubscriptionPlanRequest;
import dto.SubscriptionPlanResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class SubscriptionPlanAPI {

    private final ApiClient apiClient;

    public SubscriptionPlanAPI(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public SubscriptionPlanResponse create(
            SubscriptionPlanRequest request
    ) throws IOException, InterruptedException {

        return apiClient.post(
                ApiEndpoints.SUBSCRIPTION_PLANS,
                request,
                SubscriptionPlanResponse.class
        );
    }

    public SubscriptionPlanResponse update(
            Long planId,
            SubscriptionPlanRequest request
    ) throws IOException, InterruptedException {

        return apiClient.put(
                ApiEndpoints.SUBSCRIPTION_PLANS
                        + "/"
                        + planId,
                request,
                SubscriptionPlanResponse.class
        );
    }

    public SubscriptionPlanResponse getById(
            Long planId
    ) throws IOException, InterruptedException {

        return apiClient.get(
                ApiEndpoints.SUBSCRIPTION_PLANS
                        + "/"
                        + planId,
                SubscriptionPlanResponse.class
        );
    }

    public List<SubscriptionPlanResponse> getAll()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.SUBSCRIPTION_PLANS
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<SubscriptionPlanResponse>>() {}
        );
    }

    public List<SubscriptionPlanResponse> getActive()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.SUBSCRIPTION_PLANS
                                + "/active"
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<SubscriptionPlanResponse>>() {}
        );
    }

    public void delete(
            Long planId
    ) throws IOException, InterruptedException {

        apiClient.delete(
                ApiEndpoints.SUBSCRIPTION_PLANS
                        + "/"
                        + planId
        );
    }

    private void checkResponse(
            HttpResponse<String> response
    ) throws IOException {

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    "فشل طلب خطط الاشتراك. HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }
    }
}