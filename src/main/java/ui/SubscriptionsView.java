package ui;

import api.AdminApiManager;
import api.ClinicAPI;
import api.SubscriptionAPI;
import api.SubscriptionActivationAPI;
import api.SubscriptionPlanAPI;
import api.SubscriptionRequestAPI;

import dto.ClinicResponse;
import dto.SubscriptionActivationRequest;
import dto.SubscriptionPlanResponse;
import dto.SubscriptionRequestResponse;
import dto.SubscriptionResponse;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import session.AdminSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SubscriptionsView extends BorderPane {

    // =========================================================
    // DATE FORMAT
    // =========================================================

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================================
    // TABLES
    // =========================================================

    private final TableView<SubscriptionRow>
            subscriptionTable =
            new TableView<>();

    private final ObservableList<SubscriptionRow>
            subscriptionRows =
            FXCollections.observableArrayList();

    private final TableView<SubscriptionRequestRow>
            requestTable =
            new TableView<>();

    private final ObservableList<SubscriptionRequestRow>
            requestRows =
            FXCollections.observableArrayList();

    // =========================================================
    // APIS
    // =========================================================

    private final ClinicAPI clinicAPI;

    private final SubscriptionAPI subscriptionAPI;

    private final SubscriptionPlanAPI planAPI;

    private final SubscriptionActivationAPI activationAPI;

    private final SubscriptionRequestAPI
            subscriptionRequestAPI;

    // =========================================================
    // DATA
    // =========================================================

    private final List<ClinicResponse>
            clinics =
            new ArrayList<>();

    private final List<SubscriptionPlanResponse>
            plans =
            new ArrayList<>();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SubscriptionsView() {

        AdminApiManager apiManager =
                AdminApiManager.getInstance();

        clinicAPI =
                apiManager.getClinicAPI();

        subscriptionAPI =
                apiManager.getSubscriptionAPI();

        planAPI =
                apiManager.getSubscriptionPlanAPI();

        activationAPI =
                apiManager.getSubscriptionActivationAPI();

        subscriptionRequestAPI =
                apiManager.getSubscriptionRequestAPI();

        setPadding(
                new Insets(25)
        );

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        // =====================================================
        // TITLE
        // =====================================================

        Label title =
                new Label(
                        "إدارة الاشتراكات"
                );

        title.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: #1f2937;
        """);

        Label subtitle =
                new Label(
                        "إدارة الاشتراكات ومراجعة طلبات العيادات."
                );

        subtitle.setStyle("""
            -fx-font-size: 13px;
            -fx-text-fill: #6b7280;
        """);

        // =====================================================
        // TOOLBAR
        // =====================================================

        Button createButton =
                createToolbarButton(
                        "➕ إنشاء اشتراك"
                );

        Button activateButton =
                createToolbarButton(
                        "✅ تفعيل"
                );

        Button suspendButton =
                createToolbarButton(
                        "⛔ إيقاف"
                );

        Button renewButton =
                createToolbarButton(
                        "🔄 تجديد"
                );

        Button refreshButton =
                createToolbarButton(
                        "🔃 تحديث"
                );

        HBox toolbar =
                new HBox(
                        10,
                        createButton,
                        activateButton,
                        suspendButton,
                        renewButton,
                        refreshButton
                );

        toolbar.setAlignment(
                Pos.CENTER_RIGHT
        );

        // =====================================================
        // EVENTS
        // =====================================================

        createButton.setOnAction(
                e ->
                        showCreateSubscriptionDialog()
        );

        activateButton.setOnAction(
                e ->
                        activateSelected()
        );

        suspendButton.setOnAction(
                e ->
                        suspendSelected()
        );

        renewButton.setOnAction(
                e ->
                        renewSelected()
        );

        refreshButton.setOnAction(
                e ->
                        loadData()
        );

        // =====================================================
        // HEADER
        // =====================================================

        VBox top =
                new VBox(
                        5,
                        title,
                        subtitle,
                        new Separator(),
                        toolbar
                );

        top.setPadding(
                new Insets(
                        0,
                        0,
                        15,
                        0
                )
        );

        top.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        // =====================================================
        // MAIN CONTENT
        // =====================================================

        VBox content =
                new VBox(
                        25,
                        createSubscriptionsSection(),
                        createRequestsSection()
                );

        content.setFillWidth(
                true
        );

        setTop(
                top
        );

        setCenter(
                content
        );

        // =====================================================
        // LOAD
        // =====================================================

        loadData();
    }

    // =========================================================
    // SUBSCRIPTIONS SECTION
    // =========================================================

    private VBox createSubscriptionsSection() {

        Label title =
                new Label(
                        "الاشتراكات الحالية"
                );

        title.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: #374151;
        """);

        createSubscriptionTable();

        VBox box =
                new VBox(
                        10,
                        title,
                        subscriptionTable
                );

        box.setFillWidth(
                true
        );

        VBox.setVgrow(
                subscriptionTable,
                Priority.ALWAYS
        );

        return box;
    }

    // =========================================================
    // REQUESTS SECTION
    // =========================================================

    private VBox createRequestsSection() {

        Label title =
                new Label(
                        "طلبات الاشتراك"
                );

        title.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: #374151;
        """);

        createRequestTable();

        VBox box =
                new VBox(
                        10,
                        title,
                        requestTable
                );

        box.setFillWidth(
                true
        );

        return box;
    }

    // =========================================================
    // SUBSCRIPTION TABLE
    // =========================================================

    private void createSubscriptionTable() {

        TableColumn<
                SubscriptionRow,
                String
                > clinicColumn =
                new TableColumn<>(
                        "العيادة"
                );

        TableColumn<
                SubscriptionRow,
                String
                > planColumn =
                new TableColumn<>(
                        "الخطة"
                );

        TableColumn<
                SubscriptionRow,
                String
                > startColumn =
                new TableColumn<>(
                        "البداية"
                );

        TableColumn<
                SubscriptionRow,
                String
                > endColumn =
                new TableColumn<>(
                        "النهاية"
                );

        TableColumn<
                SubscriptionRow,
                String
                > statusColumn =
                new TableColumn<>(
                        "الحالة"
                );

        clinicColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .clinicProperty()
        );

        planColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .planProperty()
        );

        startColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .startProperty()
        );

        endColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .endProperty()
        );

        statusColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .statusProperty()
        );

        clinicColumn.setPrefWidth(
                220
        );

        planColumn.setPrefWidth(
                180
        );

        startColumn.setPrefWidth(
                140
        );

        endColumn.setPrefWidth(
                140
        );

        statusColumn.setPrefWidth(
                130
        );

        subscriptionTable
                .getColumns()
                .setAll(
                        clinicColumn,
                        planColumn,
                        startColumn,
                        endColumn,
                        statusColumn
                );

        subscriptionTable.setItems(
                subscriptionRows
        );

        subscriptionTable.setPlaceholder(
                new Label(
                        "لا توجد اشتراكات."
                )
        );

        subscriptionTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );
    }

    // =========================================================
    // REQUEST TABLE
    // =========================================================

    private void createRequestTable() {

        TableColumn<
                SubscriptionRequestRow,
                String
                > clinicColumn =
                new TableColumn<>(
                        "العيادة"
                );

        TableColumn<
                SubscriptionRequestRow,
                String
                > planColumn =
                new TableColumn<>(
                        "الخطة"
                );

        TableColumn<
                SubscriptionRequestRow,
                String
                > typeColumn =
                new TableColumn<>(
                        "نوع الطلب"
                );

        TableColumn<
                SubscriptionRequestRow,
                String
                > dateColumn =
                new TableColumn<>(
                        "تاريخ الطلب"
                );

        TableColumn<
                SubscriptionRequestRow,
                String
                > statusColumn =
                new TableColumn<>(
                        "الحالة"
                );

        TableColumn<
                SubscriptionRequestRow,
                String
                > ownerNotesColumn =
                new TableColumn<>(
                        "ملاحظات المالك"
                );

        TableColumn<
                SubscriptionRequestRow,
                String
                > adminNotesColumn =
                new TableColumn<>(
                        "ملاحظات المدير"
                );

        TableColumn<
                SubscriptionRequestRow,
                String
                > actionColumn =
                new TableColumn<>(
                        "الإجراء"
                );

        clinicColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .clinicProperty()
        );

        planColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .planProperty()
        );

        typeColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .typeProperty()
        );

        dateColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .dateProperty()
        );

        statusColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .statusProperty()
        );

        ownerNotesColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .ownerNotesProperty()
        );

        adminNotesColumn.setCellValueFactory(
                data ->
                        data.getValue()
                                .adminNotesProperty()
        );

        // =====================================================
        // ACTION COLUMN
        // =====================================================

        actionColumn.setCellFactory(
                column ->
                        new TableCell<
                                SubscriptionRequestRow,
                                String
                                >() {

                            private final Button approveButton =
                                    new Button(
                                            "✅ موافقة"
                                    );

                            private final Button rejectButton =
                                    new Button(
                                            "❌ رفض"
                                    );

                            private final HBox buttons =
                                    new HBox(
                                            8,
                                            approveButton,
                                            rejectButton
                                    );

                            {
                                buttons.setAlignment(
                                        Pos.CENTER
                                );

                                approveButton.setStyle("""
                                    -fx-background-color: #2e7d32;
                                    -fx-text-fill: white;
                                    -fx-font-weight: bold;
                                    -fx-background-radius: 6px;
                                    -fx-cursor: hand;
                                """);

                                rejectButton.setStyle("""
                                    -fx-background-color: #c62828;
                                    -fx-text-fill: white;
                                    -fx-font-weight: bold;
                                    -fx-background-radius: 6px;
                                    -fx-cursor: hand;
                                """);

                                approveButton.setOnAction(
                                        event -> {

                                            int index =
                                                    getIndex();

                                            if (index < 0 ||
                                                    index >= getTableView()
                                                            .getItems()
                                                            .size()) {

                                                return;
                                            }

                                            SubscriptionRequestRow row =
                                                    getTableView()
                                                            .getItems()
                                                            .get(index);

                                            approveRequest(
                                                    row
                                            );
                                        }
                                );

                                rejectButton.setOnAction(
                                        event -> {

                                            int index =
                                                    getIndex();

                                            if (index < 0 ||
                                                    index >= getTableView()
                                                            .getItems()
                                                            .size()) {

                                                return;
                                            }

                                            SubscriptionRequestRow row =
                                                    getTableView()
                                                            .getItems()
                                                            .get(index);

                                            rejectRequest(
                                                    row
                                            );
                                        }
                                );
                            }

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty) {

                                    setGraphic(
                                            null
                                    );

                                    return;
                                }

                                SubscriptionRequestRow row =
                                        getTableView()
                                                .getItems()
                                                .get(
                                                        getIndex()
                                                );

                                if (row == null ||
                                        !"PENDING".equalsIgnoreCase(
                                                row.getRawStatus()
                                        )) {

                                    setGraphic(
                                            null
                                    );

                                    return;
                                }

                                setGraphic(
                                        buttons
                                );
                            }
                        }
        );

        clinicColumn.setPrefWidth(
                160
        );

        planColumn.setPrefWidth(
                150
        );

        typeColumn.setPrefWidth(
                120
        );

        dateColumn.setPrefWidth(
                150
        );

        statusColumn.setPrefWidth(
                120
        );

        ownerNotesColumn.setPrefWidth(
                220
        );

        adminNotesColumn.setPrefWidth(
                220
        );

        actionColumn.setPrefWidth(
                190
        );

        requestTable
                .getColumns()
                .setAll(
                        clinicColumn,
                        planColumn,
                        typeColumn,
                        dateColumn,
                        statusColumn,
                        ownerNotesColumn,
                        adminNotesColumn,
                        actionColumn
                );

        requestTable.setItems(
                requestRows
        );

        requestTable.setPrefHeight(
                330
        );

        requestTable.setPlaceholder(
                new Label(
                        "لا توجد طلبات معلقة."
                )
        );

        requestTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );
    }

    // =========================================================
    // LOAD DATA
    // =========================================================

    private void loadData() {

        CompletableFuture
                .supplyAsync(
                        () -> {

                            try {

                                List<ClinicResponse>
                                        loadedClinics =
                                        clinicAPI
                                                .getAllClinics();

                                List<SubscriptionPlanResponse>
                                        loadedPlans =
                                        planAPI
                                                .getActive();

                                List<SubscriptionResponse>
                                        loadedSubscriptions =
                                        new ArrayList<>();

                                for (
                                        ClinicResponse clinic :
                                        loadedClinics
                                ) {

                                    List<SubscriptionResponse>
                                            clinicSubscriptions =
                                            subscriptionAPI
                                                    .getByClinic(
                                                            clinic.getClinicId()
                                                    );

                                    if (clinicSubscriptions != null) {

                                        loadedSubscriptions
                                                .addAll(
                                                        clinicSubscriptions
                                                );
                                    }
                                }

                                List<SubscriptionRequestResponse>
                                        loadedRequests =
                                        subscriptionRequestAPI
                                                .getPendingRequests();

                                return new LoadedData(
                                        loadedClinics,
                                        loadedPlans,
                                        loadedSubscriptions,
                                        loadedRequests
                                );

                            } catch (
                                    IOException |
                                    InterruptedException e
                            ) {

                                if (e instanceof
                                        InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                throw new RuntimeException(
                                        e
                                );
                            }
                        }
                )
                .thenAccept(
                        data ->
                                Platform.runLater(
                                        () ->
                                                applyLoadedData(
                                                        data
                                                )
                                )
                )
                .exceptionally(
                        ex -> {

                            Platform.runLater(
                                    () ->
                                            showError(
                                                    "تعذر تحميل بيانات الاشتراكات",
                                                    getRootCauseMessage(
                                                            ex
                                                    )
                                            )
                            );

                            return null;
                        }
                );
    }

    // =========================================================
    // APPLY DATA
    // =========================================================

    private void applyLoadedData(
            LoadedData data
    ) {

        clinics.clear();

        clinics.addAll(
                data.clinics()
        );

        plans.clear();

        plans.addAll(
                data.plans()
        );

        subscriptionRows.clear();

        for (
                SubscriptionResponse subscription :
                data.subscriptions()
        ) {

            subscriptionRows.add(
                    SubscriptionRow.fromResponse(
                            subscription
                    )
            );
        }

        requestRows.clear();

        for (
                SubscriptionRequestResponse request :
                data.requests()
        ) {

            requestRows.add(
                    SubscriptionRequestRow.fromResponse(
                            request,
                            clinics,
                            plans
                    )
            );
        }
    }

    // =========================================================
    // CREATE SUBSCRIPTION
    // =========================================================

    private void showCreateSubscriptionDialog() {

        if (clinics.isEmpty()) {

            showInfo(
                    "لا توجد عيادات",
                    "يجب إنشاء عيادة أولًا."
            );

            return;
        }

        if (plans.isEmpty()) {

            showInfo(
                    "لا توجد خطط",
                    "لا توجد خطة اشتراك نشطة حاليًا."
            );

            return;
        }

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "إنشاء اشتراك"
        );

        dialog.setHeaderText(
                "إنشاء اشتراك مباشر للعيادة"
        );

        VBox box =
                new VBox(
                        12
                );

        box.setPadding(
                new Insets(15)
        );

        box.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        ComboBox<ClinicChoice>
                clinicCombo =
                new ComboBox<>();

        ComboBox<PlanChoice>
                planCombo =
                new ComboBox<>();

        DatePicker startDate =
                new DatePicker(
                        LocalDate.now()
                );

        DatePicker endDate =
                new DatePicker();

        for (
                ClinicResponse clinic :
                clinics
        ) {

            clinicCombo.getItems().add(
                    new ClinicChoice(
                            clinic.getClinicId(),
                            clinic.getClinicName()
                    )
            );
        }

        for (
                SubscriptionPlanResponse plan :
                plans
        ) {

            planCombo.getItems().add(
                    new PlanChoice(
                            plan.getPlanId(),
                            plan.getPlanName(),
                            plan.getDurationDays()
                    )
            );
        }

        clinicCombo.setPromptText(
                "اختر العيادة"
        );

        planCombo.setPromptText(
                "اختر الخطة"
        );

        planCombo.setOnAction(
                e ->
                        updateEndDate(
                                planCombo,
                                startDate,
                                endDate
                        )
        );

        startDate.setOnAction(
                e ->
                        updateEndDate(
                                planCombo,
                                startDate,
                                endDate
                        )
        );

        box.getChildren().addAll(

                new Label(
                        "العيادة"
                ),

                clinicCombo,

                new Label(
                        "خطة الاشتراك"
                ),

                planCombo,

                new Label(
                        "تاريخ البداية"
                ),

                startDate,

                new Label(
                        "تاريخ النهاية"
                ),

                endDate
        );

        dialog.getDialogPane()
                .setContent(
                        box
                );

        dialog.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        ButtonType.OK,
                        ButtonType.CANCEL
                );

        dialog.showAndWait()
                .ifPresent(
                        result -> {

                            if (result != ButtonType.OK) {
                                return;
                            }

                            if (clinicCombo.getValue() == null ||
                                    planCombo.getValue() == null ||
                                    startDate.getValue() == null ||
                                    endDate.getValue() == null) {

                                showError(
                                        "بيانات غير مكتملة",
                                        "يرجى إدخال جميع البيانات."
                                );

                                return;
                            }

                            dto.SubscriptionRequest request =
                                    new dto.SubscriptionRequest();

                            request.setClinicId(
                                    clinicCombo
                                            .getValue()
                                            .clinicId()
                            );

                            request.setPlanId(
                                    planCombo
                                            .getValue()
                                            .planId()
                            );

                            request.setStartDate(
                                    startDate.getValue()
                            );

                            request.setEndDate(
                                    endDate.getValue()
                            );

                            request.setStatus(
                                    "ACTIVE"
                            );

                            createSubscription(
                                    request
                            );
                        }
                );
    }

    // =========================================================
    // UPDATE END DATE
    // =========================================================

    private void updateEndDate(
            ComboBox<PlanChoice> planCombo,
            DatePicker startDate,
            DatePicker endDate
    ) {

        PlanChoice selectedPlan =
                planCombo.getValue();

        LocalDate start =
                startDate.getValue();

        if (selectedPlan == null ||
                selectedPlan.durationDays() == null ||
                start == null) {

            return;
        }

        endDate.setValue(
                start.plusDays(
                        selectedPlan.durationDays() - 1L
                )
        );
    }

    // =========================================================
    // CREATE SUBSCRIPTION
    // =========================================================

    private void createSubscription(
            dto.SubscriptionRequest request
    ) {

        CompletableFuture
                .runAsync(
                        () -> {

                            try {

                                subscriptionAPI.create(
                                        request
                                );

                            } catch (
                                    IOException |
                                    InterruptedException e
                            ) {

                                if (e instanceof
                                        InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                throw new RuntimeException(
                                        e
                                );
                            }
                        }
                )
                .thenRun(
                        () ->
                                Platform.runLater(
                                        () -> {

                                            showInfo(
                                                    "نجاح",
                                                    "تم إنشاء الاشتراك بنجاح."
                                            );

                                            loadData();
                                        }
                                )
                )
                .exceptionally(
                        ex -> {

                            Platform.runLater(
                                    () ->
                                            showError(
                                                    "فشل إنشاء الاشتراك",
                                                    getRootCauseMessage(
                                                            ex
                                                    )
                                            )
                            );

                            return null;
                        }
                );
    }

    // =========================================================
    // ACTIVATE
    // =========================================================

    private void activateSelected() {

        SubscriptionRow selected =
                getSelectedSubscription();

        if (selected == null) {
            return;
        }

        if (!confirm(
                "تفعيل الاشتراك",
                "هل تريد تفعيل الاشتراك المحدد؟"
        )) {

            return;
        }

        Long adminUserId =
                AdminSession.getAdminUserId();

        if (adminUserId == null) {

            showError(
                    "الجلسة",
                    "لا يمكن تحديد المستخدم الإداري الحالي."
            );

            return;
        }

        SubscriptionActivationRequest request =
                new SubscriptionActivationRequest();

        request.setSubscriptionId(
                selected.getSubscriptionId()
        );

        request.setAdminUserId(
                adminUserId
        );

        request.setNewStatus(
                "ACTIVE"
        );

        request.setNotes(
                "تم التفعيل من تطبيق FAS Admin"
        );

        changeSubscriptionStatus(
                request
        );
    }

    // =========================================================
    // SUSPEND
    // =========================================================

    private void suspendSelected() {

        SubscriptionRow selected =
                getSelectedSubscription();

        if (selected == null) {
            return;
        }

        if (!confirm(
                "إيقاف الاشتراك",
                "هل تريد إيقاف الاشتراك المحدد؟"
        )) {

            return;
        }

        Long adminUserId =
                AdminSession.getAdminUserId();

        if (adminUserId == null) {

            showError(
                    "الجلسة",
                    "لا يمكن تحديد المستخدم الإداري الحالي."
            );

            return;
        }

        SubscriptionActivationRequest request =
                new SubscriptionActivationRequest();

        request.setSubscriptionId(
                selected.getSubscriptionId()
        );

        request.setAdminUserId(
                adminUserId
        );

        request.setNewStatus(
                "SUSPENDED"
        );

        request.setNotes(
                "تم إيقاف الاشتراك من تطبيق FAS Admin"
        );

        changeSubscriptionStatus(
                request
        );
    }

    // =========================================================
    // CHANGE STATUS
    // =========================================================

    private void changeSubscriptionStatus(
            SubscriptionActivationRequest request
    ) {

        CompletableFuture
                .runAsync(
                        () -> {

                            try {

                                activationAPI.changeStatus(
                                        request
                                );

                            } catch (
                                    IOException |
                                    InterruptedException e
                            ) {

                                if (e instanceof
                                        InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                throw new RuntimeException(
                                        e
                                );
                            }
                        }
                )
                .thenRun(
                        () ->
                                Platform.runLater(
                                        () -> {

                                            showInfo(
                                                    "نجاح",
                                                    "تم تغيير حالة الاشتراك."
                                            );

                                            loadData();
                                        }
                                )
                )
                .exceptionally(
                        ex -> {

                            Platform.runLater(
                                    () ->
                                            showError(
                                                    "فشل تغيير الحالة",
                                                    getRootCauseMessage(
                                                            ex
                                                    )
                                            )
                            );

                            return null;
                        }
                );
    }

    // =========================================================
    // RENEW
    // =========================================================

    private void renewSelected() {

        SubscriptionRow selected =
                getSelectedSubscription();

        if (selected == null) {
            return;
        }

        PlanChoice plan =
                findPlan(
                        selected.getPlanId()
                );

        if (plan == null) {

            showError(
                    "الخطة غير موجودة",
                    "تعذر العثور على الخطة الحالية."
            );

            return;
        }

        LocalDate newStart =
                selected.getEndDate() != null
                        ? selected.getEndDate()
                        .plusDays(1)
                        : LocalDate.now();

        LocalDate newEnd =
                plan.durationDays() == null
                        ? newStart
                        : newStart.plusDays(
                        plan.durationDays() - 1L
                );

        if (!confirm(
                "تجديد الاشتراك",
                "سيتم التجديد من "
                        + formatDate(newStart)
                        + " إلى "
                        + formatDate(newEnd)
                        + "."
        )) {

            return;
        }

        dto.SubscriptionRequest request =
                new dto.SubscriptionRequest();

        request.setClinicId(
                selected.getClinicId()
        );

        request.setPlanId(
                selected.getPlanId()
        );

        request.setStartDate(
                newStart
        );

        request.setEndDate(
                newEnd
        );

        request.setStatus(
                "ACTIVE"
        );

        CompletableFuture
                .runAsync(
                        () -> {

                            try {

                                subscriptionAPI.update(
                                        selected.getSubscriptionId(),
                                        request
                                );

                            } catch (
                                    IOException |
                                    InterruptedException e
                            ) {

                                if (e instanceof
                                        InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                throw new RuntimeException(
                                        e
                                );
                            }
                        }
                )
                .thenRun(
                        () ->
                                Platform.runLater(
                                        () -> {

                                            showInfo(
                                                    "نجاح",
                                                    "تم تجديد الاشتراك."
                                            );

                                            loadData();
                                        }
                                )
                )
                .exceptionally(
                        ex -> {

                            Platform.runLater(
                                    () ->
                                            showError(
                                                    "فشل التجديد",
                                                    getRootCauseMessage(
                                                            ex
                                                    )
                                            )
                            );

                            return null;
                        }
                );
    }

    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    private void approveRequest(
            SubscriptionRequestRow row
    ) {

        if (row == null ||
                row.getRequestId() == null) {

            return;
        }

        if (!"PENDING".equalsIgnoreCase(
                row.getRawStatus()
        )) {

            showInfo(
                    "الطلب",
                    "هذا الطلب تمت مراجعته مسبقًا."
            );

            return;
        }

        showReviewDialog(
                row,
                true
        );
    }

    // =========================================================
    // REJECT REQUEST
    // =========================================================

    private void rejectRequest(
            SubscriptionRequestRow row
    ) {

        if (row == null ||
                row.getRequestId() == null) {

            return;
        }

        if (!"PENDING".equalsIgnoreCase(
                row.getRawStatus()
        )) {

            showInfo(
                    "الطلب",
                    "هذا الطلب تمت مراجعته مسبقًا."
            );

            return;
        }

        showReviewDialog(
                row,
                false
        );
    }

    // =========================================================
    // REVIEW DIALOG
    // =========================================================

    private void showReviewDialog(
            SubscriptionRequestRow row,
            boolean approve
    ) {

        Long requestId =
                row.getRequestId();

        if (requestId == null) {
            return;
        }

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                approve
                        ? "الموافقة على طلب الاشتراك"
                        : "رفض طلب الاشتراك"
        );

        dialog.setHeaderText(
                approve
                        ? "مراجعة طلب الاشتراك"
                        : "رفض طلب الاشتراك"
        );

        VBox content =
                new VBox(
                        12
                );

        content.setPadding(
                new Insets(20)
        );

        content.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        Label clinic =
                new Label(
                        "العيادة: "
                                + safe(
                                row.getClinicName()
                        )
                );

        Label plan =
                new Label(
                        "الخطة: "
                                + safe(
                                row.getPlanName()
                        )
                );

        Label requestType =
                new Label(
                        "النوع: "
                                + safe(
                                row.getTypeValue()
                        )
                );

        Label ownerNotes =
                new Label(
                        "ملاحظات المالك: "
                                + safe(
                                row.getOwnerNotesValue()
                        )
                );

        ownerNotes.setWrapText(
                true
        );

        TextArea adminNotes =
                new TextArea();

        adminNotes.setPromptText(
                approve
                        ? "ملاحظات الموافقة (اختياري)"
                        : "سبب الرفض (مطلوب)"
        );

        adminNotes.setWrapText(
                true
        );

        adminNotes.setPrefRowCount(
                6
        );

        content.getChildren().addAll(
                clinic,
                plan,
                requestType,
                ownerNotes,
                new Separator(),
                new Label(
                        "ملاحظات مدير النظام"
                ),
                adminNotes
        );

        dialog.getDialogPane()
                .setContent(
                        content
                );

        dialog.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        ButtonType confirmButton =
                new ButtonType(
                        approve
                                ? "موافقة"
                                : "رفض",
                        ButtonBar.ButtonData.OK_DONE
                );

        ButtonType cancelButton =
                new ButtonType(
                        "إلغاء",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .setAll(
                        confirmButton,
                        cancelButton
                );

        dialog.showAndWait()
                .ifPresent(
                        result -> {

                            if (result != confirmButton) {
                                return;
                            }

                            String notes =
                                    adminNotes
                                            .getText()
                                            .trim();

                            if (!approve &&
                                    notes.isBlank()) {

                                showError(
                                        "سبب الرفض مطلوب",
                                        "يجب إدخال سبب رفض الطلب."
                                );

                                return;
                            }

                            if (!confirm(
                                    approve
                                            ? "تأكيد الموافقة"
                                            : "تأكيد الرفض",
                                    approve
                                            ? "هل تريد الموافقة على هذا الطلب؟"
                                            : "هل تريد رفض هذا الطلب؟"
                            )) {

                                return;
                            }

                            if (approve) {

                                approveRequestAsync(
                                        requestId,
                                        notes
                                );

                            } else {

                                rejectRequestAsync(
                                        requestId,
                                        notes
                                );
                            }
                        }
                );
    }

    // =========================================================
    // APPROVE ASYNC
    // =========================================================

    private void approveRequestAsync(
            Long requestId,
            String adminNotes
    ) {

        CompletableFuture
                .supplyAsync(
                        () -> {

                            try {

                                return subscriptionRequestAPI
                                        .approve(
                                                requestId,
                                                adminNotes
                                        );

                            } catch (
                                    IOException |
                                    InterruptedException e
                            ) {

                                if (e instanceof
                                        InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                throw new RuntimeException(
                                        e
                                );
                            }
                        }
                )
                .thenAccept(
                        response ->
                                Platform.runLater(
                                        () -> {

                                            showInfo(
                                                    "تمت الموافقة",
                                                    "تمت الموافقة على طلب الاشتراك بنجاح."
                                            );

                                            loadData();
                                        }
                                )
                )
                .exceptionally(
                        ex -> {

                            Platform.runLater(
                                    () ->
                                            showError(
                                                    "فشل الموافقة",
                                                    getRootCauseMessage(
                                                            ex
                                                    )
                                            )
                            );

                            return null;
                        }
                );
    }

    // =========================================================
    // REJECT ASYNC
    // =========================================================

    private void rejectRequestAsync(
            Long requestId,
            String adminNotes
    ) {

        CompletableFuture
                .supplyAsync(
                        () -> {

                            try {

                                return subscriptionRequestAPI
                                        .reject(
                                                requestId,
                                                adminNotes
                                        );

                            } catch (
                                    IOException |
                                    InterruptedException e
                            ) {

                                if (e instanceof
                                        InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                throw new RuntimeException(
                                        e
                                );
                            }
                        }
                )
                .thenAccept(
                        response ->
                                Platform.runLater(
                                        () -> {

                                            showInfo(
                                                    "تم رفض الطلب",
                                                    "تم رفض طلب الاشتراك وحفظ ملاحظات المدير."
                                            );

                                            loadData();
                                        }
                                )
                )
                .exceptionally(
                        ex -> {

                            Platform.runLater(
                                    () ->
                                            showError(
                                                    "فشل الرفض",
                                                    getRootCauseMessage(
                                                            ex
                                                    )
                                            )
                            );

                            return null;
                        }
                );
    }

    // =========================================================
    // SELECTED SUBSCRIPTION
    // =========================================================

    private SubscriptionRow
    getSelectedSubscription() {

        SubscriptionRow selected =
                subscriptionTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showInfo(
                    "تحديد الاشتراك",
                    "اختر اشتراكًا أولًا."
            );
        }

        return selected;
    }

    // =========================================================
    // FIND PLAN
    // =========================================================

    private PlanChoice findPlan(
            Long planId
    ) {

        if (planId == null) {
            return null;
        }

        for (
                SubscriptionPlanResponse plan :
                plans
        ) {

            if (plan.getPlanId() != null &&
                    plan.getPlanId()
                            .equals(planId)) {

                return new PlanChoice(
                        plan.getPlanId(),
                        plan.getPlanName(),
                        plan.getDurationDays()
                );
            }
        }

        return null;
    }

    // =========================================================
    // TOOLBAR BUTTON
    // =========================================================

    private Button createToolbarButton(
            String text
    ) {

        Button button =
                new Button(
                        text
                );

        button.setPadding(
                new Insets(
                        8,
                        14,
                        8,
                        14
                )
        );

        button.setStyle("""
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """);

        return button;
    }

    // =========================================================
    // CONFIRM
    // =========================================================

    private boolean confirm(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        return alert.showAndWait()
                .filter(
                        ButtonType.OK::equals
                )
                .isPresent();
    }

    // =========================================================
    // INFO
    // =========================================================

    private void showInfo(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        alert.showAndWait();
    }

    // =========================================================
    // ERROR
    // =========================================================

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message == null ||
                        message.isBlank()
                        ? "حدث خطأ غير معروف."
                        : message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        alert.showAndWait();
    }

    // =========================================================
    // ROOT CAUSE
    // =========================================================

    private String getRootCauseMessage(
            Throwable throwable
    ) {

        if (throwable == null) {

            return "حدث خطأ غير معروف.";
        }

        Throwable cause =
                throwable;

        while (
                cause.getCause() != null
        ) {

            cause =
                    cause.getCause();
        }

        String message =
                cause.getMessage();

        return message == null ||
                message.isBlank()
                ? "حدث خطأ غير معروف."
                : message;
    }

    // =========================================================
    // SAFE
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    // =========================================================
    // FORMAT DATE
    // =========================================================

    private String formatDate(
            LocalDate date
    ) {

        if (date == null) {
            return "";
        }

        return DATE_FORMAT.format(
                date
        );
    }

    // =========================================================
    // LOADED DATA
    // =========================================================

    private record LoadedData(
            List<ClinicResponse> clinics,
            List<SubscriptionPlanResponse> plans,
            List<SubscriptionResponse> subscriptions,
            List<SubscriptionRequestResponse> requests
    ) {
    }

    // =========================================================
    // CLINIC CHOICE
    // =========================================================

    private record ClinicChoice(
            Long clinicId,
            String clinicName
    ) {

        @Override
        public String toString() {

            return clinicName;
        }
    }

    // =========================================================
    // PLAN CHOICE
    // =========================================================

    private record PlanChoice(
            Long planId,
            String planName,
            Integer durationDays
    ) {

        @Override
        public String toString() {

            return planName;
        }
    }
}

// =============================================================
// SUBSCRIPTION ROW
// =============================================================

class SubscriptionRow {

    private Long subscriptionId;
    private Long clinicId;
    private Long planId;

    private LocalDate startDate;
    private LocalDate endDate;

    private String rawStatus;

    private final StringProperty clinic =
            new SimpleStringProperty();

    private final StringProperty plan =
            new SimpleStringProperty();

    private final StringProperty start =
            new SimpleStringProperty();

    private final StringProperty end =
            new SimpleStringProperty();

    private final StringProperty status =
            new SimpleStringProperty();

    public static SubscriptionRow fromResponse(
            SubscriptionResponse response
    ) {

        SubscriptionRow row =
                new SubscriptionRow();

        row.subscriptionId =
                response.getSubscriptionId();

        row.clinicId =
                response.getClinicId();

        row.planId =
                response.getPlanId();

        row.startDate =
                response.getStartDate();

        row.endDate =
                response.getEndDate();

        row.rawStatus =
                response.getStatus();

        row.clinic.set(
                safe(
                        response.getClinicName()
                )
        );

        row.plan.set(
                safe(
                        response.getPlanName()
                )
        );

        row.start.set(
                row.startDate == null
                        ? ""
                        : DateTimeFormatter
                        .ofPattern("dd/MM/yyyy")
                        .format(
                                row.startDate
                        )
        );

        row.end.set(
                row.endDate == null
                        ? ""
                        : DateTimeFormatter
                        .ofPattern("dd/MM/yyyy")
                        .format(
                                row.endDate
                        )
        );

        row.status.set(
                translateStatus(
                        row.rawStatus
                )
        );

        return row;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public Long getPlanId() {
        return planId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return rawStatus;
    }

    public StringProperty clinicProperty() {
        return clinic;
    }

    public StringProperty planProperty() {
        return plan;
    }

    public StringProperty startProperty() {
        return start;
    }

    public StringProperty endProperty() {
        return end;
    }

    public StringProperty statusProperty() {
        return status;
    }

    private static String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    private static String translateStatus(
            String status
    ) {

        if (status == null) {
            return "";
        }

        return switch (
                status.toUpperCase()
                ) {

            case "ACTIVE" ->
                    "نشط";

            case "PENDING" ->
                    "قيد الانتظار";

            case "EXPIRED" ->
                    "منتهي";

            case "SUSPENDED" ->
                    "موقوف";

            case "CANCELLED" ->
                    "ملغى";

            default ->
                    status;
        };
    }
}

// =============================================================
// SUBSCRIPTION REQUEST ROW
// =============================================================

class SubscriptionRequestRow {

    private Long requestId;
    private Long clinicId;
    private Long planId;

    private String rawStatus;

    private String clinicName;
    private String planName;
    private String typeValue;
    private String ownerNotesValue;

    private final StringProperty clinic =
            new SimpleStringProperty();

    private final StringProperty plan =
            new SimpleStringProperty();

    private final StringProperty type =
            new SimpleStringProperty();

    private final StringProperty date =
            new SimpleStringProperty();

    private final StringProperty status =
            new SimpleStringProperty();

    private final StringProperty ownerNotes =
            new SimpleStringProperty();

    private final StringProperty adminNotes =
            new SimpleStringProperty();

    public static SubscriptionRequestRow fromResponse(
            SubscriptionRequestResponse response,
            List<ClinicResponse> clinics,
            List<SubscriptionPlanResponse> plans
    ) {

        SubscriptionRequestRow row =
                new SubscriptionRequestRow();

        row.requestId =
                response.getRequestId();

        row.clinicId =
                response.getClinicId();

        row.planId =
                response.getPlanId();

        row.rawStatus =
                response.getStatus();

        row.clinicName =
                findClinicName(
                        row.clinicId,
                        clinics
                );

        row.planName =
                findPlanName(
                        row.planId,
                        plans
                );

        row.typeValue =
                translateRequestType(
                        response.getRequestType()
                );

        row.ownerNotesValue =
                safe(
                        response.getOwnerNotes()
                );

        row.clinic.set(
                row.clinicName
        );

        row.plan.set(
                row.planName
        );

        row.type.set(
                row.typeValue
        );

        row.date.set(
                formatDateTime(
                        response.getRequestedAt()
                )
        );

        row.status.set(
                translateRequestStatus(
                        row.rawStatus
                )
        );

        row.ownerNotes.set(
                row.ownerNotesValue
        );

        row.adminNotes.set(
                safe(
                        response.getAdminNotes()
                )
        );

        return row;
    }

    public Long getRequestId() {
        return requestId;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public Long getPlanId() {
        return planId;
    }

    public String getRawStatus() {
        return rawStatus;
    }

    public String getClinicName() {
        return clinicName;
    }

    public String getPlanName() {
        return planName;
    }

    public String getTypeValue() {
        return typeValue;
    }

    public String getOwnerNotesValue() {
        return ownerNotesValue;
    }

    public StringProperty clinicProperty() {
        return clinic;
    }

    public StringProperty planProperty() {
        return plan;
    }

    public StringProperty typeProperty() {
        return type;
    }

    public StringProperty dateProperty() {
        return date;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty ownerNotesProperty() {
        return ownerNotes;
    }

    public StringProperty adminNotesProperty() {
        return adminNotes;
    }

    private static String findClinicName(
            Long clinicId,
            List<ClinicResponse> clinics
    ) {

        if (clinicId == null ||
                clinics == null) {

            return "";
        }

        for (
                ClinicResponse clinic :
                clinics
        ) {

            if (clinic.getClinicId() != null &&
                    clinic.getClinicId()
                            .equals(clinicId)) {

                return safe(
                        clinic.getClinicName()
                );
            }
        }

        return String.valueOf(
                clinicId
        );
    }

    private static String findPlanName(
            Long planId,
            List<SubscriptionPlanResponse> plans
    ) {

        if (planId == null ||
                plans == null) {

            return "";
        }

        for (
                SubscriptionPlanResponse plan :
                plans
        ) {

            if (plan.getPlanId() != null &&
                    plan.getPlanId()
                            .equals(planId)) {

                return safe(
                        plan.getPlanName()
                );
            }
        }

        return String.valueOf(
                planId
        );
    }

    private static String translateRequestType(
            String type
    ) {

        if (type == null) {
            return "";
        }

        return switch (
                type.toUpperCase()
                ) {

            case "NEW" ->
                    "اشتراك جديد";

            case "RENEW" ->
                    "تجديد";

            default ->
                    type;
        };
    }

    private static String translateRequestStatus(
            String status
    ) {

        if (status == null) {
            return "";
        }

        return switch (
                status.toUpperCase()
                ) {

            case "PENDING" ->
                    "قيد المراجعة";

            case "APPROVED" ->
                    "مقبول";

            case "REJECTED" ->
                    "مرفوض";

            case "CANCELLED" ->
                    "ملغى";

            default ->
                    status;
        };
    }

    private static String formatDateTime(
            LocalDateTime dateTime
    ) {

        if (dateTime == null) {
            return "";
        }

        return DateTimeFormatter
                .ofPattern(
                        "dd/MM/yyyy HH:mm"
                )
                .format(
                        dateTime
                );
    }

    private static String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
}