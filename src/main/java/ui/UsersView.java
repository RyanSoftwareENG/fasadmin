package ui;

import api.AdminApiManager;
import api.ClinicAPI;
import api.FasUserAPI;
import api.RolePermissionAPI;

import dto.ClinicResponse;
import dto.FasUserCreateRequest;
import dto.FasUserResponse;
import dto.FasUserUpdateRequest;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UsersView extends BorderPane {

    private final TableView<UserRow> table =
            new TableView<>();

    private final ObservableList<UserRow> rows =
            FXCollections.observableArrayList();

    private final FasUserAPI userAPI;
    private final ClinicAPI clinicAPI;
    private final RolePermissionAPI rolePermissionAPI;

    private final Map<Long, String> clinicNames =
            new HashMap<>();

    public UsersView() {

        AdminApiManager apiManager =
                AdminApiManager.getInstance();

        userAPI =
                apiManager.getFasUserAPI();

        clinicAPI =
                apiManager.getClinicAPI();

        rolePermissionAPI =
                apiManager.getRolePermissionAPI();

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setPadding(
                new Insets(25)
        );

        buildUI();

        loadData();
    }

    // =====================================================
    // إنشاء الواجهة
    // =====================================================

    private void buildUI() {

        Label title =
                new Label("المستخدمون");

        title.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
        """);

        Button add =
                new Button("➕ إضافة مستخدم");

        Button edit =
                new Button("✏ تعديل");

        Button changeRole =
                new Button("🔑 الدور");

        Button disable =
                new Button("⛔ تعطيل");

        Button activate =
                new Button("✅ تفعيل");

        HBox toolbar =
                new HBox(
                        10,
                        add,
                        edit,
                        changeRole,
                        disable,
                        activate
                );

        // =====================================================
        // أعمدة الجدول
        // =====================================================

        TableColumn<UserRow, String> username =
                new TableColumn<>(
                        "اسم المستخدم"
                );

        TableColumn<UserRow, String> fullName =
                new TableColumn<>(
                        "الاسم"
                );

        TableColumn<UserRow, String> clinic =
                new TableColumn<>(
                        "العيادة"
                );

        TableColumn<UserRow, String> role =
                new TableColumn<>(
                        "الدور"
                );

        TableColumn<UserRow, String> status =
                new TableColumn<>(
                        "الحالة"
                );

        username.setPrefWidth(160);
        fullName.setPrefWidth(200);
        clinic.setPrefWidth(200);
        role.setPrefWidth(150);
        status.setPrefWidth(120);

        username.setCellValueFactory(
                data ->
                        data.getValue()
                                .usernameProperty()
        );

        fullName.setCellValueFactory(
                data ->
                        data.getValue()
                                .fullNameProperty()
        );

        clinic.setCellValueFactory(
                data ->
                        data.getValue()
                                .clinicProperty()
        );

        role.setCellValueFactory(
                data ->
                        data.getValue()
                                .roleProperty()
        );

        status.setCellValueFactory(
                data ->
                        data.getValue()
                                .statusProperty()
        );

        table.getColumns().addAll(
                username,
                fullName,
                clinic,
                role,
                status
        );

        table.setItems(rows);

        table.setPlaceholder(
                new Label(
                        "لا توجد بيانات مستخدمين."
                )
        );

        // =====================================================
        // الأحداث
        // =====================================================

        add.setOnAction(
                e -> showAddUserDialog()
        );

        edit.setOnAction(
                e -> showEditUserDialog()
        );

        changeRole.setOnAction(
                e -> showChangeRoleDialog()
        );

        disable.setOnAction(
                e -> suspendSelectedUser()
        );

        activate.setOnAction(
                e -> activateSelectedUser()
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
    // تحميل المستخدمين والعيادات والأدوار
    // =====================================================

    private void loadData() {

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        List<ClinicResponse> clinics =
                                clinicAPI.getAllClinics();

                        List<FasUserResponse> users =
                                userAPI.getAllUsers();

                        List<RolePermissionAPI.RoleResponse>
                                roles =
                                rolePermissionAPI.getRoles();

                        return new LoadedData(
                                clinics,
                                users,
                                roles
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
                .thenAccept(data -> {

                    CompletableFuture
                            .supplyAsync(() ->
                                    buildUserRows(data)
                            )
                            .thenAccept(
                                    loadedRows ->
                                            Platform.runLater(() -> {

                                                rows.setAll(
                                                        loadedRows
                                                );
                                            })
                            )
                            .exceptionally(ex -> {

                                Platform.runLater(() ->
                                        showError(
                                                "تعذر تحميل أدوار المستخدمين",
                                                getRootCauseMessage(ex)
                                        )
                                );

                                return null;
                            });
                })
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "تعذر تحميل المستخدمين",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    private List<UserRow> buildUserRows(
            LoadedData data
    ) {

        clinicNames.clear();

        for (ClinicResponse clinic :
                data.clinics()) {

            clinicNames.put(
                    clinic.getClinicId(),
                    clinic.getClinicName()
            );
        }

        Map<Long, String> roleNames =
                new HashMap<>();

        for (
                RolePermissionAPI.RoleResponse role :
                data.roles()
        ) {

            roleNames.put(
                    role.getRoleId(),
                    role.getRoleName()
            );
        }

        List<UserRow> result =
                data.users()
                        .stream()
                        .map(user ->
                                UserRow.fromResponse(
                                        user,
                                        clinicNames
                                )
                        )
                        .toList();

        /*
         * تعيين الدور هنا غير ممكن مباشرة
         * لأن FasUserResponse لا يحتوي roleId.
         *
         * سيتم تحميل دور كل مستخدم بشكل مستقل
         * من endpoint /users/{id}/roles.
         */
        for (UserRow row : result) {

            try {

                List<RolePermissionAPI.RoleResponse>
                        userRoles =
                        userAPI.getUserRoles(
                                row.getUserId()
                        );

                if (!userRoles.isEmpty()) {

                    row.setRole(
                            userRoles.get(0)
                                    .getRoleName()
                    );

                } else {

                    row.setRole(
                            "بدون دور"
                    );
                }

            } catch (
                    IOException |
                    InterruptedException e
            ) {

                if (e instanceof InterruptedException) {
                    Thread.currentThread()
                            .interrupt();
                }

                row.setRole(
                        "غير متاح"
                );
            }
        }

        return result;
    }

    // =====================================================
    // إضافة مستخدم
    // =====================================================

    private void showAddUserDialog() {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "إضافة مستخدم"
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

        TextField username =
                new TextField();

        username.setPromptText(
                "اسم المستخدم"
        );

        PasswordField password =
                new PasswordField();

        password.setPromptText(
                "كلمة المرور"
        );

        TextField fullName =
                new TextField();

        fullName.setPromptText(
                "الاسم الكامل"
        );

        ComboBox<ClinicChoice> clinicCombo =
                new ComboBox<>();

        for (
                Map.Entry<Long, String> entry :
                clinicNames.entrySet()
        ) {

            clinicCombo.getItems().add(
                    new ClinicChoice(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        clinicCombo.setPromptText(
                "اختر العيادة"
        );

        box.getChildren().addAll(
                new Label("اسم المستخدم"),
                username,

                new Label("كلمة المرور"),
                password,

                new Label("الاسم الكامل"),
                fullName,

                new Label("العيادة"),
                clinicCombo
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

                    if (username.getText().isBlank()
                            || password.getText().isBlank()
                            || fullName.getText().isBlank()
                            || clinicCombo.getValue() == null) {

                        showError(
                                "بيانات غير مكتملة",
                                "جميع الحقول مطلوبة."
                        );

                        return;
                    }

                    FasUserCreateRequest request =
                            new FasUserCreateRequest();

                    request.setClinicId(
                            clinicCombo
                                    .getValue()
                                    .clinicId()
                    );

                    request.setUsername(
                            username.getText()
                                    .trim()
                    );

                    request.setPassword(
                            password.getText()
                    );

                    request.setFullName(
                            fullName.getText()
                                    .trim()
                    );

                    createUser(request);
                });
    }

    private void createUser(
            FasUserCreateRequest request
    ) {

        CompletableFuture
                .runAsync(() -> {

                    try {

                        userAPI.createUser(
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
                                    "تم إنشاء المستخدم بنجاح."
                            );

                            loadData();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل إنشاء المستخدم",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =====================================================
    // تعديل المستخدم
    // =====================================================

    private void showEditUserDialog() {

        UserRow selected =
                getSelectedUser();

        if (selected == null) {
            return;
        }

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        return userAPI.getUser(
                                selected.getUserId()
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
                .thenAccept(user ->
                        Platform.runLater(() ->
                                showEditDialog(user)
                        )
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "تعذر تحميل المستخدم",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    private void showEditDialog(
            FasUserResponse user
    ) {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "تعديل المستخدم"
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

        Label username =
                new Label(
                        "اسم المستخدم: "
                                + safe(
                                user.getUsername()
                        )
                );

        TextField fullName =
                new TextField(
                        safe(
                                user.getFullName()
                        )
                );

        PasswordField password =
                new PasswordField();

        password.setPromptText(
                "اتركه فارغًا للإبقاء على كلمة المرور الحالية"
        );

        box.getChildren().addAll(
                username,

                new Label("الاسم الكامل"),
                fullName,

                new Label("كلمة المرور الجديدة"),
                password
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

                    if (fullName.getText().isBlank()) {

                        showError(
                                "بيانات غير مكتملة",
                                "الاسم الكامل مطلوب."
                        );

                        return;
                    }

                    FasUserUpdateRequest request =
                            new FasUserUpdateRequest();

                    request.setFullName(
                            fullName.getText()
                                    .trim()
                    );

                    if (!password.getText().isBlank()) {

                        request.setPassword(
                                password.getText()
                        );
                    }

                    updateUser(
                            user.getUserId(),
                            request
                    );
                });
    }

    private void updateUser(
            Long userId,
            FasUserUpdateRequest request
    ) {

        CompletableFuture
                .runAsync(() -> {

                    try {

                        userAPI.updateUser(
                                userId,
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
                                    "تم تعديل المستخدم بنجاح."
                            );

                            loadData();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل تعديل المستخدم",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =====================================================
    // تغيير الدور
    // =====================================================

    private void showChangeRoleDialog() {

        UserRow selected =
                getSelectedUser();

        if (selected == null) {
            return;
        }

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        List<RolePermissionAPI.RoleResponse>
                                roles =
                                rolePermissionAPI.getRoles();

                        return roles;

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
                .thenAccept(roles ->
                        Platform.runLater(() ->
                                showRoleDialog(
                                        selected,
                                        roles
                                )
                        )
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "تعذر تحميل الأدوار",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    private void showRoleDialog(
            UserRow selected,
            List<RolePermissionAPI.RoleResponse> roles
    ) {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "تعيين دور المستخدم"
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

        Label userLabel =
                new Label(
                        "المستخدم: "
                                + selected
                                .usernameProperty()
                                .get()
                );

        ComboBox<RolePermissionAPI.RoleResponse>
                roleCombo =
                new ComboBox<>(
                        FXCollections.observableArrayList(
                                roles
                        )
                );

        roleCombo.setPromptText(
                "اختر الدور"
        );

        roleCombo.setMaxWidth(
                Double.MAX_VALUE
        );

        box.getChildren().addAll(
                userLabel,
                new Label("الدور"),
                roleCombo
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

                    RolePermissionAPI.RoleResponse
                            selectedRole =
                            roleCombo.getValue();

                    if (selectedRole == null) {

                        showError(
                                "بيانات غير مكتملة",
                                "يرجى اختيار الدور."
                        );

                        return;
                    }

                    updateUserRole(
                            selected.getUserId(),
                            selectedRole
                    );
                });
    }

    private void updateUserRole(
            Long userId,
            RolePermissionAPI.RoleResponse role
    ) {

        CompletableFuture
                .runAsync(() -> {

                    try {

                        userAPI.updateUserRole(
                                userId,
                                role.getRoleId()
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
                                    "تم تعيين الدور: "
                                            + role.getRoleName()
                            );

                            loadData();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل تعيين الدور",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =====================================================
    // تعليق المستخدم
    // =====================================================

    private void suspendSelectedUser() {

        UserRow selected =
                getSelectedUser();

        if (selected == null) {
            return;
        }

        if (!confirm(
                "تعطيل المستخدم",
                "هل تريد تعطيل المستخدم المحدد؟"
        )) {
            return;
        }

        CompletableFuture
                .runAsync(() -> {

                    try {

                        userAPI.suspendUser(
                                selected.getUserId()
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
                                    "تم تعطيل المستخدم."
                            );

                            loadData();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل تعطيل المستخدم",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =====================================================
    // تفعيل المستخدم
    // =====================================================

    private void activateSelectedUser() {

        UserRow selected =
                getSelectedUser();

        if (selected == null) {
            return;
        }

        if (!confirm(
                "تفعيل المستخدم",
                "هل تريد إعادة تفعيل المستخدم المحدد؟"
        )) {
            return;
        }

        CompletableFuture
                .runAsync(() -> {

                    try {

                        userAPI.activateUser(
                                selected.getUserId()
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
                                    "تم تفعيل المستخدم."
                            );

                            loadData();
                        })
                )
                .exceptionally(ex -> {

                    Platform.runLater(() ->
                            showError(
                                    "فشل تفعيل المستخدم",
                                    getRootCauseMessage(ex)
                            )
                    );

                    return null;
                });
    }

    // =====================================================
    // Helpers
    // =====================================================

    private UserRow getSelectedUser() {

        UserRow selected =
                table.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showInfo(
                    "تحديد المستخدم",
                    "اختر مستخدمًا أولًا."
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
            cause =
                    cause.getCause();
        }

        String message =
                cause.getMessage();

        return message == null
                || message.isBlank()
                ? "حدث خطأ غير معروف."
                : message;
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    // =====================================================
    // Records
    // =====================================================

    private record LoadedData(
            List<ClinicResponse> clinics,
            List<FasUserResponse> users,
            List<RolePermissionAPI.RoleResponse> roles
    ) {
    }

    private record ClinicChoice(
            Long clinicId,
            String clinicName
    ) {

        @Override
        public String toString() {
            return clinicName;
        }
    }
}


// =============================================================
// صف جدول المستخدمين
// =============================================================

class UserRow {

    private Long userId;

    private final StringProperty username =
            new SimpleStringProperty();

    private final StringProperty fullName =
            new SimpleStringProperty();

    private final StringProperty clinic =
            new SimpleStringProperty();

    private final StringProperty role =
            new SimpleStringProperty();

    private final StringProperty status =
            new SimpleStringProperty();

    public static UserRow fromResponse(
            FasUserResponse response,
            Map<Long, String> clinicNames
    ) {

        UserRow row =
                new UserRow();

        row.userId =
                response.getUserId();

        row.username.set(
                response.getUsername()
        );

        row.fullName.set(
                response.getFullName()
        );

        String clinicName =
                clinicNames.get(
                        response.getClinicId()
                );

        row.clinic.set(
                clinicName != null
                        ? clinicName
                        : "العيادة #"
                        + response.getClinicId()
        );

        row.role.set(
                "جاري التحميل..."
        );

        row.status.set(
                response.getStatus()
        );

        return row;
    }

    public Long getUserId() {
        return userId;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public StringProperty clinicProperty() {
        return clinic;
    }

    public StringProperty roleProperty() {
        return role;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setRole(
            String roleName
    ) {

        role.set(
                roleName == null
                        ? "بدون دور"
                        : roleName
        );
    }
}