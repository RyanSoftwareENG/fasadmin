package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.FasUserCreateRequest;
import dto.FasUserResponse;
import dto.FasUserUpdateRequest;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class FasUserAPI {

    private final ApiClient apiClient;

    public FasUserAPI(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // =====================================================
    // جميع المستخدمين
    // =====================================================

    public List<FasUserResponse> getAllUsers()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.USERS
                );

        checkResponse(response);

        return apiClient
                .getObjectMapper()
                .readValue(
                        response.body(),
                        new TypeReference<List<FasUserResponse>>() {
                        }
                );
    }

    // =====================================================
    // مستخدمو عيادة محددة
    // =====================================================

    public List<FasUserResponse> getUsersByClinic(
            Long clinicId
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.USERS
                                + "/clinic/"
                                + clinicId
                );

        checkResponse(response);

        return apiClient
                .getObjectMapper()
                .readValue(
                        response.body(),
                        new TypeReference<List<FasUserResponse>>() {
                        }
                );
    }

    // =====================================================
    // مستخدم واحد
    // =====================================================

    public FasUserResponse getUser(
            Long userId
    ) throws IOException, InterruptedException {

        return apiClient.get(
                ApiEndpoints.USERS
                        + "/"
                        + userId,
                FasUserResponse.class
        );
    }

    // =====================================================
    // إنشاء مستخدم
    // =====================================================

    public FasUserResponse createUser(
            FasUserCreateRequest request
    ) throws IOException, InterruptedException {

        return apiClient.post(
                ApiEndpoints.USERS,
                request,
                FasUserResponse.class
        );
    }

    // =====================================================
    // تعديل مستخدم
    // =====================================================

    public FasUserResponse updateUser(
            Long userId,
            FasUserUpdateRequest request
    ) throws IOException, InterruptedException {

        return apiClient.put(
                ApiEndpoints.USERS
                        + "/"
                        + userId,
                request,
                FasUserResponse.class
        );
    }

    // =====================================================
    // تعليق المستخدم
    // =====================================================

    public FasUserResponse suspendUser(
            Long userId
    ) throws IOException, InterruptedException {

        return apiClient.put(
                ApiEndpoints.USERS
                        + "/"
                        + userId
                        + "/suspend",
                null,
                FasUserResponse.class
        );
    }

    // =====================================================
    // إعادة تفعيل المستخدم
    // =====================================================

    public FasUserResponse activateUser(
            Long userId
    ) throws IOException, InterruptedException {

        return apiClient.put(
                ApiEndpoints.USERS
                        + "/"
                        + userId
                        + "/activate",
                null,
                FasUserResponse.class
        );
    }

    // =====================================================
    // جلب أدوار المستخدم
    // =====================================================

    public List<RolePermissionAPI.RoleResponse> getUserRoles(
            Long userId
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.USERS
                                + "/"
                                + userId
                                + "/roles"
                );

        checkResponse(response);

        return apiClient
                .getObjectMapper()
                .readValue(
                        response.body(),
                        new TypeReference<List<RolePermissionAPI.RoleResponse>>() {
                        }
                );
    }

    // =====================================================
    // تعيين دور للمستخدم
    // =====================================================

    public void updateUserRole(
            Long userId,
            Long roleId
    ) throws IOException, InterruptedException {

        RoleUpdateRequest request =
                new RoleUpdateRequest(roleId);

        apiClient.put(
                ApiEndpoints.USERS
                        + "/"
                        + userId
                        + "/role",
                request,
                Void.class
        );
    }

    // =====================================================
    // فحص الاستجابة
    // =====================================================

    private void checkResponse(
            HttpResponse<String> response
    ) throws IOException {

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                    "فشل طلب المستخدمين. HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }
    }

    // =====================================================
    // Request لتغيير الدور
    // =====================================================

    public static class RoleUpdateRequest {

        private Long roleId;

        public RoleUpdateRequest() {
        }

        public RoleUpdateRequest(
                Long roleId
        ) {
            this.roleId = roleId;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(
                Long roleId
        ) {
            this.roleId = roleId;
        }
    }
}