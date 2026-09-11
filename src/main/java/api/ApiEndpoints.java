package api;

public final class ApiEndpoints {

    private ApiEndpoints() {
    }

    // =====================================================
    // Server
    // =====================================================

    public static final String BASE_URL =
            "http://localhost:8080";

    // =====================================================
    // Admin
    // =====================================================

    public static final String ADMIN =
            BASE_URL + "/api/admin";

    // =====================================================
    // Authentication
    // =====================================================

    public static final String ADMIN_AUTH =
            ADMIN + "/auth";

    public static final String ADMIN_LOGIN =
            ADMIN_AUTH + "/login";

    public static final String ADMIN_LOGOUT =
            ADMIN_AUTH + "/logout";

    // =====================================================
    // Clinics
    // =====================================================

    public static final String CLINICS =
            ADMIN + "/clinics";

    // =====================================================
    // Users
    // =====================================================

    public static final String USERS =
            ADMIN + "/users";

    // =====================================================
    // Roles
    // =====================================================

    public static final String ROLES =
            ADMIN + "/roles";
    public static final String DISABLED_ROLES =
            ROLES + "/disabled";

    public static String roleById(
            Long roleId
    ) {

        return ROLES
                + "/"
                + roleId;
    }

    public static String roleEnable(
            Long roleId
    ) {

        return ROLES
                + "/"
                + roleId
                + "/enable";
    }

    // =====================================================
    // Permissions
    // =====================================================

    /*
     * الصلاحيات معرفة مسبقًا من النظام.
     * لا يوجد Endpoint لإنشائها من JavaFX.
     */

    public static final String PERMISSIONS =
            ROLES + "/permissions";

    // =====================================================
    // Role / Permissions
    // =====================================================

    public static String rolePermissions(
            Long roleId
    ) {

        return ROLES
                + "/"
                + roleId
                + "/permissions";
    }

    // =====================================================
    // Subscriptions
    // =====================================================

    public static final String SUBSCRIPTIONS =
            ADMIN + "/subscriptions";

    // =====================================================
    // Subscription Plans
    // =====================================================

    public static final String SUBSCRIPTION_PLANS =
            ADMIN + "/subscription-plans";

    // =====================================================
    // Features
    // =====================================================

    public static final String FEATURES =
            ADMIN + "/features";

    // =====================================================
    // Plan Features
    // =====================================================

    public static final String PLAN_FEATURES =
            ADMIN + "/plan-features";

    // =====================================================
    // Subscription Activations
    // =====================================================

    public static final String SUBSCRIPTION_ACTIVATIONS =
            ADMIN + "/subscription-activations";

    public static String deviceActivation(
            Long subscriptionId
    ) {

        return BASE_URL
                + "/api/admin/device-activation/"
                + subscriptionId;
    }

    // =====================================================
    // Software Versions
    // =====================================================

    public static final String SOFTWARE_VERSIONS =
            ADMIN + "/versions";

    // =====================================================
    // Activity Logs
    // =====================================================

    public static final String ACTIVITY_LOGS =
            ADMIN + "/activity-logs";

    // =====================================================
    // Dashboard
    // =====================================================

    public static final String DASHBOARD =
            ADMIN + "/dashboard";

    // =====================================================
    // Devices
    // =====================================================

    public static final String DEVICES =
            ADMIN + "/devices";

    public static String device(
            Long deviceId
    ) {

        return DEVICES
                + "/"
                + deviceId;
    }

    public static String deviceStatus(
            Long deviceId,
            String status
    ) {

        return DEVICES
                + "/"
                + deviceId
                + "/status?status="
                + status;
    }
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