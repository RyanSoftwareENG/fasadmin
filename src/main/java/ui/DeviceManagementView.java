package ui;

import api.AdminApiManager;
import api.DeviceAPI;
import dto.DeviceResponse;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DeviceManagementView extends BorderPane {

    private final DeviceAPI deviceAPI;

    private final ObservableList<DeviceResponse> devices =
            FXCollections.observableArrayList();

    private final TableView<DeviceResponse> table =
            new TableView<>();

    private final Label totalLabel =
            new Label("0");

    private final Label activeLabel =
            new Label("0");

    private final Label blockedLabel =
            new Label("0");

    private final Label suspendedLabel =
            new Label("0");

    private final Label statusLabel =
            new Label();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm"
            );

    public DeviceManagementView() {

        deviceAPI =
                AdminApiManager
                        .getInstance()
                        .getDeviceAPI();

        setPadding(
                new Insets(25)
        );

        setStyle(
                "-fx-background-color: #f5f7fa;"
        );

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setTop(
                createHeader()
        );

        setCenter(
                createContent()
        );

        loadDevices();
    }

    // =====================================================
    // Header
    // =====================================================

    private Node createHeader() {

        Label title =
                new Label("إدارة الأجهزة");

        title.setStyle("""
            -fx-font-size: 28px;
            -fx-font-weight: bold;
        """);

        Label subtitle =
                new Label(
                        "إدارة الأجهزة المسجلة ومتابعة حالتها واتصالها"
                );

        subtitle.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #6b7280;
        """);

        VBox text =
                new VBox(
                        5,
                        title,
                        subtitle
                );

        Button refreshButton =
                new Button("تحديث");

        refreshButton.setStyle("""
            -fx-font-size: 14px;
            -fx-padding: 9px 18px;
            -fx-background-color: #2563eb;
            -fx-text-fill: white;
            -fx-background-radius: 7px;
            -fx-cursor: hand;
        """);

        refreshButton.setOnAction(
                event -> loadDevices()
        );

        Button activationButton =
                new Button("إنشاء رمز تفعيل");

        activationButton.setStyle("""
            -fx-font-size: 14px;
            -fx-padding: 9px 18px;
            -fx-background-color: #16a34a;
            -fx-text-fill: white;
            -fx-background-radius: 7px;
            -fx-cursor: hand;
        """);

        activationButton.setOnAction(
                event -> showActivationMessage()
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        HBox actions =
                new HBox(
                        10,
                        refreshButton,
                        activationButton
                );

        actions.setAlignment(
                Pos.CENTER
        );

        HBox header =
                new HBox(
                        20,
                        text,
                        spacer,
                        actions
                );

        header.setAlignment(
                Pos.CENTER_RIGHT
        );

        header.setPadding(
                new Insets(0, 0, 25, 0)
        );

        return header;
    }

    // =====================================================
    // Content
    // =====================================================

    private Node createContent() {

        VBox content =
                new VBox(
                        20
                );

        content.getChildren().addAll(
                createSummaryCards(),
                createTableSection()
        );

        VBox.setVgrow(
                content.getChildren().get(1),
                Priority.ALWAYS
        );

        return content;
    }

    // =====================================================
    // Summary Cards
    // =====================================================

    private Node createSummaryCards() {

        HBox cards =
                new HBox(
                        15
                );

        cards.setFillHeight(
                true
        );

        VBox totalCard =
                createSummaryCard(
                        "إجمالي الأجهزة",
                        totalLabel,
                        "جميع الأجهزة المسجلة"
                );

        VBox activeCard =
                createSummaryCard(
                        "الأجهزة النشطة",
                        activeLabel,
                        "يمكنها الاتصال بالنظام"
                );

        VBox blockedCard =
                createSummaryCard(
                        "الأجهزة المحظورة",
                        blockedLabel,
                        "لا يسمح لها بالاتصال"
                );

        VBox suspendedCard =
                createSummaryCard(
                        "الأجهزة الموقوفة",
                        suspendedLabel,
                        "متوقفة مؤقتًا"
                );

        cards.getChildren().addAll(
                totalCard,
                activeCard,
                blockedCard,
                suspendedCard
        );

        for (Node card : cards.getChildren()) {
            HBox.setHgrow(
                    card,
                    Priority.ALWAYS
            );
        }

        return cards;
    }

    private VBox createSummaryCard(
            String title,
            Label value,
            String description
    ) {

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #6b7280;
        """);

        value.setStyle("""
            -fx-font-size: 28px;
            -fx-font-weight: bold;
        """);

        Label descriptionLabel =
                new Label(description);

        descriptionLabel.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #9ca3af;
        """);

        VBox box =
                new VBox(
                        8,
                        titleLabel,
                        value,
                        descriptionLabel
                );

        box.setPadding(
                new Insets(18)
        );

        box.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10px;
            -fx-border-color: #e5e7eb;
            -fx-border-radius: 10px;
        """);

        return box;
    }

    // =====================================================
    // Table Section
    // =====================================================

    private Node createTableSection() {

        Label title =
                new Label("الأجهزة المسجلة");

        title.setStyle("""
            -fx-font-size: 20px;
            -fx-font-weight: bold;
        """);

        Label hint =
                new Label(
                        "يمكنك متابعة حالة كل جهاز وتنفيذ الإجراءات المتاحة."
                );

        hint.setStyle("""
            -fx-font-size: 13px;
            -fx-text-fill: #6b7280;
        """);

        VBox heading =
                new VBox(
                        5,
                        title,
                        hint
                );

        configureTable();

        statusLabel.setStyle("""
            -fx-font-size: 13px;
            -fx-text-fill: #6b7280;
        """);

        HBox bottom =
                new HBox(
                        statusLabel
                );

        bottom.setAlignment(
                Pos.CENTER_RIGHT
        );

        VBox section =
                new VBox(
                        15,
                        heading,
                        table,
                        bottom
                );

        section.setPadding(
                new Insets(20)
        );

        section.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10px;
            -fx-border-color: #e5e7eb;
            -fx-border-radius: 10px;
        """);

        VBox.setVgrow(
                table,
                Priority.ALWAYS
        );

        return section;
    }

    // =====================================================
    // Table
    // =====================================================

    private void configureTable() {

        table.setItems(
                devices
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

        table.setPlaceholder(
                new Label("لا توجد أجهزة مسجلة.")
        );

        TableColumn<DeviceResponse, String>
                nameColumn =
                new TableColumn<>("الجهاز");

        nameColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                safe(
                                        cell.getValue()
                                                .getDeviceName()
                                )
                        )
        );

        TableColumn<DeviceResponse, String>
                clinicColumn =
                new TableColumn<>("العيادة");

        clinicColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cell.getValue()
                                                .getClinicId()
                                )
                        )
        );

        TableColumn<DeviceResponse, String>
                installationColumn =
                new TableColumn<>("Installation ID");

        installationColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                safe(
                                        cell.getValue()
                                                .getInstallationId()
                                )
                        )
        );

        TableColumn<DeviceResponse, String>
                statusColumn =
                new TableColumn<>("الحالة");

        statusColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                translateStatus(
                                        cell.getValue()
                                                .getStatus()
                                )
                        )
        );

        statusColumn.setCellFactory(
                column ->
                        new TableCell<>() {

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

                                    setText(null);
                                    setStyle("");

                                    return;
                                }

                                setText(item);

                                DeviceResponse device =
                                        getTableView()
                                                .getItems()
                                                .get(
                                                        getIndex()
                                                );

                                setStyle(
                                        statusStyle(
                                                device.getStatus()
                                        )
                                );
                            }
                        }
        );

        TableColumn<DeviceResponse, String>
                firstSeenColumn =
                new TableColumn<>("أول ظهور");

        firstSeenColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                formatDate(
                                        cell.getValue()
                                                .getFirstSeenAt()
                                )
                        )
        );

        TableColumn<DeviceResponse, String>
                lastSeenColumn =
                new TableColumn<>("آخر اتصال");

        lastSeenColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                formatDate(
                                        cell.getValue()
                                                .getLastSeenAt()
                                )
                        )
        );

        TableColumn<DeviceResponse, Void>
                actionsColumn =
                new TableColumn<>("الإجراءات");

        actionsColumn.setCellFactory(
                column ->
                        new TableCell<>() {

                            private final Button details =
                                    new Button("تفاصيل");

                            private final Button status =
                                    new Button("الحالة");

                            private final Button delete =
                                    new Button("حذف");

                            private final HBox box =
                                    new HBox(
                                            6,
                                            details,
                                            status,
                                            delete
                                    );

                            {
                                box.setAlignment(
                                        Pos.CENTER
                                );

                                details.setOnAction(
                                        event -> {

                                            DeviceResponse device =
                                                    getTableView()
                                                            .getItems()
                                                            .get(
                                                                    getIndex()
                                                            );

                                            showDetails(
                                                    device
                                            );
                                        }
                                );

                                status.setOnAction(
                                        event -> {

                                            DeviceResponse device =
                                                    getTableView()
                                                            .getItems()
                                                            .get(
                                                                    getIndex()
                                                            );

                                            showStatusMenu(
                                                    device
                                            );
                                        }
                                );

                                delete.setOnAction(
                                        event -> {

                                            DeviceResponse device =
                                                    getTableView()
                                                            .getItems()
                                                            .get(
                                                                    getIndex()
                                                            );

                                            confirmDelete(
                                                    device
                                            );
                                        }
                                );
                            }

                            @Override
                            protected void updateItem(
                                    Void item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty) {

                                    setGraphic(null);

                                } else {

                                    setGraphic(box);
                                }
                            }
                        }
        );

        table.getColumns().addAll(
                nameColumn,
                clinicColumn,
                installationColumn,
                statusColumn,
                firstSeenColumn,
                lastSeenColumn,
                actionsColumn
        );
    }

    // =====================================================
    // Load
    // =====================================================

    private void loadDevices() {

        statusLabel.setText(
                "جاري تحميل الأجهزة..."
        );

        Thread thread =
                new Thread(() -> {

                    try {

                        List<DeviceResponse> result =
                                deviceAPI.getAllDevices();

                        Platform.runLater(() -> {

                            devices.setAll(
                                    result
                            );

                            updateSummary(
                                    result
                            );

                            statusLabel.setText(
                                    "تم تحميل "
                                            + result.size()
                                            + " جهاز."
                            );
                        });

                    } catch (Exception e) {

                        Platform.runLater(() -> {

                            statusLabel.setText(
                                    "تعذر تحميل الأجهزة."
                            );

                            showError(
                                    "فشل تحميل الأجهزة",
                                    e.getMessage()
                            );
                        });
                    }

                });

        thread.setDaemon(
                true
        );

        thread.start();
    }

    // =====================================================
    // Summary
    // =====================================================

    private void updateSummary(
            List<DeviceResponse> list
    ) {

        long active =
                list.stream()
                        .filter(
                                d ->
                                        "ACTIVE".equalsIgnoreCase(
                                                d.getStatus()
                                        )
                        )
                        .count();

        long blocked =
                list.stream()
                        .filter(
                                d ->
                                        "BLOCKED".equalsIgnoreCase(
                                                d.getStatus()
                                        )
                        )
                        .count();

        long REVOKED =
                list.stream()
                        .filter(
                                d ->
                                        "REVOKED".equalsIgnoreCase(
                                                d.getStatus()
                                        )
                        )
                        .count();

        totalLabel.setText(
                String.valueOf(
                        list.size()
                )
        );

        activeLabel.setText(
                String.valueOf(
                        active
                )
        );

        blockedLabel.setText(
                String.valueOf(
                        blocked
                )
        );

        suspendedLabel.setText(
                String.valueOf(
                        REVOKED
                )
        );
    }

    // =====================================================
    // Details
    // =====================================================

    private void showDetails(
            DeviceResponse device
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "تفاصيل الجهاز"
        );

        alert.setHeaderText(
                safe(device.getDeviceName())
        );

        String details =
                "معرف الجهاز: "
                        + device.getDeviceId()
                        + "\n\n"
                        + "معرف العيادة: "
                        + device.getClinicId()
                        + "\n\n"
                        + "Installation ID:\n"
                        + safe(
                        device.getInstallationId()
                )
                        + "\n\n"
                        + "الحالة: "
                        + translateStatus(
                        device.getStatus()
                )
                        + "\n\n"
                        + "أول ظهور: "
                        + formatDate(
                        device.getFirstSeenAt()
                )
                        + "\n\n"
                        + "آخر اتصال: "
                        + formatDate(
                        device.getLastSeenAt()
                );

        alert.setContentText(
                details
        );

        alert.showAndWait();
    }

    // =====================================================
    // Status
    // =====================================================

    private void showStatusMenu(
            DeviceResponse device
    ) {

        ChoiceDialog<String> dialog =
                new ChoiceDialog<>(
                        translateStatus(
                                device.getStatus()
                        ),
                        "نشط",
                        "محظور",
                        "موقوف"
                );

        dialog.setTitle(
                "تغيير حالة الجهاز"
        );

        dialog.setHeaderText(
                safe(
                        device.getDeviceName()
                )
        );

        dialog.setContentText(
                "اختر الحالة الجديدة:"
        );

        dialog.showAndWait()
                .ifPresent(
                        selected ->
                                changeDeviceStatus(
                                        device,
                                        selected
                                )
                );
    }

    private void changeDeviceStatus(
            DeviceResponse device,
            String selected
    ) {

        String status;

        switch (selected) {

            case "نشط":
                status = "ACTIVE";
                break;

            case "محظور":
                status = "BLOCKED";
                break;

            case "موقوف":
                status = "REVOKED";
                break;

            default:
                return;
        }

        statusLabel.setText(
                "جاري تغيير حالة الجهاز..."
        );

        Thread thread =
                new Thread(() -> {

                    try {

                        DeviceResponse updated =
                                deviceAPI.changeStatus(
                                        device.getDeviceId(),
                                        status
                                );

                        Platform.runLater(() -> {

                            int index =
                                    devices.indexOf(
                                            device
                                    );

                            if (index >= 0) {

                                devices.set(
                                        index,
                                        updated
                                );
                            }

                            updateSummary(
                                    devices
                            );

                            table.refresh();

                            statusLabel.setText(
                                    "تم تغيير حالة الجهاز."
                            );
                        });

                    } catch (Exception e) {

                        Platform.runLater(() ->
                                showError(
                                        "فشل تغيير الحالة",
                                        e.getMessage()
                                )
                        );
                    }

                });

        thread.setDaemon(
                true
        );

        thread.start();
    }

    // =====================================================
    // Delete
    // =====================================================

    private void confirmDelete(
            DeviceResponse device
    ) {

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "حذف الجهاز"
        );

        confirmation.setHeaderText(
                "هل تريد حذف هذا الجهاز؟"
        );

        confirmation.setContentText(
                "الجهاز: "
                        + safe(
                        device.getDeviceName()
                )
                        + "\n\n"
                        + "Installation ID:\n"
                        + safe(
                        device.getInstallationId()
                )
        );

        confirmation.showAndWait()
                .filter(
                        result ->
                                result ==
                                        ButtonType.OK
                )
                .ifPresent(
                        result ->
                                deleteDevice(
                                        device
                                )
                );
    }

    private void deleteDevice(
            DeviceResponse device
    ) {

        statusLabel.setText(
                "جاري حذف الجهاز..."
        );

        Thread thread =
                new Thread(() -> {

                    try {

                        deviceAPI.deleteDevice(
                                device.getDeviceId()
                        );

                        Platform.runLater(() -> {

                            devices.remove(
                                    device
                            );

                            updateSummary(
                                    devices
                            );

                            statusLabel.setText(
                                    "تم حذف الجهاز."
                            );
                        });

                    } catch (Exception e) {

                        Platform.runLater(() ->
                                showError(
                                        "فشل حذف الجهاز",
                                        e.getMessage()
                                )
                        );
                    }

                });

        thread.setDaemon(
                true
        );

        thread.start();
    }

    // =====================================================
    // Activation
    // =====================================================

    private void showActivationMessage() {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "إنشاء رمز تفعيل"
        );

        alert.setHeaderText(
                "إنشاء رمز تفعيل لجهاز جديد"
        );

        alert.setContentText(
                "سنربط هذا الزر لاحقًا مع نافذة إنشاء رمز التفعيل، "
                        + "بحيث يتم اختيار الاشتراك ثم إنشاء الرمز."
        );

        alert.showAndWait();
    }

    // =====================================================
    // Helpers
    // =====================================================

    private String translateStatus(
            String status
    ) {

        if (status == null) {
            return "غير معروف";
        }

        switch (status.toUpperCase()) {

            case "ACTIVE":
                return "نشط";

            case "BLOCKED":
                return "محظور";

            case "REVOKED":
                return "موقوف";

            default:
                return status;
        }
    }

    private String statusStyle(
            String status
    ) {

        if (status == null) {
            return "";
        }

        switch (status.toUpperCase()) {

            case "ACTIVE":
                return """
                    -fx-text-fill: #15803d;
                    -fx-font-weight: bold;
                """;

            case "BLOCKED":
                return """
                    -fx-text-fill: #dc2626;
                    -fx-font-weight: bold;
                """;

            case "REVOKED":
                return """
                    -fx-text-fill: #ca8a04;
                    -fx-font-weight: bold;
                """;

            default:
                return "";
        }
    }

    private String formatDate(
            LocalDateTime date
    ) {

        if (date == null) {
            return "لا يوجد";
        }

        return DATE_FORMATTER.format(
                date
        );
    }

    private String safe(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {

            return "غير محدد";
        }

        return value;
    }

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
                message == null
                        ? "حدث خطأ غير معروف."
                        : message
        );

        alert.showAndWait();
    }
}