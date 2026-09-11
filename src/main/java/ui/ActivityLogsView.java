package ui;

import api.ActivityLogAPI;
import api.AdminApiManager;
import dto.AdminActivityLogFilterRequest;
import dto.AdminActivityLogResponse;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ActivityLogsView extends BorderPane {

    private final ActivityLogAPI activityLogAPI;

    private final TableView<ActivityLogRow> table =
            new TableView<>();

    private final ObservableList<ActivityLogRow> rows =
            FXCollections.observableArrayList();

    private final TextField search =
            new TextField();

    private final DatePicker from =
            new DatePicker();

    private final DatePicker to =
            new DatePicker();

    public ActivityLogsView() {

        AdminApiManager apiManager =
                AdminApiManager.getInstance();

        activityLogAPI =
                apiManager.getActivityLogAPI();

        setPadding(new Insets(25));

        Label title =
                new Label("سجل النشاط");

        title.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
        """);

        search.setPromptText(
                "بحث في سجل النشاط..."
        );

        from.setPromptText(
                "من تاريخ"
        );

        to.setPromptText(
                "إلى تاريخ"
        );

        Button searchButton =
                new Button("🔎 بحث");

        Button refreshButton =
                new Button("🔃 تحديث");

        HBox filters =
                new HBox(
                        10,
                        search,
                        from,
                        to,
                        searchButton,
                        refreshButton
                );

        // =====================================================
        // الأعمدة
        // =====================================================

        TableColumn<ActivityLogRow, String> date =
                new TableColumn<>("التاريخ");

        TableColumn<ActivityLogRow, String> user =
                new TableColumn<>("المستخدم");

        TableColumn<ActivityLogRow, String> action =
                new TableColumn<>("العملية");

        TableColumn<ActivityLogRow, String> entity =
                new TableColumn<>("العنصر");

        TableColumn<ActivityLogRow, String> details =
                new TableColumn<>("التفاصيل");

        date.setPrefWidth(180);
        user.setPrefWidth(160);
        action.setPrefWidth(140);
        entity.setPrefWidth(160);
        details.setPrefWidth(350);

        date.setCellValueFactory(
                data -> data.getValue().dateProperty()
        );

        user.setCellValueFactory(
                data -> data.getValue().userProperty()
        );

        action.setCellValueFactory(
                data -> data.getValue().actionProperty()
        );

        entity.setCellValueFactory(
                data -> data.getValue().entityProperty()
        );

        details.setCellValueFactory(
                data -> data.getValue().detailsProperty()
        );

        table.getColumns().addAll(
                date,
                user,
                action,
                entity,
                details
        );

        table.setItems(rows);

        // =====================================================
        // الأحداث
        // =====================================================

        searchButton.setOnAction(e ->
                searchLogs()
        );

        refreshButton.setOnAction(e ->
                loadLogs()
        );

        setTop(
                new VBox(
                        15,
                        title,
                        filters
                )
        );

        setCenter(table);

        loadLogs();
    }

    // =========================================================
    // تحميل جميع السجلات
    // =========================================================

    private void loadLogs() {

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        return activityLogAPI.getAll();

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
                .thenAccept(logs ->
                        Platform.runLater(() ->
                                setRows(logs)
                        )
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "تعذر تحميل سجل النشاط",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // البحث
    // =========================================================

    private void searchLogs() {

        LocalDate fromDate =
                from.getValue();

        LocalDate toDate =
                to.getValue();

        if (fromDate != null &&
                toDate != null &&
                fromDate.isAfter(toDate)) {

            showError(
                    "تاريخ غير صحيح",
                    "تاريخ البداية لا يمكن أن يكون بعد تاريخ النهاية."
            );

            return;
        }

        AdminActivityLogFilterRequest request =
                new AdminActivityLogFilterRequest();

        request.setKeyword(
                clean(search.getText())
        );

        request.setFrom(
                fromDate
        );

        request.setTo(
                toDate
        );

        /*
         * لا نرسل adminUserId من الواجهة هنا.
         *
         * السيرفر يستطيع لاحقًا استخدام المستخدم الحالي
         * أو السماح بالفلترة حسب مدير معين من خلال صلاحية مناسبة.
         */

        request.setAdminUserId(
                null
        );

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        return activityLogAPI.search(
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
                .thenAccept(logs ->
                        Platform.runLater(() ->
                                setRows(logs)
                        )
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل البحث",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =========================================================
    // تحديث الجدول
    // =========================================================

    private void setRows(
            List<AdminActivityLogResponse> logs
    ) {

        rows.clear();

        for (AdminActivityLogResponse log : logs) {

            rows.add(
                    ActivityLogRow.fromResponse(log)
            );
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

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
// Activity Log Row
// =============================================================

class ActivityLogRow {

    private final SimpleStringProperty date =
            new SimpleStringProperty();

    private final SimpleStringProperty user =
            new SimpleStringProperty();

    private final SimpleStringProperty action =
            new SimpleStringProperty();

    private final SimpleStringProperty entity =
            new SimpleStringProperty();

    private final SimpleStringProperty details =
            new SimpleStringProperty();

    public static ActivityLogRow fromResponse(
            AdminActivityLogResponse response
    ) {

        ActivityLogRow row =
                new ActivityLogRow();

        row.date.set(
                response.getCreatedAt() == null
                        ? ""
                        : response.getCreatedAt().toString()
        );

        row.user.set(
                response.getAdminUsername() == null
                        ? String.valueOf(
                        response.getAdminUserId()
                )
                        : response.getAdminUsername()
        );

        row.action.set(
                safe(response.getAction())
        );

        row.entity.set(
                response.getEntityName() == null
                        ? ""
                        : response.getEntityName()
        );

        row.details.set(
                response.getDetails() == null
                        ? ""
                        : response.getDetails()
        );

        return row;
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public SimpleStringProperty userProperty() {
        return user;
    }

    public SimpleStringProperty actionProperty() {
        return action;
    }

    public SimpleStringProperty entityProperty() {
        return entity;
    }

    public SimpleStringProperty detailsProperty() {
        return details;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}