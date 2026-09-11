package ui;

import api.AdminApiManager;
import api.SoftwareVersionAPI;
import dto.SoftwareVersionRequest;
import dto.SoftwareVersionResponse;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SoftwareUpdatesView extends BorderPane {

    private final TableView<SoftwareVersionRow> table =
            new TableView<>();

    private final ObservableList<SoftwareVersionRow> rows =
            FXCollections.observableArrayList();

    private final SoftwareVersionAPI versionAPI;

    public SoftwareUpdatesView() {

        AdminApiManager apiManager =
                AdminApiManager.getInstance();

        versionAPI =
                apiManager.getSoftwareVersionAPI();

        setPadding(new Insets(25));

        Label title =
                new Label("إدارة التحديثات");

        title.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
        """);

        Button add =
                new Button("➕ إصدار جديد");

        Button block =
                new Button("⛔ حظر الإصدار");

        Button retire =
                new Button("📦 إنهاء الإصدار");

        Button activate =
                new Button("✅ تفعيل الإصدار");

        Button refresh =
                new Button("🔃 تحديث");

        HBox toolbar =
                new HBox(
                        10,
                        add,
                        block,
                        retire,
                        activate,
                        refresh
                );

        // =====================================================
        // الأعمدة
        // =====================================================

        TableColumn<SoftwareVersionRow, String> application =
                new TableColumn<>("التطبيق");

        TableColumn<SoftwareVersionRow, String> version =
                new TableColumn<>("الإصدار");

        TableColumn<SoftwareVersionRow, String> minimum =
                new TableColumn<>("الحد الأدنى");

        TableColumn<SoftwareVersionRow, String> releaseDate =
                new TableColumn<>("تاريخ الإصدار");

        TableColumn<SoftwareVersionRow, String> mandatory =
                new TableColumn<>("إجباري");

        TableColumn<SoftwareVersionRow, String> status =
                new TableColumn<>("الحالة");

        application.setPrefWidth(180);
        version.setPrefWidth(130);
        minimum.setPrefWidth(130);
        releaseDate.setPrefWidth(130);
        mandatory.setPrefWidth(100);
        status.setPrefWidth(120);

        application.setCellValueFactory(
                data -> data.getValue().applicationProperty()
        );

        version.setCellValueFactory(
                data -> data.getValue().versionProperty()
        );

        minimum.setCellValueFactory(
                data -> data.getValue().minimumProperty()
        );

        releaseDate.setCellValueFactory(
                data -> data.getValue().releaseDateProperty()
        );

        mandatory.setCellValueFactory(
                data -> data.getValue().mandatoryProperty()
        );

        status.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );

        table.getColumns().addAll(
                application,
                version,
                minimum,
                releaseDate,
                mandatory,
                status
        );

        table.setItems(rows);

        // =====================================================
        // الأحداث
        // =====================================================

        add.setOnAction(e ->
                showAddDialog()
        );

        block.setOnAction(e ->
                changeStatus("BLOCKED")
        );

        retire.setOnAction(e ->
                changeStatus("RETIRED")
        );

        activate.setOnAction(e ->
                changeStatus("ACTIVE")
        );

        refresh.setOnAction(e ->
                loadVersions()
        );

        setTop(
                new VBox(
                        15,
                        title,
                        toolbar
                )
        );

        setCenter(table);

        loadVersions();
    }

    // =========================================================
    // تحميل الإصدارات
    // =========================================================

    private void loadVersions() {

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        return versionAPI.getAll();

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
                .thenAccept(versions ->
                        Platform.runLater(() -> {

                            rows.clear();

                            for (SoftwareVersionResponse version
                                    : versions) {

                                rows.add(
                                        SoftwareVersionRow
                                                .fromResponse(version)
                                );
                            }
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "تعذر تحميل الإصدارات",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // إضافة إصدار
    // =========================================================

    private void showAddDialog() {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "إضافة إصدار جديد"
        );

        VBox box =
                new VBox(12);

        box.setPadding(
                new Insets(15)
        );

        TextField applicationName =
                new TextField();

        applicationName.setPromptText(
                "اسم التطبيق"
        );

        TextField versionNumber =
                new TextField();

        versionNumber.setPromptText(
                "رقم الإصدار"
        );

        TextField minimumVersion =
                new TextField();

        minimumVersion.setPromptText(
                "الحد الأدنى للإصدار"
        );

        DatePicker releaseDate =
                new DatePicker(
                        LocalDate.now()
                );

        ComboBox<Boolean> mandatory =
                new ComboBox<>(
                        FXCollections.observableArrayList(
                                false,
                                true
                        )
                );

        mandatory.setValue(false);

        TextArea releaseNotes =
                new TextArea();

        releaseNotes.setPromptText(
                "ملاحظات الإصدار"
        );

        releaseNotes.setPrefRowCount(5);

        box.getChildren().addAll(

                new Label("اسم التطبيق"),
                applicationName,

                new Label("رقم الإصدار"),
                versionNumber,

                new Label("الحد الأدنى للإصدار"),
                minimumVersion,

                new Label("تاريخ الإصدار"),
                releaseDate,

                new Label("إصدار إجباري"),
                mandatory,

                new Label("ملاحظات الإصدار"),
                releaseNotes
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

                    if (applicationName.getText().isBlank()
                            || versionNumber.getText().isBlank()
                            || releaseDate.getValue() == null) {

                        showError(
                                "بيانات غير مكتملة",
                                "اسم التطبيق ورقم الإصدار وتاريخ الإصدار مطلوبة."
                        );

                        return;
                    }

                    SoftwareVersionRequest request =
                            new SoftwareVersionRequest();

                    request.setApplicationName(
                            applicationName.getText().trim()
                    );

                    request.setVersionNumber(
                            versionNumber.getText().trim()
                    );

                    request.setMinimumVersion(
                            clean(minimumVersion.getText())
                    );

                    request.setReleaseDate(
                            releaseDate.getValue()
                    );

                    request.setStatus(
                            "ACTIVE"
                    );

                    request.setMandatory(
                            mandatory.getValue()
                    );

                    request.setReleaseNotes(
                            clean(releaseNotes.getText())
                    );

                    createVersion(request);
                });
    }

    private void createVersion(
            SoftwareVersionRequest request
    ) {

        CompletableFuture
                .runAsync(() -> {

                    try {

                        versionAPI.create(
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
                                    "تم إنشاء الإصدار بنجاح."
                            );

                            loadVersions();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل إنشاء الإصدار",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // تغيير حالة الإصدار
    // =========================================================

    private void changeStatus(
            String newStatus
    ) {

        SoftwareVersionRow selected =
                table.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showInfo(
                    "تحديد الإصدار",
                    "اختر إصدارًا أولًا."
            );

            return;
        }

        if (newStatus.equalsIgnoreCase(
                selected.getStatus()
        )) {

            showInfo(
                    "لا يوجد تغيير",
                    "الإصدار موجود بالفعل بهذه الحالة."
            );

            return;
        }

        String title;
        String message;

        switch (newStatus) {

            case "BLOCKED" -> {
                title = "حظر الإصدار";
                message =
                        "هل تريد حظر الإصدار المحدد؟";
            }

            case "RETIRED" -> {
                title = "إنهاء الإصدار";
                message =
                        "هل تريد إنهاء الإصدار المحدد؟";
            }

            case "ACTIVE" -> {
                title = "تفعيل الإصدار";
                message =
                        "هل تريد إعادة تفعيل الإصدار المحدد؟";
            }

            default -> {
                return;
            }
        }

        if (!confirm(title, message)) {
            return;
        }

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        SoftwareVersionResponse current =
                                versionAPI.getById(
                                        selected.getVersionId()
                                );

                        SoftwareVersionRequest request =
                                new SoftwareVersionRequest();

                        request.setApplicationName(
                                current.getApplicationName()
                        );

                        request.setVersionNumber(
                                current.getVersionNumber()
                        );

                        request.setMinimumVersion(
                                current.getMinimumVersion()
                        );

                        request.setReleaseDate(
                                current.getReleaseDate()
                        );

                        request.setStatus(
                                newStatus
                        );

                        request.setMandatory(
                                current.getMandatory()
                        );

                        request.setReleaseNotes(
                                current.getReleaseNotes()
                        );

                        return versionAPI.update(
                                selected.getVersionId(),
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
                .thenAccept(updated ->
                        Platform.runLater(() -> {

                            showInfo(
                                    "نجاح",
                                    "تم تغيير حالة الإصدار."
                            );

                            loadVersions();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل تغيير حالة الإصدار",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // Helpers
    // =========================================================

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
}


// =============================================================
// Software Version Row
// =============================================================

class SoftwareVersionRow {

    private Long versionId;

    private String status;

    private final StringProperty application =
            new SimpleStringProperty();

    private final StringProperty version =
            new SimpleStringProperty();

    private final StringProperty minimum =
            new SimpleStringProperty();

    private final StringProperty releaseDate =
            new SimpleStringProperty();

    private final StringProperty mandatory =
            new SimpleStringProperty();

    private final StringProperty statusProperty =
            new SimpleStringProperty();

    public static SoftwareVersionRow fromResponse(
            SoftwareVersionResponse response
    ) {

        SoftwareVersionRow row =
                new SoftwareVersionRow();

        row.versionId =
                response.getVersionId();

        row.status =
                response.getStatus();

        row.application.set(
                safe(response.getApplicationName())
        );

        row.version.set(
                safe(response.getVersionNumber())
        );

        row.minimum.set(
                safe(response.getMinimumVersion())
        );

        row.releaseDate.set(
                response.getReleaseDate() == null
                        ? ""
                        : response.getReleaseDate().toString()
        );

        row.mandatory.set(
                Boolean.TRUE.equals(
                        response.getMandatory()
                )
                        ? "نعم"
                        : "لا"
        );

        row.statusProperty.set(
                safe(response.getStatus())
        );

        return row;
    }

    public Long getVersionId() {
        return versionId;
    }

    public String getStatus() {
        return status;
    }

    public StringProperty applicationProperty() {
        return application;
    }

    public StringProperty versionProperty() {
        return version;
    }

    public StringProperty minimumProperty() {
        return minimum;
    }

    public StringProperty releaseDateProperty() {
        return releaseDate;
    }

    public StringProperty mandatoryProperty() {
        return mandatory;
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}