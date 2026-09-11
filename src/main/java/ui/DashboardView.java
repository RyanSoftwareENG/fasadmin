package ui;

import api.AdminApiManager;
import api.DashboardAPI;
import dto.AdminDashboardResponse;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DashboardView extends BorderPane {

    private final DashboardAPI dashboardAPI;

// =====================================================
// Labels - الإحصائيات الرئيسية
// =====================================================

    private final Label totalClinics =
            valueLabel();

    private final Label activeClinics =
            valueLabel();

    private final Label totalUsers =
            valueLabel();

    private final Label activeUsers =
            valueLabel();

    private final Label totalSubscriptions =
            valueLabel();

    private final Label activeSubscriptions =
            valueLabel();

// =====================================================
// Labels - التشغيل
// =====================================================

    private final Label totalDevices =
            valueLabel();

    private final Label activeDevices =
            valueLabel();

    private final Label blockedDevices =
            valueLabel();

    private final Label userSessions =
            valueLabel();

    private final Label adminSessions =
            valueLabel();

// =====================================================
// Labels - الخطط والمزايا
// =====================================================

    private final Label totalPlans =
            valueLabel();

    private final Label activePlans =
            valueLabel();

    private final Label totalFeatures =
            valueLabel();

    private final Label activeFeatures =
            valueLabel();

// =====================================================
// Labels - الاشتراكات حسب الحالة
// =====================================================

    private final Label pendingSubscriptions =
            valueLabel();

    private final Label expiredSubscriptions =
            valueLabel();

    private final Label suspendedSubscriptions =
            valueLabel();

    private final Label cancelledSubscriptions =
            valueLabel();

// =====================================================
// Labels - الإدارة
// =====================================================

    private final Label adminUsers =
            valueLabel();

    private final Label adminActiveUsers =
            valueLabel();

    private final Label roles =
            valueLabel();

    private final Label permissions =
            valueLabel();

// =====================================================
// النشاط
// =====================================================

    private final VBox activityContainer =
            new VBox(10);

    private final VBox activationContainer =
            new VBox(10);

// =====================================================
// حالة التحميل
// =====================================================

    private final Label stateLabel =
            new Label("جاهز");

    private final Button refreshButton =
            new Button("🔄 تحديث البيانات");

    public DashboardView() {

        dashboardAPI =
                AdminApiManager
                        .getInstance()
                        .getDashboardAPI();

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setStyle("""
        -fx-background-color: #f4f7f9;
    """);

        setPadding(
                new Insets(24)
        );

        buildUI();

        loadDashboard();
    }

// =====================================================
// بناء الواجهة
// =====================================================

    private void buildUI() {

        VBox root =
                new VBox(20);

        root.setFillWidth(true);

        // =================================================
        // Header
        // =================================================

        HBox header =
                new HBox(15);

        header.setAlignment(
                Pos.CENTER_RIGHT
        );

        VBox titleBox =
                new VBox(4);

        Label title =
                new Label(
                        "لوحة مراقبة FAS"
                );

        title.setStyle("""
        -fx-font-size: 28px;
        -fx-font-weight: bold;
        -fx-text-fill: #263238;
    """);

        Label subtitle =
                new Label(
                        "نظرة شاملة على حالة العيادات والمستخدمين والاشتراكات والأجهزة والنظام."
                );

        subtitle.setStyle("""
        -fx-font-size: 13px;
        -fx-text-fill: #607D8B;
    """);

        titleBox.getChildren().addAll(
                title,
                subtitle
        );

        Region headerSpacer =
                new Region();

        HBox.setHgrow(
                headerSpacer,
                Priority.ALWAYS
        );

        stateLabel.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #607D8B;
    """);

        refreshButton.setPrefHeight(38);

        refreshButton.setStyle("""
        -fx-background-color: #263238;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 8;
        -fx-padding: 0 16 0 16;
    """);

        refreshButton.setOnAction(
                e -> loadDashboard()
        );

        header.getChildren().addAll(
                titleBox,
                headerSpacer,
                stateLabel,
                refreshButton
        );

        // =================================================
        // KPI Section
        // =================================================

        Label kpiTitle =
                sectionTitle(
                        "المؤشرات الرئيسية"
                );

        GridPane kpiGrid =
                new GridPane();

        kpiGrid.setHgap(14);
        kpiGrid.setVgap(14);

        kpiGrid.add(
                createMetricCard(
                        "العيادات",
                        "إجمالي العيادات",
                        totalClinics,
                        "🏥"
                ),
                0, 0
        );

        kpiGrid.add(
                createMetricCard(
                        "العيادات النشطة",
                        "العيادات العاملة حاليًا",
                        activeClinics,
                        "✅"
                ),
                1, 0
        );

        kpiGrid.add(
                createMetricCard(
                        "المستخدمون",
                        "إجمالي مستخدمي العيادات",
                        totalUsers,
                        "👥"
                ),
                2, 0
        );

        kpiGrid.add(
                createMetricCard(
                        "المستخدمون النشطون",
                        "الحسابات النشطة",
                        activeUsers,
                        "🟢"
                ),
                3, 0
        );

        kpiGrid.add(
                createMetricCard(
                        "الاشتراكات",
                        "إجمالي الاشتراكات",
                        totalSubscriptions,
                        "💳"
                ),
                0, 1
        );

        kpiGrid.add(
                createMetricCard(
                        "الاشتراكات النشطة",
                        "اشتراكات سارية حاليًا",
                        activeSubscriptions,
                        "🔥"
                ),
                1, 1
        );

        kpiGrid.add(
                createMetricCard(
                        "الأجهزة",
                        "إجمالي أجهزة العيادات",
                        totalDevices,
                        "💻"
                ),
                2, 1
        );

        kpiGrid.add(
                createMetricCard(
                        "الأجهزة النشطة",
                        "أجهزة تعمل حاليًا",
                        activeDevices,
                        "📡"
                ),
                3, 1
        );

        for (int i = 0; i < 4; i++) {

            ColumnConstraints cc =
                    new ColumnConstraints();

            cc.setHgrow(
                    Priority.ALWAYS
            );

            cc.setFillWidth(true);

            kpiGrid
                    .getColumnConstraints()
                    .add(cc);
        }

        // =================================================
        // Operational Section
        // =================================================

        Label operationsTitle =
                sectionTitle(
                        "الحالة التشغيلية"
                );

        GridPane operationsGrid =
                new GridPane();

        operationsGrid.setHgap(14);
        operationsGrid.setVgap(14);

        operationsGrid.add(
                createMiniCard(
                        "جلسات مستخدمي العيادات",
                        userSessions,
                        "جلسة نشطة"
                ),
                0, 0
        );

        operationsGrid.add(
                createMiniCard(
                        "جلسات FAS Admin",
                        adminSessions,
                        "جلسة نشطة"
                ),
                1, 0
        );

        operationsGrid.add(
                createMiniCard(
                        "الأجهزة المحظورة",
                        blockedDevices,
                        "جهاز"
                ),
                2, 0
        );

        operationsGrid.add(
                createMiniCard(
                        "الخطط النشطة",
                        activePlans,
                        "خطة"
                ),
                3, 0
        );

        operationsGrid.add(
                createMiniCard(
                        "المزايا النشطة",
                        activeFeatures,
                        "ميزة"
                ),
                4, 0
        );

        // =================================================
        // Subscription Status Section
        // =================================================

        Label subscriptionsTitle =
                sectionTitle(
                        "حالة الاشتراكات"
                );

        HBox subscriptionStatus =
                new HBox(14);

        subscriptionStatus.setFillHeight(true);

        subscriptionStatus.getChildren().addAll(
                createStatusCard(
                        "قيد الانتظار",
                        pendingSubscriptions,
                        "PENDING"
                ),
                createStatusCard(
                        "نشطة",
                        activeSubscriptions,
                        "ACTIVE"
                ),
                createStatusCard(
                        "منتهية",
                        expiredSubscriptions,
                        "EXPIRED"
                ),
                createStatusCard(
                        "موقفة",
                        suspendedSubscriptions,
                        "SUSPENDED"
                ),
                createStatusCard(
                        "ملغاة",
                        cancelledSubscriptions,
                        "CANCELLED"
                )
        );

        // =================================================
        // System Management
        // =================================================

        Label managementTitle =
                sectionTitle(
                        "إدارة النظام"
                );

        GridPane managementGrid =
                new GridPane();

        managementGrid.setHgap(14);
        managementGrid.setVgap(14);

        managementGrid.add(
                createMiniCard(
                        "خطط الاشتراك",
                        totalPlans,
                        "خطة"
                ),
                0, 0
        );

        managementGrid.add(
                createMiniCard(
                        "المزايا",
                        totalFeatures,
                        "ميزة"
                ),
                1, 0
        );

        managementGrid.add(
                createMiniCard(
                        "مديرو النظام",
                        adminUsers,
                        "حساب"
                ),
                2, 0
        );

        managementGrid.add(
                createMiniCard(
                        "المديرون النشطون",
                        adminActiveUsers,
                        "حساب نشط"
                ),
                3, 0
        );

        managementGrid.add(
                createMiniCard(
                        "الأدوار",
                        roles,
                        "دور"
                ),
                4, 0
        );

        managementGrid.add(
                createMiniCard(
                        "الصلاحيات",
                        permissions,
                        "صلاحية"
                ),
                5, 0
        );

        // =================================================
        // Recent activity
        // =================================================

        Label activitiesTitle =
                sectionTitle(
                        "المراقبة والنشاط"
                );

        HBox monitoring =
                new HBox(16);

        VBox recentActivityBox =
                createPanel(
                        "آخر النشاطات الإدارية",
                        activityContainer
                );

        VBox recentActivationBox =
                createPanel(
                        "آخر عمليات الاشتراك",
                        activationContainer
                );

        HBox.setHgrow(
                recentActivityBox,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                recentActivationBox,
                Priority.ALWAYS
        );

        monitoring.getChildren().addAll(
                recentActivityBox,
                recentActivationBox
        );

        // =================================================
        // Root
        // =================================================

        root.getChildren().addAll(
                header,

                kpiTitle,
                kpiGrid,

                operationsTitle,
                operationsGrid,

                subscriptionsTitle,
                subscriptionStatus,

                managementTitle,
                managementGrid,

                activitiesTitle,
                monitoring
        );

        ScrollPane scroll =
                new ScrollPane(root);

        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.setStyle("""
        -fx-background-color: transparent;
        -fx-border-color: transparent;
    """);

        setCenter(scroll);
    }

// =====================================================
// تحميل Dashboard
// =====================================================

    private void loadDashboard() {

        refreshButton.setDisable(true);

        stateLabel.setText(
                "جارٍ تحديث البيانات..."
        );

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        return dashboardAPI
                                .getDashboard();

                    } catch (
                            IOException |
                            InterruptedException e
                    ) {

                        if (e instanceof InterruptedException) {
                            Thread.currentThread()
                                    .interrupt();
                        }

                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(response ->
                        Platform.runLater(() -> {

                            updateDashboard(
                                    response
                            );

                            refreshButton
                                    .setDisable(false);

                            stateLabel.setText(
                                    "تم التحديث الآن"
                            );
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() -> {

                        refreshButton
                                .setDisable(false);

                        stateLabel.setText(
                                "تعذر تحديث البيانات"
                        );

                        showError(
                                "تعذر تحميل لوحة التحكم",
                                getRootCauseMessage(ex)
                        );
                    });

                    return null;
                });
    }

// =====================================================
// تحديث البيانات
// =====================================================

    private void updateDashboard(
            AdminDashboardResponse data
    ) {

        if (data == null) {
            return;
        }

        // -----------------------------------------------
        // Clinics
        // -----------------------------------------------

        totalClinics.setText(
                String.valueOf(
                        data.getTotalClinics()
                )
        );

        activeClinics.setText(
                String.valueOf(
                        data.getActiveClinics()
                )
        );

        // -----------------------------------------------
        // Users
        // -----------------------------------------------

        totalUsers.setText(
                String.valueOf(
                        data.getTotalUsers()
                )
        );

        activeUsers.setText(
                String.valueOf(
                        data.getActiveUsers()
                )
        );

        // -----------------------------------------------
        // Subscriptions
        // -----------------------------------------------

        totalSubscriptions.setText(
                String.valueOf(
                        data.getTotalSubscriptions()
                )
        );

        activeSubscriptions.setText(
                String.valueOf(
                        data.getActiveSubscriptions()
                )
        );

        pendingSubscriptions.setText(
                String.valueOf(
                        data.getPendingSubscriptions()
                )
        );

        expiredSubscriptions.setText(
                String.valueOf(
                        data.getExpiredSubscriptions()
                )
        );

        suspendedSubscriptions.setText(
                String.valueOf(
                        data.getSuspendedSubscriptions()
                )
        );

        cancelledSubscriptions.setText(
                String.valueOf(
                        data.getCancelledSubscriptions()
                )
        );

        // -----------------------------------------------
        // Devices
        // -----------------------------------------------

        totalDevices.setText(
                String.valueOf(
                        data.getTotalDevices()
                )
        );

        activeDevices.setText(
                String.valueOf(
                        data.getActiveDevices()
                )
        );

        blockedDevices.setText(
                String.valueOf(
                        data.getBlockedDevices()
                )
        );

        // -----------------------------------------------
        // Sessions
        // -----------------------------------------------

        userSessions.setText(
                String.valueOf(
                        data.getActiveUserSessions()
                )
        );

        adminSessions.setText(
                String.valueOf(
                        data.getActiveAdminSessions()
                )
        );

        // -----------------------------------------------
        // Plans
        // -----------------------------------------------

        totalPlans.setText(
                String.valueOf(
                        data.getTotalPlans()
                )
        );

        activePlans.setText(
                String.valueOf(
                        data.getActivePlans()
                )
        );

        // -----------------------------------------------
        // Features
        // -----------------------------------------------

        totalFeatures.setText(
                String.valueOf(
                        data.getTotalFeatures()
                )
        );

        activeFeatures.setText(
                String.valueOf(
                        data.getActiveFeatures()
                )
        );

        // -----------------------------------------------
        // Admin
        // -----------------------------------------------

        adminUsers.setText(
                String.valueOf(
                        data.getTotalAdminUsers()
                )
        );

        adminActiveUsers.setText(
                String.valueOf(
                        data.getActiveAdminUsers()
                )
        );

        roles.setText(
                String.valueOf(
                        data.getTotalRoles()
                )
        );

        permissions.setText(
                String.valueOf(
                        data.getTotalPermissions()
                )
        );

        // -----------------------------------------------
        // Activities
        // -----------------------------------------------

        updateActivities(
                data.getRecentActivities()
        );

        updateActivations(
                data.getRecentActivations()
        );
    }

// =====================================================
// آخر النشاطات
// =====================================================

    private void updateActivities(
            List<AdminDashboardResponse.ActivityItem> items
    ) {

        activityContainer
                .getChildren()
                .clear();

        if (items == null ||
                items.isEmpty()) {

            activityContainer
                    .getChildren()
                    .add(
                            emptyLabel(
                                    "لا توجد نشاطات إدارية مسجلة."
                            )
                    );

            return;
        }

        for (
                AdminDashboardResponse.ActivityItem item :
                items
        ) {

            activityContainer
                    .getChildren()
                    .add(
                            createActivityRow(
                                    item
                            )
                    );
        }
    }

// =====================================================
// عمليات الاشتراك الأخيرة
// =====================================================

    private void updateActivations(
            List<AdminDashboardResponse.ActivationItem> items
    ) {

        activationContainer
                .getChildren()
                .clear();

        if (items == null ||
                items.isEmpty()) {

            activationContainer
                    .getChildren()
                    .add(
                            emptyLabel(
                                    "لا توجد عمليات اشتراك مسجلة."
                            )
                    );

            return;
        }

        for (
                AdminDashboardResponse.ActivationItem item :
                items
        ) {

            activationContainer
                    .getChildren()
                    .add(
                            createActivationRow(
                                    item
                            )
                    );
        }
    }

// =====================================================
// Activity Row
// =====================================================

    private VBox createActivityRow(
            AdminDashboardResponse.ActivityItem item
    ) {

        VBox box =
                new VBox(4);

        box.setPadding(
                new Insets(10)
        );

        box.setStyle("""
        -fx-background-color: #f8fafb;
        -fx-background-radius: 8;
        -fx-border-color: #e5e9ec;
        -fx-border-radius: 8;
    """);

        String entity =
                safe(
                        item.getEntityName()
                );

        String action =
                safe(
                        item.getAction()
                );

        String details =
                safe(
                        item.getDetails()
                );

        Label main =
                new Label(
                        action
                                + (entity.isBlank()
                                ? ""
                                : "  •  " + entity)
                );

        main.setStyle("""
        -fx-font-size: 13px;
        -fx-font-weight: bold;
        -fx-text-fill: #263238;
    """);

        main.setWrapText(true);

        Label detail =
                new Label(
                        details
                );

        detail.setWrapText(true);

        detail.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #607D8B;
    """);

        Label date =
                new Label(
                        formatDate(
                                item.getCreatedAt()
                        )
                );

        date.setStyle("""
        -fx-font-size: 11px;
        -fx-text-fill: #90A4AE;
    """);

        box.getChildren().addAll(
                main,
                detail,
                date
        );

        return box;
    }

// =====================================================
// Activation Row
// =====================================================

    private VBox createActivationRow(
            AdminDashboardResponse.ActivationItem item
    ) {

        VBox box =
                new VBox(4);

        box.setPadding(
                new Insets(10)
        );

        box.setStyle("""
        -fx-background-color: #f8fafb;
        -fx-background-radius: 8;
        -fx-border-color: #e5e9ec;
        -fx-border-radius: 8;
    """);

        String oldStatus =
                safe(
                        item.getPreviousStatus()
                );

        String newStatus =
                safe(
                        item.getNewStatus()
                );

        Label status =
                new Label(
                        (oldStatus.isBlank()
                                ? "جديد"
                                : oldStatus)
                                + "  →  "
                                + newStatus
                );

        status.setStyle("""
        -fx-font-size: 13px;
        -fx-font-weight: bold;
        -fx-text-fill: #263238;
    """);

        Label subscription =
                new Label(
                        "الاشتراك #"
                                + safe(
                                item.getSubscriptionId()
                        )
                );

        subscription.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #607D8B;
    """);

        Label notes =
                new Label(
                        safe(item.getNotes())
                );

        notes.setWrapText(true);

        notes.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #607D8B;
    """);

        Label date =
                new Label(
                        formatDate(
                                item.getActivationDate()
                        )
                );

        date.setStyle("""
        -fx-font-size: 11px;
        -fx-text-fill: #90A4AE;
    """);

        box.getChildren().addAll(
                status,
                subscription,
                notes,
                date
        );

        return box;
    }

// =====================================================
// Metric Card
// =====================================================

    private VBox createMetricCard(
            String title,
            String subtitle,
            Label value,
            String icon
    ) {

        VBox card =
                new VBox(8);

        card.setMinHeight(135);
        card.setPadding(
                new Insets(18)
        );

        card.setAlignment(
                Pos.CENTER_RIGHT
        );

        card.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 14;
        -fx-border-color: #e1e7ea;
        -fx-border-radius: 14;
    """);

        HBox top =
                new HBox();

        top.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label iconLabel =
                new Label(icon);

        iconLabel.setStyle("""
        -fx-font-size: 22px;
    """);

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle("""
        -fx-font-size: 14px;
        -fx-font-weight: bold;
        -fx-text-fill: #455A64;
    """);

        top.getChildren().addAll(
                titleLabel,
                spacer,
                iconLabel
        );

        value.setStyle("""
        -fx-font-size: 32px;
        -fx-font-weight: bold;
        -fx-text-fill: #263238;
    """);

        Label subtitleLabel =
                new Label(subtitle);

        subtitleLabel.setStyle("""
        -fx-font-size: 11px;
        -fx-text-fill: #90A4AE;
    """);

        card.getChildren().addAll(
                top,
                value,
                subtitleLabel
        );

        return card;
    }

// =====================================================
// Mini Card
// =====================================================

    private VBox createMiniCard(
            String title,
            Label value,
            String unit
    ) {

        VBox card =
                new VBox(5);

        card.setMinHeight(95);
        card.setPadding(
                new Insets(14)
        );

        card.setAlignment(
                Pos.CENTER_RIGHT
        );

        card.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 12;
        -fx-border-color: #e1e7ea;
        -fx-border-radius: 12;
    """);

        Label titleLabel =
                new Label(title);

        titleLabel.setWrapText(true);

        titleLabel.setStyle("""
        -fx-font-size: 12px;
        -fx-font-weight: bold;
        -fx-text-fill: #607D8B;
    """);

        value.setStyle("""
        -fx-font-size: 25px;
        -fx-font-weight: bold;
        -fx-text-fill: #263238;
    """);

        Label unitLabel =
                new Label(unit);

        unitLabel.setStyle("""
        -fx-font-size: 10px;
        -fx-text-fill: #90A4AE;
    """);

        card.getChildren().addAll(
                titleLabel,
                value,
                unitLabel
        );

        return card;
    }

// =====================================================
// Status Card
// =====================================================

    private VBox createStatusCard(
            String title,
            Label value,
            String status
    ) {

        VBox card =
                new VBox(7);

        card.setMinWidth(150);
        card.setPrefHeight(100);

        card.setPadding(
                new Insets(14)
        );

        card.setAlignment(
                Pos.CENTER
        );

        card.setStyle(
                statusStyle(status)
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle("""
        -fx-font-size: 12px;
        -fx-font-weight: bold;
        -fx-text-fill: #455A64;
    """);

        value.setStyle("""
        -fx-font-size: 26px;
        -fx-font-weight: bold;
        -fx-text-fill: #263238;
    """);

        card.getChildren().addAll(
                titleLabel,
                value
        );

        HBox.setHgrow(
                card,
                Priority.ALWAYS
        );

        return card;
    }

// =====================================================
// Panel
// =====================================================

    private VBox createPanel(
            String title,
            VBox content
    ) {

        VBox panel =
                new VBox(10);

        panel.setPadding(
                new Insets(15)
        );

        panel.setMinHeight(280);

        panel.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 12;
        -fx-border-color: #e1e7ea;
        -fx-border-radius: 12;
    """);

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle("""
        -fx-font-size: 15px;
        -fx-font-weight: bold;
        -fx-text-fill: #263238;
    """);

        ScrollPane scroll =
                new ScrollPane(content);

        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.setStyle("""
        -fx-background-color: transparent;
        -fx-border-color: transparent;
    """);

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS
        );

        panel.getChildren().addAll(
                titleLabel,
                scroll
        );

        return panel;
    }

// =====================================================
// Section Title
// =====================================================

    private Label sectionTitle(
            String text
    ) {

        Label label =
                new Label(text);

        label.setStyle("""
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        -fx-text-fill: #263238;
    """);

        return label;
    }

// =====================================================
// Value Label
// =====================================================

    private static Label valueLabel() {

        Label label =
                new Label("0");

        label.setMinWidth(
                45
        );

        return label;
    }

// =====================================================
// Empty
// =====================================================

    private Label emptyLabel(
            String text
    ) {

        Label label =
                new Label(text);

        label.setWrapText(true);

        label.setStyle("""
        -fx-font-size: 12px;
        -fx-text-fill: #90A4AE;
        -fx-padding: 15;
    """);

        return label;
    }

// =====================================================
// Status Style
// =====================================================

    private String statusStyle(
            String status
    ) {

        return switch (status) {

            case "ACTIVE" -> """
            -fx-background-color: #E8F5E9;
            -fx-background-radius: 10;
            -fx-border-color: #C8E6C9;
            -fx-border-radius: 10;
        """;

            case "PENDING" -> """
            -fx-background-color: #FFF8E1;
            -fx-background-radius: 10;
            -fx-border-color: #FFECB3;
            -fx-border-radius: 10;
        """;

            case "EXPIRED" -> """
            -fx-background-color: #ECEFF1;
            -fx-background-radius: 10;
            -fx-border-color: #CFD8DC;
            -fx-border-radius: 10;
        """;

            case "SUSPENDED" -> """
            -fx-background-color: #FFF3E0;
            -fx-background-radius: 10;
            -fx-border-color: #FFE0B2;
            -fx-border-radius: 10;
        """;

            case "CANCELLED" -> """
            -fx-background-color: #FFEBEE;
            -fx-background-radius: 10;
            -fx-border-color: #FFCDD2;
            -fx-border-radius: 10;
        """;

            default -> """
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-color: #e1e7ea;
            -fx-border-radius: 10;
        """;
        };
    }

// =====================================================
// Date
// =====================================================

    private String formatDate(
            LocalDateTime dateTime
    ) {

        if (dateTime == null) {
            return "";
        }

        return dateTime.format(
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd  HH:mm"
                )
        );
    }

// =====================================================
// Safe
// =====================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    private String safe(
            Long value
    ) {

        return value == null
                ? "-"
                : String.valueOf(value);
    }

// =====================================================
// Error
// =====================================================

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(
                message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        alert.showAndWait();
    }

// =====================================================
// Root Cause
// =====================================================

    private String getRootCauseMessage(
            Throwable throwable
    ) {

        Throwable cause =
                throwable;

        while (
                cause != null &&
                        cause.getCause() != null
        ) {

            cause =
                    cause.getCause();
        }

        if (cause == null ||
                cause.getMessage() == null ||
                cause.getMessage().isBlank()) {

            return "حدث خطأ غير معروف.";
        }

        return cause.getMessage();
    }
}