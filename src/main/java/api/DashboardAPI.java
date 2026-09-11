package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.AdminDashboardResponse;

import java.io.IOException;

public class DashboardAPI {

    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public DashboardAPI(
            ApiClient apiClient
    ) {
        this.apiClient = apiClient;
        this.objectMapper =
                apiClient.getObjectMapper();
    }

    public AdminDashboardResponse getDashboard()
            throws IOException, InterruptedException {

        return apiClient.get(
                ApiEndpoints.DASHBOARD,
                AdminDashboardResponse.class
        );
    }
}