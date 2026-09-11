package component;

import ui.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AdminSidebar extends VBox {

    private final MainAdminView mainView;

    public AdminSidebar(
            MainAdminView mainView
    ) {

        this.mainView = mainView;

        setPrefWidth(230);
        setSpacing(8);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);

        setStyle("""
            -fx-background-color: #263238;
        """);

        Label logo =
                new Label("FAS Admin");

        logo.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 25px;
            -fx-font-weight: bold;
            -fx-padding: 0 0 20 0;
        """);

        // =====================================================
        // القائمة الرئيسية
        // =====================================================

        Button dashboard =
                createButton("🏠  لوحة التحكم");

        Button clinics =
                createButton("🏥  العيادات");

        Button users =
                createButton("👤  المستخدمون");

        Button devices =
                createButton("💻  إدارة الأجهزة");

        Button roles =
                createButton("🔐  الأدوار والصلاحيات");

        Button features =
                createButton("🧩  المزايا");

        Button subscriptionPlans =
                createButton("📦  خطط الاشتراك");

        Button subscriptions =
                createButton("💳  الاشتراكات");

        Button activity =
                createButton("📋  سجل النشاط");

        Button updates =
                createButton("🔄  التحديثات");

        Button settings =
                createButton("⚙  الإعدادات");

        Button logout =
                createButton("🚪  تسجيل الخروج");

        // =====================================================
        // الأحداث
        // =====================================================

        dashboard.setOnAction(e ->
                mainView.showDashboard()
        );

        clinics.setOnAction(e ->
                mainView.showClinics()
        );

        users.setOnAction(e ->
                mainView.showUsers()
        );

        devices.setOnAction(e ->
                mainView.showDevices()
        );

        roles.setOnAction(e ->
                mainView.showRolesPermissions()
        );

        features.setOnAction(e ->
                mainView.showFeatures()
        );

        subscriptionPlans.setOnAction(e ->
                mainView.showSubscriptionPlans()
        );

        subscriptions.setOnAction(e ->
                mainView.showSubscriptions()
        );

        activity.setOnAction(e ->
                mainView.showActivityLogs()
        );

        updates.setOnAction(e ->
                mainView.showSoftwareUpdates()
        );

        settings.setOnAction(e ->
                mainView.showSettings()
        );

        logout.setOnAction(e ->
                logout()
        );

        // =====================================================
        // Spacer
        // =====================================================

        VBox spacer =
                new VBox();

        VBox.setVgrow(
                spacer,
                javafx.scene.layout.Priority.ALWAYS
        );

        // =====================================================
        // إضافة العناصر
        // =====================================================

        getChildren().addAll(
                logo,
                dashboard,
                clinics,
                users,
                devices,
                roles,
                features,
                subscriptionPlans,
                subscriptions,
                activity,
                updates,
                settings,
                spacer,
                logout
        );
    }

    // =====================================================
    // إنشاء زر
    // =====================================================

    private Button createButton(
            String text
    ) {

        Button button =
                new Button(text);

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setMinHeight(42);

        button.setAlignment(
                Pos.CENTER_LEFT
        );

        button.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-background-radius: 6;
        """);

        button.setOnMouseEntered(e ->
                button.setStyle("""
                    -fx-background-color: #37474F;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 6;
                """)
        );

        button.setOnMouseExited(e ->
                button.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 6;
                """)
        );

        return button;
    }

    // =====================================================
    // تسجيل الخروج
    // =====================================================

    private void logout() {

        app.Main.getMainStage().setScene(
                new javafx.scene.Scene(
                        new LoginView(),
                        1000,
                        700
                )
        );
    }
}