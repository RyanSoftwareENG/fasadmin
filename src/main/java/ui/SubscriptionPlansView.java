package ui;

import api.AdminApiManager;
import api.SubscriptionPlanAPI;
import dto.SubscriptionPlanRequest;
import dto.SubscriptionPlanResponse;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SubscriptionPlansView extends BorderPane {

    private final TableView<PlanRow> table =
            new TableView<>();

    private final ObservableList<PlanRow> rows =
            FXCollections.observableArrayList();

    private final SubscriptionPlanAPI planAPI;

    public SubscriptionPlansView() {

        AdminApiManager apiManager =
                AdminApiManager.getInstance();

        planAPI =
                apiManager.getSubscriptionPlanAPI();

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setPadding(
                new Insets(25)
        );

        buildUI();

        loadPlans();
    }

    // =====================================================
    // إنشاء الواجهة
    // =====================================================

    private void buildUI() {

        Label title =
                new Label("خطط الاشتراك");

        title.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
        """);

        Button add =
                new Button("➕ إضافة خطة");

        Button edit =
                new Button("✏ تعديل");

        Button disable =
                new Button("⛔ تعطيل");

        Button activate =
                new Button("✅ تفعيل");

        Button delete =
                new Button("🗑 حذف");

        Button refresh =
                new Button("🔃 تحديث");

        HBox toolbar =
                new HBox(
                        10,
                        add,
                        edit,
                        disable,
                        activate,
                        delete,
                        refresh
                );

        // =====================================================
        // أعمدة الجدول
        // =====================================================

        TableColumn<PlanRow, String> name =
                new TableColumn<>("الخطة");

        TableColumn<PlanRow, String> description =
                new TableColumn<>("الوصف");

        TableColumn<PlanRow, String> price =
                new TableColumn<>("السعر");

        TableColumn<PlanRow, String> currency =
                new TableColumn<>("العملة");

        TableColumn<PlanRow, String> duration =
                new TableColumn<>("المدة");

        TableColumn<PlanRow, String> status =
                new TableColumn<>("الحالة");

        name.setPrefWidth(180);
        description.setPrefWidth(300);
        price.setPrefWidth(120);
        currency.setPrefWidth(100);
        duration.setPrefWidth(120);
        status.setPrefWidth(120);

        name.setCellValueFactory(
                data ->
                        data.getValue()
                                .nameProperty()
        );

        description.setCellValueFactory(
                data ->
                        data.getValue()
                                .descriptionProperty()
        );

        price.setCellValueFactory(
                data ->
                        data.getValue()
                                .priceProperty()
        );

        currency.setCellValueFactory(
                data ->
                        data.getValue()
                                .currencyProperty()
        );

        duration.setCellValueFactory(
                data ->
                        data.getValue()
                                .durationProperty()
        );

        status.setCellValueFactory(
                data ->
                        data.getValue()
                                .statusProperty()
        );

        table.getColumns().addAll(
                name,
                description,
                price,
                currency,
                duration,
                status
        );

        table.setItems(rows);

        table.setPlaceholder(
                new Label(
                        "لا توجد خطط اشتراك."
                )
        );

        // =====================================================
        // الأحداث
        // =====================================================

        add.setOnAction(
                e -> showAddPlanDialog()
        );

        edit.setOnAction(
                e -> showEditPlanDialog()
        );

        disable.setOnAction(
                e -> changeSelectedStatus("DISABLED")
        );

        activate.setOnAction(
                e -> changeSelectedStatus("ACTIVE")
        );

        delete.setOnAction(
                e -> deleteSelectedPlan()
        );

        refresh.setOnAction(
                e -> loadPlans()
        );

        VBox top =
                new VBox(
                        15,
                        title,
                        toolbar
                );

        setTop(top);
        setCenter(table);
    }

    // =====================================================
    // تحميل الخطط
    // =====================================================

    private void loadPlans() {

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        return planAPI.getAll();

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
                .thenAccept(plans ->
                        Platform.runLater(() -> {

                            rows.clear();

                            for (
                                    SubscriptionPlanResponse plan :
                                    plans
                            ) {

                                rows.add(
                                        PlanRow.fromResponse(
                                                plan
                                        )
                                );
                            }
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "تعذر تحميل خطط الاشتراك",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =====================================================
    // إضافة خطة
    // =====================================================

    private void showAddPlanDialog() {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "إضافة خطة اشتراك"
        );

        dialog.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        VBox box =
                new VBox(12);

        box.setPadding(
                new Insets(15)
        );

        TextField name =
                new TextField();

        name.setPromptText(
                "اسم الخطة"
        );

        TextArea description =
                new TextArea();

        description.setPromptText(
                "وصف الخطة"
        );

        description.setPrefRowCount(4);

        TextField price =
                new TextField();

        price.setPromptText(
                "السعر"
        );

        TextField currency =
                new TextField();

        currency.setPromptText(
                "العملة - مثال: YER"
        );

        TextField duration =
                new TextField();

        duration.setPromptText(
                "المدة بالأيام"
        );

        box.getChildren().addAll(

                new Label("اسم الخطة"),
                name,

                new Label("الوصف"),
                description,

                new Label("السعر"),
                price,

                new Label("العملة"),
                currency,

                new Label("المدة بالأيام"),
                duration
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
                                "اسم الخطة مطلوب."
                        );

                        return;
                    }

                    BigDecimal parsedPrice =
                            parsePrice(
                                    price.getText()
                            );

                    if (!price.getText().isBlank()
                            && parsedPrice == null) {

                        showError(
                                "السعر غير صالح",
                                "يرجى إدخال سعر رقمي صحيح."
                        );

                        return;
                    }

                    Integer parsedDuration =
                            parseDuration(
                                    duration.getText()
                            );

                    if (!duration.getText().isBlank()
                            && parsedDuration == null) {

                        showError(
                                "المدة غير صالحة",
                                "يرجى إدخال عدد أيام صحيح."
                        );

                        return;
                    }

                    if (parsedDuration != null
                            && parsedDuration <= 0) {

                        showError(
                                "المدة غير صالحة",
                                "يجب أن تكون المدة أكبر من صفر."
                        );

                        return;
                    }

                    SubscriptionPlanRequest request =
                            new SubscriptionPlanRequest();

                    request.setPlanName(
                            name.getText().trim()
                    );

                    request.setDescription(
                            clean(description.getText())
                    );

                    request.setPrice(
                            parsedPrice
                    );

                    request.setCurrencyCode(
                            clean(currency.getText())
                    );

                    request.setDurationDays(
                            parsedDuration
                    );

                    request.setStatus(
                            "ACTIVE"
                    );

                    createPlan(request);
                });
    }

    private void createPlan(
            SubscriptionPlanRequest request
    ) {

        CompletableFuture
                .runAsync(() -> {

                    try {

                        planAPI.create(
                                request
                        );

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
                .thenRun(() ->
                        Platform.runLater(() -> {

                            showInfo(
                                    "نجاح",
                                    "تم إنشاء خطة الاشتراك."
                            );

                            loadPlans();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل إنشاء الخطة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =====================================================
    // تعديل خطة
    // =====================================================

    private void showEditPlanDialog() {

        PlanRow selected =
                getSelectedPlan();

        if (selected == null) {
            return;
        }

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        return planAPI.getById(
                                selected.getPlanId()
                        );

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
                .thenAccept(plan ->
                        Platform.runLater(() ->
                                showEditDialog(plan)
                        )
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "تعذر تحميل الخطة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    private void showEditDialog(
            SubscriptionPlanResponse plan
    ) {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "تعديل خطة الاشتراك"
        );

        dialog.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        VBox box =
                new VBox(12);

        box.setPadding(
                new Insets(15)
        );

        TextField name =
                new TextField(
                        safe(plan.getPlanName())
                );

        TextArea description =
                new TextArea(
                        safe(plan.getDescription())
                );

        description.setPrefRowCount(4);

        TextField price =
                new TextField(
                        plan.getPrice() == null
                                ? ""
                                : plan.getPrice().toPlainString()
                );

        TextField currency =
                new TextField(
                        safe(plan.getCurrencyCode())
                );

        TextField duration =
                new TextField(
                        plan.getDurationDays() == null
                                ? ""
                                : String.valueOf(
                                plan.getDurationDays()
                        )
                );

        ComboBox<String> status =
                new ComboBox<>(
                        FXCollections.observableArrayList(
                                "ACTIVE",
                                "DISABLED"
                        )
                );

        status.setValue(
                plan.getStatus()
        );

        box.getChildren().addAll(

                new Label("اسم الخطة"),
                name,

                new Label("الوصف"),
                description,

                new Label("السعر"),
                price,

                new Label("العملة"),
                currency,

                new Label("المدة بالأيام"),
                duration,

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
                                "اسم الخطة مطلوب."
                        );

                        return;
                    }

                    BigDecimal parsedPrice =
                            parsePrice(
                                    price.getText()
                            );

                    if (!price.getText().isBlank()
                            && parsedPrice == null) {

                        showError(
                                "السعر غير صالح",
                                "يرجى إدخال سعر رقمي صحيح."
                        );

                        return;
                    }

                    Integer parsedDuration =
                            parseDuration(
                                    duration.getText()
                            );

                    if (!duration.getText().isBlank()
                            && parsedDuration == null) {

                        showError(
                                "المدة غير صالحة",
                                "يرجى إدخال عدد أيام صحيح."
                        );

                        return;
                    }

                    if (parsedDuration != null
                            && parsedDuration <= 0) {

                        showError(
                                "المدة غير صالحة",
                                "يجب أن تكون المدة أكبر من صفر."
                        );

                        return;
                    }

                    SubscriptionPlanRequest request =
                            new SubscriptionPlanRequest();

                    request.setPlanName(
                            name.getText().trim()
                    );

                    request.setDescription(
                            clean(description.getText())
                    );

                    request.setPrice(
                            parsedPrice
                    );

                    request.setCurrencyCode(
                            clean(currency.getText())
                    );

                    request.setDurationDays(
                            parsedDuration
                    );

                    request.setStatus(
                            status.getValue() == null
                                    ? "ACTIVE"
                                    : status.getValue()
                    );

                    updatePlan(
                            plan.getPlanId(),
                            request
                    );
                });
    }

    private void updatePlan(
            Long planId,
            SubscriptionPlanRequest request
    ) {

        CompletableFuture
                .runAsync(() -> {

                    try {

                        planAPI.update(
                                planId,
                                request
                        );

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
                .thenRun(() ->
                        Platform.runLater(() -> {

                            showInfo(
                                    "نجاح",
                                    "تم تعديل خطة الاشتراك."
                            );

                            loadPlans();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل تعديل الخطة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =====================================================
    // تغيير حالة الخطة
    // =====================================================

    private void changeSelectedStatus(
            String newStatus
    ) {

        PlanRow selected =
                getSelectedPlan();

        if (selected == null) {
            return;
        }

        String title =
                "ACTIVE".equals(newStatus)
                        ? "تفعيل الخطة"
                        : "تعطيل الخطة";

        String message =
                "ACTIVE".equals(newStatus)
                        ? "هل تريد تفعيل الخطة المحددة؟"
                        : "هل تريد تعطيل الخطة المحددة؟";

        if (!confirm(title, message)) {
            return;
        }

        CompletableFuture
                .runAsync(() -> {

                    try {

                        SubscriptionPlanResponse current =
                                planAPI.getById(
                                        selected.getPlanId()
                                );

                        SubscriptionPlanRequest request =
                                new SubscriptionPlanRequest();

                        request.setPlanName(
                                current.getPlanName()
                        );

                        request.setDescription(
                                current.getDescription()
                        );

                        request.setPrice(
                                current.getPrice()
                        );

                        request.setCurrencyCode(
                                current.getCurrencyCode()
                        );

                        request.setDurationDays(
                                current.getDurationDays()
                        );

                        request.setStatus(
                                newStatus
                        );

                        planAPI.update(
                                selected.getPlanId(),
                                request
                        );

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
                .thenRun(() ->
                        Platform.runLater(() -> {

                            showInfo(
                                    "تم",
                                    "تم تغيير حالة الخطة."
                            );

                            loadPlans();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل تغيير حالة الخطة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =====================================================
    // حذف الخطة
    // =====================================================

    private void deleteSelectedPlan() {

        PlanRow selected =
                getSelectedPlan();

        if (selected == null) {
            return;
        }

        if (!confirm(
                "حذف الخطة",
                "هل تريد حذف خطة الاشتراك المحددة؟\n\n"
                        + selected.nameProperty().get()
        )) {
            return;
        }

        CompletableFuture
                .runAsync(() -> {

                    try {

                        planAPI.delete(
                                selected.getPlanId()
                        );

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
                .thenRun(() ->
                        Platform.runLater(() -> {

                            showInfo(
                                    "تم",
                                    "تم حذف خطة الاشتراك."
                            );

                            loadPlans();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل حذف الخطة",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =====================================================
    // Helpers
    // =====================================================

    private PlanRow getSelectedPlan() {

        PlanRow selected =
                table.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showInfo(
                    "تحديد الخطة",
                    "اختر خطة أولًا."
            );
        }

        return selected;
    }

    private BigDecimal parsePrice(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {

            return null;
        }

        try {

            return new BigDecimal(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }

    private Integer parseDuration(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {

            return null;
        }

        try {

            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }

    private String clean(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isBlank()
                ? null
                : trimmed;
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
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

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

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

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        alert.showAndWait();
    }

    private String getRootCauseMessage(
            Throwable throwable
    ) {

        Throwable cause =
                throwable;

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
// Plan Row
// =============================================================

class PlanRow {

    private Long planId;

    private final StringProperty name =
            new SimpleStringProperty();

    private final StringProperty description =
            new SimpleStringProperty();

    private final StringProperty price =
            new SimpleStringProperty();

    private final StringProperty currency =
            new SimpleStringProperty();

    private final StringProperty duration =
            new SimpleStringProperty();

    private final StringProperty status =
            new SimpleStringProperty();

    public static PlanRow fromResponse(
            SubscriptionPlanResponse response
    ) {

        PlanRow row =
                new PlanRow();

        row.planId =
                response.getPlanId();

        row.name.set(
                response.getPlanName() == null
                        ? ""
                        : response.getPlanName()
        );

        row.description.set(
                response.getDescription() == null
                        ? ""
                        : response.getDescription()
        );

        row.price.set(
                response.getPrice() == null
                        ? ""
                        : response.getPrice().toPlainString()
        );

        row.currency.set(
                response.getCurrencyCode() == null
                        ? ""
                        : response.getCurrencyCode()
        );

        row.duration.set(
                response.getDurationDays() == null
                        ? ""
                        : response.getDurationDays()
                        + " يوم"
        );

        row.status.set(
                response.getStatus() == null
                        ? ""
                        : response.getStatus()
        );

        return row;
    }

    public Long getPlanId() {
        return planId;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty priceProperty() {
        return price;
    }

    public StringProperty currencyProperty() {
        return currency;
    }

    public StringProperty durationProperty() {
        return duration;
    }

    public StringProperty statusProperty() {
        return status;
    }
}