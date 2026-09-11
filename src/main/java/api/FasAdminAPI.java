package api;

import dto.AdminLoginResponse;
import session.AdminSession;

import java.io.IOException;

public class FasAdminAPI {

    private final ApiClient apiClient;
    private final AuthAPI authAPI;

    public FasAdminAPI() {

        this.apiClient = new ApiClient();

        this.authAPI = new AuthAPI(apiClient);
    }

    public AdminLoginResponse login(
            String username,
            String password
    ) throws IOException, InterruptedException {

        AdminLoginResponse response =
                authAPI.login(
                        username,
                        password
                );

        if (response != null &&
                response.isSuccess()) {

            AdminSession.start(
                    response.getAdminUserId(),
                    response.getUsername(),
                    response.getFullName(),
                    response.getToken()
            );

            apiClient.setToken(
                    response.getToken()
            );
        }

        return response;
    }

    public void logout()
            throws IOException, InterruptedException {

        try {

            if (AdminSession.isLoggedIn()) {
                authAPI.logout();
            }

        } finally {

            apiClient.clearToken();
            AdminSession.clear();
        }
    }

    public ApiClient getApiClient() {
        return apiClient;
    }
}