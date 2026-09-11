package api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;

public class RolePermissionAPI {

    // =====================================================
    // API Client
    // =====================================================

    private final ApiClient apiClient;

    private final ObjectMapper objectMapper;

    // =====================================================
    // Constructor
    // =====================================================

    public RolePermissionAPI(
            ApiClient apiClient
    ) {

        this.apiClient =
                apiClient;

        this.objectMapper =
                apiClient.getObjectMapper();
    }

    // =====================================================
    // الأدوار
    // =====================================================

    // -----------------------------------------------------
    // جلب الأدوار النشطة
    // GET /api/admin/roles
    // -----------------------------------------------------

    public List<RoleResponse> getRoles()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.ROLES
                );

        ensureSuccess(
                response,
                "فشل جلب الأدوار"
        );

        return objectMapper.readValue(
                response.body(),
                new TypeReference<List<RoleResponse>>() {}
        );
    }

    // -----------------------------------------------------
    // إنشاء دور
    // POST /api/admin/roles
    // -----------------------------------------------------

    public RoleResponse createRole(
            String roleName,
            String description
    ) throws IOException, InterruptedException {

        validateRoleName(
                roleName
        );

        RoleRequest request =
                new RoleRequest(
                        roleName.trim(),
                        normalizeDescription(
                                description
                        )
                );

        return apiClient.post(
                ApiEndpoints.ROLES,
                request,
                RoleResponse.class
        );
    }

    // -----------------------------------------------------
    // تعديل دور
    // PUT /api/admin/roles/{roleId}
    // -----------------------------------------------------

    public RoleResponse updateRole(
            Long roleId,
            String roleName,
            String description
    ) throws IOException, InterruptedException {

        validateRoleId(
                roleId
        );

        validateRoleName(
                roleName
        );

        RoleRequest request =
                new RoleRequest(
                        roleName.trim(),
                        normalizeDescription(
                                description
                        )
                );

        return apiClient.put(
                ApiEndpoints.roleById(
                        roleId
                ),
                request,
                RoleResponse.class
        );
    }

    // -----------------------------------------------------
    // تعطيل دور
    // DELETE /api/admin/roles/{roleId}
    //
    // ملاحظة:
    // الـ Backend لا يحذف السجل فعليًا.
    // بل يحول STATUS إلى DISABLED.
    // -----------------------------------------------------

    public void disableRole(
            Long roleId
    ) throws IOException, InterruptedException {

        validateRoleId(
                roleId
        );

        apiClient.delete(
                ApiEndpoints.roleById(
                        roleId
                )
        );
    }

    // -----------------------------------------------------
    // تفعيل دور
    // PUT /api/admin/roles/{roleId}/enable
    // -----------------------------------------------------

    public void enableRole(
            Long roleId
    ) throws IOException, InterruptedException {

        validateRoleId(
                roleId
        );

        apiClient.put(
                ApiEndpoints.roleEnable(
                        roleId
                ),
                null,
                Void.class
        );
    }

    // =====================================================
    // الصلاحيات
    // =====================================================

    // -----------------------------------------------------
    // جلب جميع الصلاحيات النشطة
    // GET /api/admin/roles/permissions
    // -----------------------------------------------------

    public List<PermissionResponse> getPermissions()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.PERMISSIONS
                );

        ensureSuccess(
                response,
                "فشل جلب الصلاحيات"
        );

        return objectMapper.readValue(
                response.body(),
                new TypeReference<List<PermissionResponse>>() {}
        );
    }

    // -----------------------------------------------------
    // جلب صلاحيات دور محدد
    // GET /api/admin/roles/{roleId}/permissions
    // -----------------------------------------------------

    public List<RolePermissionResponse>
    getRolePermissions(
            Long roleId
    ) throws IOException, InterruptedException {

        validateRoleId(
                roleId
        );

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.rolePermissions(
                                roleId
                        )
                );

        ensureSuccess(
                response,
                "فشل جلب صلاحيات الدور"
        );

        return objectMapper.readValue(
                response.body(),
                new TypeReference<List<RolePermissionResponse>>() {}
        );
    }

    // -----------------------------------------------------
    // حفظ صلاحيات الدور
    // PUT /api/admin/roles/{roleId}/permissions
    // -----------------------------------------------------

    public void updateRolePermissions(
            Long roleId,
            Set<Long> permissionIds
    ) throws IOException, InterruptedException {

        validateRoleId(
                roleId
        );

        RolePermissionUpdateRequest request =
                new RolePermissionUpdateRequest(
                        permissionIds
                );

        apiClient.put(
                ApiEndpoints.rolePermissions(
                        roleId
                ),
                request,
                Void.class
        );
    }

    // =====================================================
    // التحقق من Role ID
    // =====================================================

    private void validateRoleId(
            Long roleId
    ) {

        if (roleId == null ||
                roleId <= 0) {

            throw new IllegalArgumentException(
                    "معرف الدور غير صالح."
            );
        }
    }

    // =====================================================
    // التحقق من اسم الدور
    // =====================================================

    private void validateRoleName(
            String roleName
    ) {

        if (roleName == null ||
                roleName.isBlank()) {

            throw new IllegalArgumentException(
                    "اسم الدور مطلوب."
            );
        }

        if (roleName.trim().length() > 100) {

            throw new IllegalArgumentException(
                    "اسم الدور يجب ألا يتجاوز 100 حرف."
            );
        }
    }

    // =====================================================
    // تنظيف وصف الدور
    // =====================================================

    private String normalizeDescription(
            String description
    ) {

        if (description == null ||
                description.isBlank()) {

            return null;
        }

        String value =
                description.trim();

        if (value.length() > 500) {

            throw new IllegalArgumentException(
                    "وصف الدور يجب ألا يتجاوز 500 حرف."
            );
        }

        return value;
    }

    // =====================================================
    // التحقق من استجابة API
    // =====================================================

    private void ensureSuccess(
            HttpResponse<String> response,
            String message
    ) throws IOException {

        if (response == null) {

            throw new IOException(
                    message
                            + ". السيرفر لم يُرجع استجابة."
            );
        }

        int status =
                response.statusCode();

        if (status < 200 ||
                status >= 300) {

            String body =
                    response.body();

            if (body == null ||
                    body.isBlank()) {

                body =
                        "لا توجد تفاصيل من السيرفر.";
            }

            throw new IOException(
                    message
                            + ". HTTP "
                            + status
                            + " - "
                            + body
            );
        }
    }

    // =====================================================
    // Role Request
    // =====================================================

    public static class RoleRequest {

        private String roleName;

        private String description;

        public RoleRequest() {
        }

        public RoleRequest(
                String roleName,
                String description
        ) {

            this.roleName =
                    roleName;

            this.description =
                    description;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(
                String roleName
        ) {

            this.roleName =
                    roleName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(
                String description
        ) {

            this.description =
                    description;
        }
    }

    // =====================================================
    // Role Response
    // =====================================================

    public static class RoleResponse {

        private Long roleId;

        private String roleName;

        private String description;

        private String status;

        public RoleResponse() {
        }

        public Long getRoleId() {
            return roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public String getDescription() {
            return description;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public String toString() {

            return roleName == null
                    ? ""
                    : roleName;
        }
    }

    // =====================================================
    // Permission Response
    // =====================================================

    public static class PermissionResponse {

        private Long permissionId;

        private String permissionCode;

        private String permissionName;

        private String description;

        private String status;

        public PermissionResponse() {
        }

        public Long getPermissionId() {
            return permissionId;
        }

        public String getPermissionCode() {
            return permissionCode;
        }

        public String getPermissionName() {
            return permissionName;
        }

        public String getDescription() {
            return description;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public String toString() {

            return permissionName == null
                    ? permissionCode
                    : permissionName;
        }
    }

    // =====================================================
    // Role Permission Response
    // =====================================================

    public static class RolePermissionResponse {

        private Long roleId;

        private String roleName;

        private Long permissionId;

        private String permissionCode;

        private String permissionName;

        private boolean assigned;

        public RolePermissionResponse() {
        }

        public Long getRoleId() {
            return roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public Long getPermissionId() {
            return permissionId;
        }

        public String getPermissionCode() {
            return permissionCode;
        }

        public String getPermissionName() {
            return permissionName;
        }

        public boolean isAssigned() {
            return assigned;
        }
    }

    // =====================================================
    // Role Permission Update Request
    // =====================================================

    public static class RolePermissionUpdateRequest {

        private Set<Long> permissionIds;

        public RolePermissionUpdateRequest() {
        }

        public RolePermissionUpdateRequest(
                Set<Long> permissionIds
        ) {

            this.permissionIds =
                    permissionIds;
        }

        public Set<Long> getPermissionIds() {
            return permissionIds;
        }

        public void setPermissionIds(
                Set<Long> permissionIds
        ) {

            this.permissionIds =
                    permissionIds;
        }
    }
// =====================================================
// جلب الأدوار المعطلة
// GET /api/admin/roles/disabled
// =====================================================

    public List<RoleResponse> getDisabledRoles()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.get(
                        ApiEndpoints.DISABLED_ROLES
                );

        ensureSuccess(
                response,
                "فشل جلب الأدوار المعطلة"
        );

        return objectMapper.readValue(
                response.body(),
                new TypeReference<List<RoleResponse>>() {}
        );
    }
}