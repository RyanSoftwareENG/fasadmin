package ui;

import component.AdminHeader;
import component.AdminSidebar;
import javafx.scene.layout.BorderPane;

public class MainAdminView extends BorderPane {

    private final BorderPane contentPane = new BorderPane();
    private final AdminHeader header =
            new AdminHeader("لوحة التحكم");

    public MainAdminView() {

        setStyle("""
            -fx-background-color: #f4f6f8;
        """);

        AdminSidebar sidebar =
                new AdminSidebar(this);

        setLeft(sidebar);
        setTop(header);
        setCenter(contentPane);

        showDashboard();
    }

    public void showDashboard() {
        header.setTitle("لوحة التحكم");
        contentPane.setCenter(new DashboardView());
    }

    public void showClinics() {
        header.setTitle("إدارة العيادات");
        contentPane.setCenter(new ClinicsView());
    }

    public void showUsers() {
        header.setTitle("إدارة المستخدمين");
        contentPane.setCenter(new UsersView());
    }

    public void showDevices() {
        header.setTitle("إدارة الأجهزة");
        contentPane.setCenter(new DeviceManagementView());
    }

    public void showRolesPermissions() {
        header.setTitle("الأدوار والصلاحيات");
        contentPane.setCenter(new RolesPermissionsView());
    }

    public void showSubscriptions() {
        header.setTitle("إدارة الاشتراكات");
        contentPane.setCenter(new SubscriptionsView());
    }

    public void showFeatures() {
        header.setTitle("مزايا النظام");
        contentPane.setCenter(new FeaturesView());
    }

    public void showActivityLogs() {
        header.setTitle("سجل النشاط");
        contentPane.setCenter(new ActivityLogsView());
    }

    public void showSoftwareUpdates() {
        header.setTitle("إدارة التحديثات");
        contentPane.setCenter(new SoftwareUpdatesView());
    }

    public void showSettings() {
        header.setTitle("الإعدادات");
        contentPane.setCenter(new SettingsView());
    }

    public void showSubscriptionPlans() {
        header.setTitle("خطط الاشتراك");
        contentPane.setCenter(
                new SubscriptionPlansView()
        );
    }
}