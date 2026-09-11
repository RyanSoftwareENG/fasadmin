package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.PlanFeatureRequest;
import dto.PlanFeatureResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class PlanFeatureAPI {

    private final ApiClient apiClient;

    public PlanFeatureAPI(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public PlanFeatureResponse assign(
            PlanFeatureRequest request
    ) throws IOException, InterruptedException {

        return apiClient.post(
                ApiEndpoints.PLAN_FEATURES,
                request,
                PlanFeatureResponse.class
        );
    }

    public PlanFeatureResponse update(
            Long planId,
            Long featureId,
            PlanFeatureRequest request
    ) throws IOException, InterruptedException {

        return apiClient.put(
                ApiEndpoints.PLAN_FEATURES
                        + "/"
                        + planId
                        + "/"
                        + featureId,
                request,
                PlanFeatureResponse.class
        );
    }

    public List<PlanFeatureResponse> getByPlan(
            Long planId
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.PLAN_FEATURES
                                + "/plan/"
                                + planId
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<PlanFeatureResponse>>() {}
        );
    }

    public List<PlanFeatureResponse> getEnabledByPlan(
            Long planId
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.PLAN_FEATURES
                                + "/plan/"
                                + planId
                                + "/enabled"
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<PlanFeatureResponse>>() {}
        );
    }

    public void remove(
            Long planId,
            Long featureId
    ) throws IOException, InterruptedException {

        apiClient.delete(
                ApiEndpoints.PLAN_FEATURES
                        + "/"
                        + planId
                        + "/"
                        + featureId
        );
    }

    private void checkResponse(
            HttpResponse<String> response
    ) throws IOException {

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    "فشل طلب ربط الخطة بالميزة. HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }
    }
}