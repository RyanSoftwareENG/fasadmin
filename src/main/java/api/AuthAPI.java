package api;

import dto.AdminLoginRequest;
import dto.AdminLoginResponse;

import java.io.IOException;

public class AuthAPI {

    private final ApiClient apiClient;

    public AuthAPI(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public AdminLoginResponse login(
            String username,
            String password
    ) throws IOException, InterruptedException {

        AdminLoginRequest request =
                new AdminLoginRequest();

        request.setUsername(username);
        request.setPassword(password);

        AdminLoginResponse response =
                apiClient.postAuthResponse(
                        ApiEndpoints.ADMIN_LOGIN,
                        request,
                        AdminLoginResponse.class
                );

        if (response != null &&
                response.isSuccess() &&
                response.getToken() != null &&
                !response.getToken().isBlank()) {

            apiClient.setToken(
                    response.getToken()
            );
        }

        return response;
    }

    public void logout()
            throws IOException, InterruptedException {

        apiClient.post(
                ApiEndpoints.ADMIN_LOGOUT,
                null,
                AdminLoginResponse.class
        );

        apiClient.clearToken();
    }
}