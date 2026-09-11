package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.AdminActivityLogFilterRequest;
import dto.AdminActivityLogResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class ActivityLogAPI {

    private final ApiClient apiClient;

    public ActivityLogAPI(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<AdminActivityLogResponse> getAll()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.ACTIVITY_LOGS
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<AdminActivityLogResponse>>() {}
        );
    }

    public List<AdminActivityLogResponse> search(
            AdminActivityLogFilterRequest request
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.postRaw(
                        ApiEndpoints.ACTIVITY_LOGS + "/search",
                        request
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<AdminActivityLogResponse>>() {}
        );
    }

    public List<AdminActivityLogResponse> getByAdminUser(
            Long adminUserId
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.ACTIVITY_LOGS
                                + "/admin/"
                                + adminUserId
                );

        checkResponse(response);

        return apiClient.getObjectMapper().readValue(
                response.body(),
                new TypeReference<List<AdminActivityLogResponse>>() {}
        );
    }

    private void checkResponse(
            HttpResponse<String> response
    ) throws IOException {

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    "فشل طلب سجل النشاط. HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }
    }
}