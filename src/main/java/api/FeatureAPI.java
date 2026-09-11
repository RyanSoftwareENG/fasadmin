package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.FeatureRequest;
import dto.FeatureResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class FeatureAPI {

    private final ApiClient apiClient;

    public FeatureAPI(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public FeatureResponse create(
            FeatureRequest request
    ) throws IOException, InterruptedException {

        return apiClient.post(
                ApiEndpoints.FEATURES,
                request,
                FeatureResponse.class
        );
    }

    public FeatureResponse update(
            Long featureId,
            FeatureRequest request
    ) throws IOException, InterruptedException {

        return apiClient.put(
                ApiEndpoints.FEATURES
                        + "/"
                        + featureId,
                request,
                FeatureResponse.class
        );
    }

    public FeatureResponse getById(
            Long featureId
    ) throws IOException, InterruptedException {

        return apiClient.get(
                ApiEndpoints.FEATURES
                        + "/"
                        + featureId,
                FeatureResponse.class
        );
    }

    public List<FeatureResponse> getAll()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.FEATURES
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<FeatureResponse>>() {}
        );
    }

    public List<FeatureResponse> getActive()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.FEATURES
                                + "/active"
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<FeatureResponse>>() {}
        );
    }

    public void delete(
            Long featureId
    ) throws IOException, InterruptedException {

        apiClient.delete(
                ApiEndpoints.FEATURES
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
                    "فشل طلب الميزات. HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }
    }
}