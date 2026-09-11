package ui;

import api.AdminApiManager;
import api.ClinicAPI;
import dto.ClinicCreateRequest;
import dto.ClinicResponse;
import dto.ClinicUpdateRequest;

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

public class ClinicsView extends BorderPane {

    private final TableView<ClinicRow> table =
            new TableView<>();

    private final ObservableList<ClinicRow> rows =
            FXCollections.observableArrayList();

    private final ClinicAPI clinicAPI;

    public ClinicsView() {

        AdminApiManager apiManager =
                AdminApiManager.getInstance();

        clinicAPI =
                apiManager.getClinicAPI();

        setPadding(new Insets(25));

        Label title =
                new Label("العيادات");

        title.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
        """);

        Button add =
                new Button("➕ إضافة عيادة");

        Button edit =
                new Button("✏ تعديل");

        Button suspend =
                new Button("⛔ إيقاف");

        Button activate =
                new Button("✅ تفعيل");

        HBox toolbar =
                new HBox(
                        10,
                        add,
                        edit,
                        suspend,
                        activate
                );

        add.setOnAction(e ->
                showAddClinicDialog()
        );

        edit.setOnAction(e ->
                showEditClinicDialog()
        );

        suspend.setOnAction(e ->
                suspendSelectedClinic()
        );

        activate.setOnAction(e ->
                activateSelectedClinic()
        );

        TableColumn<ClinicRow, String> code =
                new TableColumn<>("الكود");

        TableColumn<ClinicRow, String> name =
                new TableColumn<>("اسم العيادة");

        TableColumn<ClinicRow, String> owner =
                new TableColumn<>("المالك");

        TableColumn<ClinicRow, String> status =
                new TableColumn<>("الحالة");

        code.setCellValueFactory(
                data -> data.getValue().codeProperty()
        );

        name.setCellValueFactory(
                data -> data.getValue().nameProperty()
        );

        owner.setCellValueFactory(
                data -> data.getValue().ownerProperty()
        );

        status.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );

        table.getColumns().addAll(
                code,
                name,
                owner,
                status
        );

        table.setItems(rows);

        VBox top =
                new VBox(
                        15,
                        title,
                        toolbar
                );

        setTop(top);
        setCenter(table);

        loadClinics();
    }

    // =========================================================
    // تحميل العيادات
    // =========================================================

    private void loadClinics() {

        CompletableFuture
                .supplyAsync(() -> {

                    try {
                        return clinicAPI.getAllClinics();

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
                .thenAccept(clinics -> {

                    Platform.runLater(() -> {

                        rows.clear();

                        for (ClinicResponse clinic : clinics) {
                            rows.add(
                                    ClinicRow.fromResponse(clinic)
                            );
                        }
                    });

                })
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "تعذر تحميل العيادات",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // إضافة عيادة
    // =========================================================

    private void showAddClinicDialog() {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle("إضافة عيادة");

        VBox box =
                new VBox(12);

        box.setPadding(
                new Insets(15)
        );

        TextField code =
                new TextField();

        code.setPromptText("كود العيادة");

        TextField name =
                new TextField();

        name.setPromptText("اسم العيادة");

        TextField owner =
                new TextField();

        owner.setPromptText("اسم المالك");

        TextField phone =
                new TextField();

        phone.setPromptText("رقم الهاتف");

        TextField email =
                new TextField();

        email.setPromptText("البريد الإلكتروني");

        TextField address =
                new TextField();

        address.setPromptText("العنوان");

        box.getChildren().addAll(
                new Label("كود العيادة"),
                code,
                new Label("اسم العيادة"),
                name,
                new Label("المالك"),
                owner,
                new Label("الهاتف"),
                phone,
                new Label("البريد الإلكتروني"),
                email,
                new Label("العنوان"),
                address
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
                                "كود العيادة واسم العيادة مطلوبان."
                        );

                        return;
                    }

                    ClinicCreateRequest request =
                            new ClinicCreateRequest();

                    request.setClinicCode(
                            code.getText().trim()
                    );

                    request.setClinicName(
                            name.getText().trim()
                    );

                    request.setOwnerName(
                            owner.getText().trim()
                    );

                    request.setPhone(
                            phone.getText().trim()
                    );

                    request.setEmail(
                            email.getText().trim()
                    );

                    request.setAddress(
                            address.getText().trim()
                    );

                    createClinic(request);
                });
    }

    private void createClinic(
            ClinicCreateRequest request
    ) {

        CompletableFuture
                .runAsync(() -> {

                    try {

                        clinicAPI.createClinic(
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
                                    "تم إنشاء العيادة بنجاح."
                            );

                            loadClinics();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل إنشاء العيادة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // تعديل العيادة
    // =========================================================

    private void showEditClinicDialog() {

        ClinicRow selected =
                table.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showInfo(
                    "تحديد العيادة",
                    "اختر عيادة أولًا."
            );

            return;
        }

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        return clinicAPI.getClinic(
                                selected.getClinicId()
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
                .thenAccept(clinic ->
                        Platform.runLater(() ->
                                showEditDialog(clinic)
                        )
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "تعذر تحميل بيانات العيادة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    private void showEditDialog(
            ClinicResponse clinic
    ) {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle("تعديل العيادة");

        VBox box =
                new VBox(12);

        box.setPadding(new Insets(15));

        TextField code =
                new TextField(
                        safe(clinic.getClinicCode())
                );

        TextField name =
                new TextField(
                        safe(clinic.getClinicName())
                );

        TextField owner =
                new TextField(
                        safe(clinic.getOwnerName())
                );

        TextField phone =
                new TextField(
                        safe(clinic.getPhone())
                );

        TextField email =
                new TextField(
                        safe(clinic.getEmail())
                );

        TextField address =
                new TextField(
                        safe(clinic.getAddress())
                );

        box.getChildren().addAll(
                new Label("كود العيادة"),
                code,
                new Label("اسم العيادة"),
                name,
                new Label("المالك"),
                owner,
                new Label("الهاتف"),
                phone,
                new Label("البريد الإلكتروني"),
                email,
                new Label("العنوان"),
                address
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
                                "اسم العيادة مطلوب."
                        );

                        return;
                    }

                    ClinicUpdateRequest request =
                            new ClinicUpdateRequest();

                    request.setClinicName(
                            name.getText().trim()
                    );

                    request.setOwnerName(
                            owner.getText().trim()
                    );

                    request.setPhone(
                            phone.getText().trim()
                    );

                    request.setEmail(
                            email.getText().trim()
                    );

                    request.setAddress(
                            address.getText().trim()
                    );

                    updateClinic(
                            clinic.getClinicId(),
                            request
                    );
                });
    }

    private void updateClinic(
            Long clinicId,
            ClinicUpdateRequest request
    ) {

        CompletableFuture
                .runAsync(() -> {

                    try {

                        clinicAPI.updateClinic(
                                clinicId,
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
                                    "تم تعديل العيادة بنجاح."
                            );

                            loadClinics();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل تعديل العيادة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // إيقاف العيادة
    // =========================================================

    private void suspendSelectedClinic() {

        ClinicRow selected =
                getSelectedClinic();

        if (selected == null) {
            return;
        }

        if (!confirm(
                "إيقاف العيادة",
                "هل تريد إيقاف العيادة المحددة؟"
        )) {
            return;
        }

        CompletableFuture
                .runAsync(() -> {

                    try {

                        clinicAPI.suspendClinic(
                                selected.getClinicId()
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
                                    "تم إيقاف العيادة."
                            );

                            loadClinics();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل إيقاف العيادة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // تفعيل العيادة
    // =========================================================

    private void activateSelectedClinic() {

        ClinicRow selected =
                getSelectedClinic();

        if (selected == null) {
            return;
        }

        if (!confirm(
                "تفعيل العيادة",
                "هل تريد إعادة تفعيل العيادة المحددة؟"
        )) {
            return;
        }

        CompletableFuture
                .runAsync(() -> {

                    try {

                        clinicAPI.activateClinic(
                                selected.getClinicId()
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
                                    "تم تفعيل العيادة."
                            );

                            loadClinics();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل تفعيل العيادة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // Helpers
    // =========================================================

    private ClinicRow getSelectedClinic() {

        ClinicRow selected =
                table.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showInfo(
                    "تحديد العيادة",
                    "اختر عيادة أولًا."
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

    private String safe(String value) {
        return value == null ? "" : value;
    }
}


// =============================================================
// جدول العيادات
// =============================================================

class ClinicRow {

    private Long clinicId;

    private final StringProperty code =
            new SimpleStringProperty();

    private final StringProperty name =
            new SimpleStringProperty();

    private final StringProperty owner =
            new SimpleStringProperty();

    private final StringProperty status =
            new SimpleStringProperty();

    public static ClinicRow fromResponse(
            ClinicResponse response
    ) {

        ClinicRow row =
                new ClinicRow();

        row.clinicId =
                response.getClinicId();

        row.code.set(
                response.getClinicCode()
        );

        row.name.set(
                response.getClinicName()
        );

        row.owner.set(
                response.getOwnerName()
        );

        row.status.set(
                response.getStatus()
        );

        return row;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public StringProperty codeProperty() {
        return code;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty ownerProperty() {
        return owner;
    }

    public StringProperty statusProperty() {
        return status;
    }
}