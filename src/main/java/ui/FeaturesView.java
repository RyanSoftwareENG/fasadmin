package ui;

import api.AdminApiManager;
import api.FeatureAPI;
import dto.FeatureRequest;
import dto.FeatureResponse;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FeaturesView extends BorderPane {

    private final TableView<FeatureRow> table =
            new TableView<>();

    private final ObservableList<FeatureRow> rows =
            FXCollections.observableArrayList();

    private final FeatureAPI featureAPI;

    public FeaturesView() {

        AdminApiManager apiManager =
                AdminApiManager.getInstance();

        featureAPI =
                apiManager.getFeatureAPI();

        setPadding(new Insets(25));

        Label title =
                new Label("مزايا النظام");

        title.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
        """);

        Button add =
                new Button("➕ إضافة ميزة");

        Button edit =
                new Button("✏ تعديل");

        Button disable =
                new Button("⛔ تعطيل");

        Button activate =
                new Button("✅ تفعيل");

        Button refresh =
                new Button("🔃 تحديث");

        HBox toolbar =
                new HBox(
                        10,
                        add,
                        edit,
                        disable,
                        activate,
                        refresh
                );

        // =====================================================
        // الأعمدة
        // =====================================================

        TableColumn<FeatureRow, String> code =
                new TableColumn<>("الكود");

        TableColumn<FeatureRow, String> name =
                new TableColumn<>("الميزة");

        TableColumn<FeatureRow, String> description =
                new TableColumn<>("الوصف");

        TableColumn<FeatureRow, String> status =
                new TableColumn<>("الحالة");

        code.setPrefWidth(180);
        name.setPrefWidth(200);
        description.setPrefWidth(350);
        status.setPrefWidth(120);

        code.setCellValueFactory(
                data -> data.getValue().codeProperty()
        );

        name.setCellValueFactory(
                data -> data.getValue().nameProperty()
        );

        description.setCellValueFactory(
                data -> data.getValue().descriptionProperty()
        );

        status.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );

        table.getColumns().addAll(
                code,
                name,
                description,
                status
        );

        table.setItems(rows);

        // =====================================================
        // الأحداث
        // =====================================================

        add.setOnAction(e ->
                showAddFeatureDialog()
        );

        edit.setOnAction(e ->
                showEditFeatureDialog()
        );

        disable.setOnAction(e ->
                changeSelectedStatus("DISABLED")
        );

        activate.setOnAction(e ->
                changeSelectedStatus("ACTIVE")
        );

        refresh.setOnAction(e ->
                loadFeatures()
        );

        setTop(
                new VBox(
                        15,
                        title,
                        toolbar
                )
        );

        setCenter(table);

        loadFeatures();
    }

    // =========================================================
    // تحميل جميع المزايا
    // =========================================================

    private void loadFeatures() {

        CompletableFuture
                .supplyAsync(() -> {

                    try {
                        return featureAPI.getAll();

                    } catch (
                            IOException |
                            InterruptedException e
                    ) {

                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }

                        throw new RuntimeException(e);
                    }

                })
                .thenAccept(features ->
                        Platform.runLater(() -> {

                            rows.clear();

                            for (FeatureResponse feature
                                    : features) {

                                rows.add(
                                        FeatureRow.fromResponse(
                                                feature
                                        )
                                );
                            }
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "تعذر تحميل المزايا",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // إضافة ميزة
    // =========================================================

    private void showAddFeatureDialog() {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "إضافة ميزة"
        );

        VBox box =
                new VBox(12);

        box.setPadding(
                new Insets(15)
        );

        TextField code =
                new TextField();

        code.setPromptText(
                "رمز الميزة"
        );

        TextField name =
                new TextField();

        name.setPromptText(
                "اسم الميزة"
        );

        TextArea description =
                new TextArea();

        description.setPromptText(
                "وصف الميزة"
        );

        description.setPrefRowCount(4);

        box.getChildren().addAll(

                new Label("رمز الميزة"),
                code,

                new Label("اسم الميزة"),
                name,

                new Label("الوصف"),
                description
        );

        dialog.getDialogPane()
                .setContent(box);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        ButtonType.OK,
                        ButtonType.CANCEL
                );

        dialog.showAndWait()
                .ifPresent(result -> {

                    if (result != ButtonType.OK) {
                        return;
                    }

                    if (code.getText().isBlank()
                            || name.getText().isBlank()) {

                        showError(
                                "بيانات غير مكتملة",
                                "رمز الميزة واسم الميزة مطلوبان."
                        );

                        return;
                    }

                    FeatureRequest request =
                            new FeatureRequest();

                    request.setFeatureCode(
                            code.getText().trim()
                    );

                    request.setFeatureName(
                            name.getText().trim()
                    );

                    request.setDescription(
                            clean(description.getText())
                    );

                    request.setStatus(
                            "ACTIVE"
                    );

                    createFeature(request);
                });
    }

    private void createFeature(
            FeatureRequest request
    ) {

        CompletableFuture
                .runAsync(() -> {

                    try {

                        featureAPI.create(
                                request
                        );

                    } catch (
                            IOException |
                            InterruptedException e
                    ) {

                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }

                        throw new RuntimeException(e);
                    }

                })
                .thenRun(() ->
                        Platform.runLater(() -> {

                            showInfo(
                                    "نجاح",
                                    "تم إنشاء الميزة بنجاح."
                            );

                            loadFeatures();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل إنشاء الميزة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // تعديل ميزة
    // =========================================================

    private void showEditFeatureDialog() {

        FeatureRow selected =
                getSelectedFeature();

        if (selected == null) {
            return;
        }

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        return featureAPI.getById(
                                selected.getFeatureId()
                        );

                    } catch (
                            IOException |
                            InterruptedException e
                    ) {

                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }

                        throw new RuntimeException(e);
                    }

                })
                .thenAccept(feature ->
                        Platform.runLater(() ->
                                showEditDialog(feature)
                        )
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "تعذر تحميل الميزة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    private void showEditDialog(
            FeatureResponse feature
    ) {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "تعديل الميزة"
        );

        VBox box =
                new VBox(12);

        box.setPadding(
                new Insets(15)
        );

        Label code =
                new Label(
                        "رمز الميزة: "
                                + safe(
                                feature.getFeatureCode()
                        )
                );

        TextField name =
                new TextField(
                        safe(
                                feature.getFeatureName()
                        )
                );

        TextArea description =
                new TextArea(
                        safe(
                                feature.getDescription()
                        )
                );

        description.setPrefRowCount(4);

        ComboBox<String> status =
                new ComboBox<>(
                        FXCollections.observableArrayList(
                                "ACTIVE",
                                "DISABLED"
                        )
                );

        status.setValue(
                feature.getStatus()
        );

        box.getChildren().addAll(

                code,

                new Label("اسم الميزة"),
                name,

                new Label("الوصف"),
                description,

                new Label("الحالة"),
                status
        );

        dialog.getDialogPane()
                .setContent(box);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        ButtonType.OK,
                        ButtonType.CANCEL
                );

        dialog.showAndWait()
                .ifPresent(result -> {

                    if (result != ButtonType.OK) {
                        return;
                    }

                    if (name.getText().isBlank()) {

                        showError(
                                "بيانات غير مكتملة",
                                "اسم الميزة مطلوب."
                        );

                        return;
                    }

                    FeatureRequest request =
                            new FeatureRequest();

                    request.setFeatureCode(
                            feature.getFeatureCode()
                    );

                    request.setFeatureName(
                            name.getText().trim()
                    );

                    request.setDescription(
                            clean(description.getText())
                    );

                    request.setStatus(
                            status.getValue() == null
                                    ? "ACTIVE"
                                    : status.getValue()
                    );

                    updateFeature(
                            feature.getFeatureId(),
                            request
                    );
                });
    }

    private void updateFeature(
            Long featureId,
            FeatureRequest request
    ) {

        CompletableFuture
                .runAsync(() -> {

                    try {

                        featureAPI.update(
                                featureId,
                                request
                        );

                    } catch (
                            IOException |
                            InterruptedException e
                    ) {

                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }

                        throw new RuntimeException(e);
                    }

                })
                .thenRun(() ->
                        Platform.runLater(() -> {

                            showInfo(
                                    "نجاح",
                                    "تم تعديل الميزة بنجاح."
                            );

                            loadFeatures();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل تعديل الميزة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // تغيير الحالة
    // =========================================================

    private void changeSelectedStatus(
            String newStatus
    ) {

        FeatureRow selected =
                getSelectedFeature();

        if (selected == null) {
            return;
        }

        String title =
                "ACTIVE".equals(newStatus)
                        ? "تفعيل الميزة"
                        : "تعطيل الميزة";

        String message =
                "ACTIVE".equals(newStatus)
                        ? "هل تريد تفعيل الميزة المحددة؟"
                        : "هل تريد تعطيل الميزة المحددة؟";

        if (!confirm(title, message)) {
            return;
        }

        CompletableFuture
                .runAsync(() -> {

                    try {

                        FeatureResponse current =
                                featureAPI.getById(
                                        selected.getFeatureId()
                                );

                        FeatureRequest request =
                                new FeatureRequest();

                        request.setFeatureCode(
                                current.getFeatureCode()
                        );

                        request.setFeatureName(
                                current.getFeatureName()
                        );

                        request.setDescription(
                                current.getDescription()
                        );

                        request.setStatus(
                                newStatus
                        );

                        featureAPI.update(
                                selected.getFeatureId(),
                                request
                        );

                    } catch (
                            IOException |
                            InterruptedException e
                    ) {

                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }

                        throw new RuntimeException(e);
                    }

                })
                .thenRun(() ->
                        Platform.runLater(() -> {

                            showInfo(
                                    "تم",
                                    "تم تغيير حالة الميزة."
                            );

                            loadFeatures();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل تغيير حالة الميزة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // Helpers
    // =========================================================

    private FeatureRow getSelectedFeature() {

        FeatureRow selected =
                table.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showInfo(
                    "تحديد الميزة",
                    "اختر ميزة أولًا."
            );
        }

        return selected;
    }

    private boolean confirm(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait()
                .filter(ButtonType.OK::equals)
                .isPresent();
    }

    private void showInfo(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getRootCauseMessage(
            Throwable throwable
    ) {

        Throwable cause = throwable;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String message =
                cause.getMessage();

        return message == null ||
                message.isBlank()
                ? "حدث خطأ غير معروف."
                : message;
    }

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isBlank()
                ? null
                : trimmed;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}


// =============================================================
// Feature Row
// =============================================================

class FeatureRow {

    private Long featureId;

    private final StringProperty code =
            new SimpleStringProperty();

    private final StringProperty name =
            new SimpleStringProperty();

    private final StringProperty description =
            new SimpleStringProperty();

    private final StringProperty status =
            new SimpleStringProperty();

    public static FeatureRow fromResponse(
            FeatureResponse response
    ) {

        FeatureRow row =
                new FeatureRow();

        row.featureId =
                response.getFeatureId();

        row.code.set(
                response.getFeatureCode()
        );

        row.name.set(
                response.getFeatureName()
        );

        row.description.set(
                response.getDescription() == null
                        ? ""
                        : response.getDescription()
        );

        row.status.set(
                response.getStatus()
        );

        return row;
    }

    public Long getFeatureId() {
        return featureId;
    }

    public StringProperty codeProperty() {
        return code;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty statusProperty() {
        return status;
    }
}