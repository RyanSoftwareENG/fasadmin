package api;

public final class AdminApiManager {

    private static final AdminApiManager INSTANCE =
            new AdminApiManager();

    private final FasAdminAPI fasAdminAPI;

    private final ClinicAPI clinicAPI;

    private final FasUserAPI fasUserAPI;

    private final RolePermissionAPI rolePermissionAPI;

    private final SubscriptionAPI subscriptionAPI;

    private final SubscriptionPlanAPI subscriptionPlanAPI;

    private final FeatureAPI featureAPI;

    private final PlanFeatureAPI planFeatureAPI;

    private final SubscriptionActivationAPI
            subscriptionActivationAPI;

    private final DeviceActivationAPI
            deviceActivationAPI;

    private final SoftwareVersionAPI
            softwareVersionAPI;

    private final ActivityLogAPI
            activityLogAPI;

    private final DashboardAPI
            dashboardAPI;

    private final DeviceAPI deviceAPI;

    private final SubscriptionRequestAPI subscriptionRequestAPI;

    private AdminApiManager() {

        fasAdminAPI =
                new FasAdminAPI();

        ApiClient apiClient =
                fasAdminAPI.getApiClient();

        clinicAPI =
                new ClinicAPI(
                        apiClient
                );

        fasUserAPI =
                new FasUserAPI(
                        apiClient
                );

        rolePermissionAPI =
                new RolePermissionAPI(
                        apiClient
                );

        subscriptionAPI =
                new SubscriptionAPI(
                        apiClient
                );

        subscriptionPlanAPI =
                new SubscriptionPlanAPI(
                        apiClient
                );

        featureAPI =
                new FeatureAPI(
                        apiClient
                );

        planFeatureAPI =
                new PlanFeatureAPI(
                        apiClient
                );

        subscriptionActivationAPI =
                new SubscriptionActivationAPI(
                        apiClient
                );

        deviceActivationAPI =
                new DeviceActivationAPI(
                        apiClient
                );

        softwareVersionAPI =
                new SoftwareVersionAPI(
                        apiClient
                );

        activityLogAPI =
                new ActivityLogAPI(
                        apiClient
                );

        dashboardAPI =
                new DashboardAPI(
                        apiClient
                );

        deviceAPI = new DeviceAPI(apiClient);

        this.subscriptionRequestAPI =
                new SubscriptionRequestAPI(
                        apiClient
                );
    }

// =====================================================
// Instance
// =====================================================

    public static AdminApiManager getInstance() {

        return INSTANCE;
    }

// =====================================================
// APIs
// =====================================================

    public FasAdminAPI getFasAdminAPI() {

        return fasAdminAPI;
    }

    public ClinicAPI getClinicAPI() {

        return clinicAPI;
    }

    public FasUserAPI getFasUserAPI() {

        return fasUserAPI;
    }

    public RolePermissionAPI getRolePermissionAPI() {

        return rolePermissionAPI;
    }

    public SubscriptionAPI getSubscriptionAPI() {

        return subscriptionAPI;
    }

    public SubscriptionPlanAPI
    getSubscriptionPlanAPI() {

        return subscriptionPlanAPI;
    }

    public FeatureAPI getFeatureAPI() {

        return featureAPI;
    }

    public PlanFeatureAPI getPlanFeatureAPI() {

        return planFeatureAPI;
    }

    public SubscriptionActivationAPI
    getSubscriptionActivationAPI() {

        return subscriptionActivationAPI;
    }

    public DeviceActivationAPI
    getDeviceActivationAPI() {

        return deviceActivationAPI;
    }

    public SoftwareVersionAPI
    getSoftwareVersionAPI() {

        return softwareVersionAPI;
    }

    public ActivityLogAPI
    getActivityLogAPI() {

        return activityLogAPI;
    }

    public DashboardAPI
    getDashboardAPI() {

        return dashboardAPI;
    }
    public DeviceAPI getDeviceAPI() {
        return deviceAPI;
    }

    public SubscriptionRequestAPI
    getSubscriptionRequestAPI() {

        return subscriptionRequestAPI;
    }
}
