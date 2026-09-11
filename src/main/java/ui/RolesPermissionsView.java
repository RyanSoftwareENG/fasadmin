package ui;

import api.AdminApiManager;
import api.RolePermissionAPI;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class RolesPermissionsView extends BorderPane {

    // =====================================================
    // API
    // =====================================================

    private final RolePermissionAPI api;

    // =====================================================
    // الأدوار النشطة
    // =====================================================

    private final ListView<RolePermissionAPI.RoleResponse>
            rolesList =
            new ListView<>();

    private final ObservableList<RolePermissionAPI.RoleResponse>
            roles =
            FXCollections.observableArrayList();

    // =====================================================
    // الأدوار المعطلة
    // =====================================================

    private final ListView<RolePermissionAPI.RoleResponse>
            disabledRolesList =
            new ListView<>();

    private final ObservableList<RolePermissionAPI.RoleResponse>
            disabledRoles =
            FXCollections.observableArrayList();

    // =====================================================
    // تبويبات الأدوار
    // =====================================================

    private final TabPane rolesTabs =
            new TabPane();

    // =====================================================
    // الصلاحيات
    // =====================================================

    private final VBox permissionsBox =
            new VBox(8);

    private final ScrollPane permissionsScroll =
            new ScrollPane();

    private final Map<Long, CheckBox>
            permissionCheckBoxes =
            new HashMap<>();

    // =====================================================
    // إدارة الأدوار
    // =====================================================

    private final Button addRoleButton =
            new Button("إضافة دور");

    private final Button editRoleButton =
            new Button("تعديل الدور");

    private final Button disableRoleButton =
            new Button("تعطيل الدور");

    private final Button enableRoleButton =
            new Button("تفعيل الدور");

    private final Button reloadButton =
            new Button("تحديث");

    // =====================================================
    // إدارة الصلاحيات
    // =====================================================

    private final Button savePermissionsButton =
            new Button("حفظ الصلاحيات");

    private final Button selectAllButton =
            new Button("تحديد الكل");

    private final Button clearAllButton =
            new Button("إلغاء الكل");

    // =====================================================
    // النصوص
    // =====================================================

    private final Label selectedRoleLabel =
            new Label("لم يتم اختيار دور");

    private final Label statusLabel =
            new Label();

    // =====================================================
    // التحميل
    // =====================================================

    private final ProgressIndicator loadingIndicator =
            new ProgressIndicator();

    private final AtomicLong permissionLoadVersion =
            new AtomicLong(0);

    // =====================================================
    // Constructor
    // =====================================================

    public RolesPermissionsView() {

        api =
                AdminApiManager
                        .getInstance()
                        .getRolePermissionAPI();

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setPadding(
                new Insets(25)
        );

        setStyle("""
            -fx-background-color: #f4f6f8;
        """);

        buildUI();

        loadAllRoles();
    }

    // =====================================================
    // بناء الواجهة
    // =====================================================

    private void buildUI() {

        setTop(
                createHeader()
        );

        setCenter(
                createMainContent()
        );
    }

    // =====================================================
    // Header
    // =====================================================

    private Node createHeader() {

        Label title =
                new Label(
                        "الأدوار والصلاحيات"
                );

        title.setStyle("""
            -fx-font-size: 26px;
            -fx-font-weight: bold;
            -fx-text-fill: #263238;
        """);

        Label subtitle =
                new Label(
                        "إدارة الأدوار وإسناد وظائف النظام لكل دور"
                );

        subtitle.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #607d8b;
        """);

        VBox text =
                new VBox(
                        5,
                        title,
                        subtitle
                );

        reloadButton.setPrefSize(
                100,
                38
        );

        reloadButton.setStyle("""
            -fx-background-color: #1565c0;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 6px;
            -fx-cursor: hand;
        """);

        reloadButton.setOnAction(
                event -> loadAllRoles()
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        HBox header =
                new HBox(
                        15,
                        text,
                        spacer,
                        reloadButton
                );

        header.setAlignment(
                Pos.CENTER_RIGHT
        );

        header.setPadding(
                new Insets(
                        0,
                        0,
                        20,
                        0
                )
        );

        return header;
    }

    // =====================================================
    // المحتوى الرئيسي
    // =====================================================

    private Node createMainContent() {

        VBox rolesPane =
                createRolesPane();

        VBox permissionsPane =
                createPermissionsPane();

        HBox content =
                new HBox(
                        20,
                        rolesPane,
                        permissionsPane
                );

        HBox.setHgrow(
                permissionsPane,
                Priority.ALWAYS
        );

        return content;
    }

    // =====================================================
    // قسم الأدوار
    // =====================================================

    private VBox createRolesPane() {

        Label title =
                new Label(
                        "الأدوار"
                );

        title.setStyle("""
            -fx-font-size: 19px;
            -fx-font-weight: bold;
            -fx-text-fill: #263238;
        """);

        Label hint =
                new Label(
                        "يمكن إنشاء وتعديل وتعطيل الأدوار"
                );

        hint.setWrapText(true);

        hint.setStyle("""
            -fx-font-size: 13px;
            -fx-text-fill: #78909c;
        """);

        // =================================================
        // القوائم
        // =================================================

        rolesList.setItems(
                roles
        );

        disabledRolesList.setItems(
                disabledRoles
        );

        configureRoleList(
                rolesList
        );

        configureRoleList(
                disabledRolesList
        );

        // =================================================
        // تبويب النشطة
        // =================================================

        Tab activeTab =
                new Tab(
                        "الأدوار النشطة"
                );

        activeTab.setClosable(
                false
        );

        activeTab.setContent(
                rolesList
        );

        // =================================================
        // تبويب المعطلة
        // =================================================

        Tab disabledTab =
                new Tab(
                        "الأدوار المعطلة"
                );

        disabledTab.setClosable(
                false
        );

        disabledTab.setContent(
                disabledRolesList
        );

        // =================================================
        // TabPane
        // =================================================

        rolesTabs.getTabs()
                .setAll(
                        activeTab,
                        disabledTab
                );

        rolesTabs.setTabClosingPolicy(
                TabPane.TabClosingPolicy.UNAVAILABLE
        );

        rolesTabs.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldTab, newTab) -> {

                            if (newTab == null) {
                                return;
                            }

                            if (newTab == activeTab) {

                                clearDisabledRoleSelection();

                                RolePermissionAPI.RoleResponse
                                        selected =
                                        rolesList
                                                .getSelectionModel()
                                                .getSelectedItem();

                                updateActiveRoleButtons(
                                        selected
                                );

                            } else {

                                clearActiveRoleSelection();

                                RolePermissionAPI.RoleResponse
                                        selected =
                                        disabledRolesList
                                                .getSelectionModel()
                                                .getSelectedItem();

                                updateDisabledRoleButtons(
                                        selected
                                );
                            }
                        }
                );

        // =================================================
        // اختيار دور نشط
        // =================================================

        rolesList.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldRole, newRole) -> {

                            if (newRole == null) {

                                editRoleButton.setDisable(
                                        true
                                );

                                disableRoleButton.setDisable(
                                        true
                                );

                                selectedRoleLabel.setText(
                                        "لم يتم اختيار دور"
                                );

                                return;
                            }

                            rolesTabs.getSelectionModel()
                                    .select(0);

                            selectedRoleLabel.setText(
                                    "الدور: "
                                            + newRole.getRoleName()
                            );

                            editRoleButton.setDisable(
                                    false
                            );

                            disableRoleButton.setDisable(
                                    false
                            );

                            enableRoleButton.setDisable(
                                    true
                            );

                            loadRolePermissions(
                                    newRole.getRoleId(),
                                    newRole.getRoleName()
                            );
                        }
                );

        // =================================================
        // اختيار دور معطل
        // =================================================

        disabledRolesList.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldRole, newRole) -> {

                            if (newRole == null) {

                                enableRoleButton.setDisable(
                                        true
                                );

                                selectedRoleLabel.setText(
                                        "لم يتم اختيار دور"
                                );

                                return;
                            }

                            rolesTabs.getSelectionModel()
                                    .select(1);

                            invalidatePermissionLoad();

                            clearPermissionControls();

                            selectedRoleLabel.setText(
                                    "الدور المعطل: "
                                            + newRole.getRoleName()
                            );

                            enableRoleButton.setDisable(
                                    false
                            );

                            editRoleButton.setDisable(
                                    true
                            );

                            disableRoleButton.setDisable(
                                    true
                            );

                            savePermissionsButton.setDisable(
                                    true
                            );

                            selectAllButton.setDisable(
                                    true
                            );

                            clearAllButton.setDisable(
                                    true
                            );

                            loadingIndicator.setVisible(
                                    false
                            );

                            loadingIndicator.setManaged(
                                    false
                            );

                            permissionsBox
                                    .getChildren()
                                    .setAll(
                                            createMessageLabel(
                                                    "الدور معطل. فعّل الدور أولًا لإدارة صلاحياته."
                                            )
                                    );
                        }
                );

        // =================================================
        // أزرار إدارة الأدوار
        // =================================================

        configureRoleButtons();

        // =================================================
        // صندوق الأزرار
        // =================================================

        VBox roleActions =
                new VBox(
                        8,
                        addRoleButton,
                        editRoleButton,
                        disableRoleButton,
                        enableRoleButton
                );

        roleActions.setFillWidth(
                true
        );

        // =================================================
        // الحاوية
        // =================================================

        VBox box =
                new VBox(
                        8,
                        title,
                        hint,
                        rolesTabs,
                        roleActions
                );

        box.setPadding(
                new Insets(18)
        );

        box.setPrefWidth(
                300
        );

        box.setMinWidth(
                280
        );

        box.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10px;
            -fx-border-color: #e0e0e0;
            -fx-border-radius: 10px;
        """);

        VBox.setVgrow(
                rolesTabs,
                Priority.ALWAYS
        );

        return box;
    }

    // =====================================================
    // إعداد قوائم الأدوار
    // =====================================================

    private void configureRoleList(
            ListView<RolePermissionAPI.RoleResponse> list
    ) {

        list.setPrefHeight(
                300
        );

        list.setPlaceholder(
                new Label(
                        "لا توجد أدوار."
                )
        );

        list.setCellFactory(
                view ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    RolePermissionAPI.RoleResponse item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty ||
                                        item == null) {

                                    setText(null);
                                    setStyle("");

                                    return;
                                }

                                setText(
                                        item.getRoleName()
                                );

                                setStyle("""
                                    -fx-font-size: 15px;
                                    -fx-padding: 12px;
                                """);
                            }
                        }
        );
    }

    // =====================================================
    // أزرار إدارة الأدوار
    // =====================================================

    private void configureRoleButtons() {

        addRoleButton.setPrefHeight(
                38
        );

        editRoleButton.setPrefHeight(
                38
        );

        disableRoleButton.setPrefHeight(
                38
        );

        enableRoleButton.setPrefHeight(
                38
        );

        addRoleButton.setStyle("""
            -fx-background-color: #2e7d32;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 6px;
            -fx-cursor: hand;
        """);

        editRoleButton.setStyle("""
            -fx-background-color: #1565c0;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 6px;
            -fx-cursor: hand;
        """);

        disableRoleButton.setStyle("""
            -fx-background-color: #c62828;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 6px;
            -fx-cursor: hand;
        """);

        enableRoleButton.setStyle("""
            -fx-background-color: #2e7d32;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 6px;
            -fx-cursor: hand;
        """);

        addRoleButton.setOnAction(
                event -> showAddRoleDialog()
        );

        editRoleButton.setOnAction(
                event -> showEditRoleDialog()
        );

        disableRoleButton.setOnAction(
                event -> disableSelectedRole()
        );

        enableRoleButton.setOnAction(
                event -> enableSelectedRole()
        );

        editRoleButton.setDisable(
                true
        );

        disableRoleButton.setDisable(
                true
        );

        enableRoleButton.setDisable(
                true
        );
    }

    // =====================================================
    // قسم الصلاحيات
    // =====================================================

    private VBox createPermissionsPane() {

        Label title =
                new Label(
                        "وظائف الدور"
                );

        title.setStyle("""
            -fx-font-size: 19px;
            -fx-font-weight: bold;
            -fx-text-fill: #263238;
        """);

        selectedRoleLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-text-fill: #1565c0;
        """);

        // =================================================
        // أزرار الصلاحيات
        // =================================================

        selectAllButton.setPrefHeight(
                36
        );

        clearAllButton.setPrefHeight(
                36
        );

        selectAllButton.setStyle("""
            -fx-background-color: #eceff1;
            -fx-text-fill: #263238;
            -fx-font-weight: bold;
            -fx-background-radius: 6px;
            -fx-cursor: hand;
        """);

        clearAllButton.setStyle("""
            -fx-background-color: #eceff1;
            -fx-text-fill: #263238;
            -fx-font-weight: bold;
            -fx-background-radius: 6px;
            -fx-cursor: hand;
        """);

        selectAllButton.setOnAction(
                event -> selectAllPermissions()
        );

        clearAllButton.setOnAction(
                event -> clearAllPermissions()
        );

        HBox permissionControls =
                new HBox(
                        8,
                        selectAllButton,
                        clearAllButton
                );

        permissionControls.setAlignment(
                Pos.CENTER_RIGHT
        );

        // =================================================
        // ScrollPane
        // =================================================

        permissionsBox.setPadding(
                new Insets(15)
        );

        permissionsBox.setSpacing(
                8
        );

        permissionsBox.setFillWidth(
                true
        );

        permissionsBox.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        permissionsScroll.setContent(
                permissionsBox
        );

        permissionsScroll.setFitToWidth(
                true
        );

        permissionsScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        permissionsScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        // =================================================
        // زر الحفظ
        // =================================================

        savePermissionsButton.setPrefWidth(
                180
        );

        savePermissionsButton.setPrefHeight(
                42
        );

        savePermissionsButton.setDisable(
                true
        );

        savePermissionsButton.setStyle("""
            -fx-background-color: #2e7d32;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-background-radius: 6px;
            -fx-cursor: hand;
        """);

        savePermissionsButton.setOnAction(
                event -> savePermissions()
        );

        // =================================================
        // التحميل
        // =================================================

        loadingIndicator.setPrefSize(
                22,
                22
        );

        loadingIndicator.setVisible(
                false
        );

        loadingIndicator.setManaged(
                false
        );

        // =================================================
        // الحالة
        // =================================================

        HBox bottom =
                new HBox(
                        10,
                        savePermissionsButton,
                        loadingIndicator,
                        statusLabel
                );

        bottom.setAlignment(
                Pos.CENTER_RIGHT
        );

        bottom.setPadding(
                new Insets(
                        12,
                        0,
                        0,
                        0
                )
        );

        // =================================================
        // الحاوية
        // =================================================

        VBox box =
                new VBox(
                        8,
                        title,
                        selectedRoleLabel,
                        permissionControls,
                        permissionsScroll,
                        bottom
                );

        box.setPadding(
                new Insets(18)
        );

        box.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10px;
            -fx-border-color: #e0e0e0;
            -fx-border-radius: 10px;
        """);

        VBox.setVgrow(
                permissionsScroll,
                Priority.ALWAYS
        );

        return box;
    }

    // =====================================================
    // تحميل جميع الأدوار
    // =====================================================

    private void loadAllRoles() {

        reloadButton.setDisable(true);

        addRoleButton.setDisable(true);
        editRoleButton.setDisable(true);
        disableRoleButton.setDisable(true);
        enableRoleButton.setDisable(true);

        rolesList.setDisable(true);
        disabledRolesList.setDisable(true);

        invalidatePermissionLoad();

        clearPermissions();

        setStatus(
                "جاري تحميل الأدوار...",
                false
        );

        Thread thread =
                new Thread(
                        () -> {

                            try {

                                List<RolePermissionAPI.RoleResponse>
                                        active =
                                        api.getRoles();

                                List<RolePermissionAPI.RoleResponse>
                                        disabled =
                                        api.getDisabledRoles();

                                Platform.runLater(
                                        () -> {

                                            roles.setAll(
                                                    active == null
                                                            ? List.of()
                                                            : active
                                            );

                                            disabledRoles.setAll(
                                                    disabled == null
                                                            ? List.of()
                                                            : disabled
                                            );

                                            rolesList.setDisable(false);
                                            disabledRolesList.setDisable(false);
                                            reloadButton.setDisable(false);
                                            addRoleButton.setDisable(false);

                                            enableRoleButton.setDisable(true);

                                            if (roles.isEmpty()) {

                                                selectedRoleLabel.setText(
                                                        "لا توجد أدوار نشطة"
                                                );

                                            }

                                            if (disabledRoles.isEmpty()) {

                                                // لا شيء
                                            }

                                            if (!roles.isEmpty()) {

                                                rolesTabs
                                                        .getSelectionModel()
                                                        .select(0);

                                                rolesList
                                                        .getSelectionModel()
                                                        .select(0);

                                            } else if (!disabledRoles.isEmpty()) {

                                                rolesTabs
                                                        .getSelectionModel()
                                                        .select(1);

                                                disabledRolesList
                                                        .getSelectionModel()
                                                        .select(0);

                                            } else {

                                                selectedRoleLabel.setText(
                                                        "لا توجد أدوار"
                                                );

                                                permissionsBox
                                                        .getChildren()
                                                        .setAll(
                                                                createMessageLabel(
                                                                        "أضف دورًا جديدًا للبدء."
                                                                )
                                                        );

                                                loadingIndicator.setVisible(false);
                                                loadingIndicator.setManaged(false);

                                                setStatus(
                                                        "لا توجد أدوار.",
                                                        false
                                                );
                                            }
                                        }
                                );

                            } catch (
                                    IOException |
                                    InterruptedException e
                            ) {

                                if (e instanceof InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                Platform.runLater(
                                        () -> {

                                            rolesList.setDisable(false);
                                            disabledRolesList.setDisable(false);
                                            reloadButton.setDisable(false);
                                            addRoleButton.setDisable(false);

                                            showError(
                                                    "فشل تحميل الأدوار",
                                                    e
                                            );
                                        }
                                );
                            }

                        },
                        "FAS-Roles-Loader"
                );

        thread.setDaemon(true);

        thread.start();
    }

    // =====================================================
    // تحميل صلاحيات الدور
    // =====================================================

    private void loadRolePermissions(
            Long roleId,
            String roleName
    ) {

        if (roleId == null ||
                roleId <= 0) {

            return;
        }

        long requestVersion =
                permissionLoadVersion.incrementAndGet();

        clearPermissionControls();

        setStatus(
                "جاري تحميل وظائف "
                        + roleName
                        + "...",
                false
        );

        Thread thread =
                new Thread(
                        () -> {

                            try {

                                List<RolePermissionAPI.RolePermissionResponse>
                                        result =
                                        api.getRolePermissions(
                                                roleId
                                        );

                                Platform.runLater(
                                        () -> {

                                            if (requestVersion
                                                    != permissionLoadVersion.get()) {

                                                return;
                                            }

                                            RolePermissionAPI.RoleResponse
                                                    selected =
                                                    rolesList
                                                            .getSelectionModel()
                                                            .getSelectedItem();

                                            if (selected == null ||
                                                    !roleId.equals(
                                                            selected.getRoleId()
                                                    )) {

                                                return;
                                            }

                                            permissionCheckBoxes.clear();

                                            permissionsBox
                                                    .getChildren()
                                                    .clear();

                                            loadingIndicator.setVisible(false);
                                            loadingIndicator.setManaged(false);

                                            if (result == null ||
                                                    result.isEmpty()) {

                                                permissionsBox
                                                        .getChildren()
                                                        .add(
                                                                createEmptyPermissionsMessage()
                                                        );

                                                savePermissionsButton.setDisable(true);
                                                selectAllButton.setDisable(true);
                                                clearAllButton.setDisable(true);

                                                setStatus(
                                                        "لا توجد صلاحيات نشطة معرفة.",
                                                        false
                                                );

                                                return;
                                            }

                                            for (
                                                    RolePermissionAPI.RolePermissionResponse
                                                            permission :
                                                    result
                                            ) {

                                                if (permission == null ||
                                                        permission.getPermissionId() == null) {

                                                    continue;
                                                }

                                                CheckBox checkBox =
                                                        createPermissionCheckBox(
                                                                permission
                                                        );

                                                permissionCheckBoxes.put(
                                                        permission.getPermissionId(),
                                                        checkBox
                                                );

                                                permissionsBox
                                                        .getChildren()
                                                        .add(
                                                                checkBox
                                                        );
                                            }

                                            boolean available =
                                                    !permissionCheckBoxes.isEmpty();

                                            savePermissionsButton.setDisable(
                                                    !available
                                            );

                                            selectAllButton.setDisable(
                                                    !available
                                            );

                                            clearAllButton.setDisable(
                                                    !available
                                            );

                                            setStatus(
                                                    "تم تحميل "
                                                            + permissionCheckBoxes.size()
                                                            + " وظيفة للدور "
                                                            + roleName
                                                            + ".",
                                                    false
                                            );
                                        }
                                );

                            } catch (
                                    IOException |
                                    InterruptedException e
                            ) {

                                if (e instanceof InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                Platform.runLater(
                                        () -> {

                                            if (requestVersion
                                                    != permissionLoadVersion.get()) {

                                                return;
                                            }

                                            loadingIndicator.setVisible(false);
                                            loadingIndicator.setManaged(false);

                                            savePermissionsButton.setDisable(true);
                                            selectAllButton.setDisable(true);
                                            clearAllButton.setDisable(true);

                                            showError(
                                                    "فشل تحميل وظائف الدور",
                                                    e
                                            );
                                        }
                                );
                            }

                        },
                        "FAS-Permissions-Loader"
                );

        thread.setDaemon(true);

        thread.start();
    }

    // =====================================================
    // CheckBox
    // =====================================================

    private CheckBox createPermissionCheckBox(
            RolePermissionAPI.RolePermissionResponse permission
    ) {

        String name =
                permission.getPermissionName();

        if (name == null ||
                name.isBlank()) {

            name =
                    permission.getPermissionCode();
        }

        CheckBox checkBox =
                new CheckBox(
                        name
                );

        checkBox.setSelected(
                permission.isAssigned()
        );

        checkBox.setUserData(
                permission.getPermissionId()
        );

        checkBox.setMaxWidth(
                Double.MAX_VALUE
        );

        checkBox.setPrefHeight(
                40
        );

        checkBox.setStyle("""
            -fx-font-size: 14px;
            -fx-padding: 8px 12px;
            -fx-cursor: hand;
        """);

        String code =
                permission.getPermissionCode();

        String tooltip =
                permission.getPermissionName() == null
                        ? ""
                        : permission.getPermissionName();

        if (code != null &&
                !code.isBlank()) {

            if (!tooltip.isBlank()) {
                tooltip += "\n";
            }

            tooltip +=
                    "Code: "
                            + code;
        }

        checkBox.setTooltip(
                new Tooltip(
                        tooltip
                )
        );

        return checkBox;
    }

    // =====================================================
    // تحديد الكل
    // =====================================================

    private void selectAllPermissions() {

        permissionCheckBoxes
                .values()
                .forEach(
                        checkBox ->
                                checkBox.setSelected(true)
                );
    }

    // =====================================================
    // إلغاء الكل
    // =====================================================

    private void clearAllPermissions() {

        permissionCheckBoxes
                .values()
                .forEach(
                        checkBox ->
                                checkBox.setSelected(false)
                );
    }

    // =====================================================
    // حفظ الصلاحيات
    // =====================================================

    private void savePermissions() {

        RolePermissionAPI.RoleResponse selectedRole =
                rolesList
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedRole == null ||
                selectedRole.getRoleId() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "تنبيه",
                    "يرجى اختيار دور نشط أولًا."
            );

            return;
        }

        if (loadingIndicator.isVisible()) {
            return;
        }

        Set<Long> selectedPermissionIds =
                new HashSet<>();

        for (
                Map.Entry<Long, CheckBox> entry :
                permissionCheckBoxes.entrySet()
        ) {

            if (entry.getValue().isSelected()) {

                selectedPermissionIds.add(
                        entry.getKey()
                );
            }
        }

        Long roleId =
                selectedRole.getRoleId();

        String roleName =
                selectedRole.getRoleName();

        savePermissionsButton.setDisable(true);
        selectAllButton.setDisable(true);
        clearAllButton.setDisable(true);

        addRoleButton.setDisable(true);
        editRoleButton.setDisable(true);
        disableRoleButton.setDisable(true);
        reloadButton.setDisable(true);

        setStatus(
                "جاري حفظ صلاحيات "
                        + roleName
                        + "...",
                false
        );

        Thread thread =
                new Thread(
                        () -> {

                            try {

                                api.updateRolePermissions(
                                        roleId,
                                        selectedPermissionIds
                                );

                                Platform.runLater(
                                        () -> {

                                            addRoleButton.setDisable(false);
                                            editRoleButton.setDisable(false);
                                            disableRoleButton.setDisable(false);
                                            reloadButton.setDisable(false);

                                            savePermissionsButton.setDisable(false);
                                            selectAllButton.setDisable(false);
                                            clearAllButton.setDisable(false);

                                            setStatus(
                                                    "تم حفظ صلاحيات "
                                                            + roleName
                                                            + " بنجاح.",
                                                    false
                                            );
                                        }
                                );

                            } catch (
                                    IOException |
                                    InterruptedException e
                            ) {

                                if (e instanceof InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                Platform.runLater(
                                        () -> {

                                            addRoleButton.setDisable(false);
                                            reloadButton.setDisable(false);

                                            RolePermissionAPI.RoleResponse
                                                    current =
                                                    rolesList
                                                            .getSelectionModel()
                                                            .getSelectedItem();

                                            editRoleButton.setDisable(
                                                    current == null
                                            );

                                            disableRoleButton.setDisable(
                                                    current == null
                                            );

                                            savePermissionsButton.setDisable(
                                                    permissionCheckBoxes.isEmpty()
                                            );

                                            selectAllButton.setDisable(
                                                    permissionCheckBoxes.isEmpty()
                                            );

                                            clearAllButton.setDisable(
                                                    permissionCheckBoxes.isEmpty()
                                            );

                                            showError(
                                                    "فشل حفظ الصلاحيات",
                                                    e
                                            );
                                        }
                                );
                            }

                        },
                        "FAS-Permissions-Saver"
                );

        thread.setDaemon(true);

        thread.start();
    }

    // =====================================================
    // إضافة دور
    // =====================================================

    private void showAddRoleDialog() {

        RoleDialogResult result =
                showRoleDialog(
                        "إضافة دور",
                        "إضافة دور جديد",
                        null,
                        null
                );

        if (result == null) {
            return;
        }

        setRoleManagementDisabled(
                true
        );

        setStatus(
                "جاري إنشاء الدور...",
                false
        );

        Thread thread =
                new Thread(
                        () -> {

                            try {

                                api.createRole(
                                        result.roleName(),
                                        result.description()
                                );

                                Platform.runLater(
                                        () -> {

                                            setRoleManagementDisabled(false);

                                            setStatus(
                                                    "تم إنشاء الدور بنجاح.",
                                                    false
                                            );

                                            loadAllRoles();
                                        }
                                );

                            } catch (
                                    IOException |
                                    InterruptedException |
                                    RuntimeException e
                            ) {

                                if (e instanceof InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                Platform.runLater(
                                        () -> {

                                            setRoleManagementDisabled(false);

                                            showError(
                                                    "فشل إنشاء الدور",
                                                    e
                                            );
                                        }
                                );
                            }

                        },
                        "FAS-Role-Creator"
                );

        thread.setDaemon(true);

        thread.start();
    }

    // =====================================================
    // تعديل دور
    // =====================================================

    private void showEditRoleDialog() {

        RolePermissionAPI.RoleResponse selectedRole =
                rolesList
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedRole == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "تنبيه",
                    "يرجى اختيار دور نشط أولًا."
            );

            return;
        }

        RoleDialogResult result =
                showRoleDialog(
                        "تعديل الدور",
                        "تعديل بيانات الدور",
                        selectedRole.getRoleName(),
                        selectedRole.getDescription()
                );

        if (result == null) {
            return;
        }

        Long roleId =
                selectedRole.getRoleId();

        setRoleManagementDisabled(
                true
        );

        setStatus(
                "جاري تعديل الدور...",
                false
        );

        Thread thread =
                new Thread(
                        () -> {

                            try {

                                api.updateRole(
                                        roleId,
                                        result.roleName(),
                                        result.description()
                                );

                                Platform.runLater(
                                        () -> {

                                            setRoleManagementDisabled(false);

                                            setStatus(
                                                    "تم تعديل الدور بنجاح.",
                                                    false
                                            );

                                            loadAllRoles();
                                        }
                                );

                            } catch (
                                    IOException |
                                    InterruptedException |
                                    RuntimeException e
                            ) {

                                if (e instanceof InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                Platform.runLater(
                                        () -> {

                                            setRoleManagementDisabled(false);

                                            showError(
                                                    "فشل تعديل الدور",
                                                    e
                                            );
                                        }
                                );
                            }

                        },
                        "FAS-Role-Updater"
                );

        thread.setDaemon(true);

        thread.start();
    }

    // =====================================================
    // تعطيل الدور
    // =====================================================

    private void disableSelectedRole() {

        RolePermissionAPI.RoleResponse selectedRole =
                rolesList
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedRole == null ||
                selectedRole.getRoleId() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "تنبيه",
                    "يرجى اختيار دور نشط أولًا."
            );

            return;
        }

        String roleName =
                selectedRole.getRoleName();

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "تعطيل الدور"
        );

        confirmation.setHeaderText(
                "تعطيل الدور: "
                        + roleName
        );

        confirmation.setContentText(
                "سيصبح الدور غير نشط ولن يظهر ضمن الأدوار النشطة."
        );

        confirmation.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        ButtonType disable =
                new ButtonType(
                        "تعطيل"
                );

        ButtonType cancel =
                new ButtonType(
                        "إلغاء",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        confirmation.getButtonTypes()
                .setAll(
                        disable,
                        cancel
                );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty() ||
                result.get() != disable) {

            return;
        }

        Long roleId =
                selectedRole.getRoleId();

        setRoleManagementDisabled(
                true
        );

        setStatus(
                "جاري تعطيل الدور...",
                false
        );

        Thread thread =
                new Thread(
                        () -> {

                            try {

                                api.disableRole(
                                        roleId
                                );

                                Platform.runLater(
                                        () -> {

                                            setRoleManagementDisabled(false);

                                            setStatus(
                                                    "تم تعطيل الدور بنجاح.",
                                                    false
                                            );

                                            loadAllRoles();
                                        }
                                );

                            } catch (
                                    IOException |
                                    InterruptedException e
                            ) {

                                if (e instanceof InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                Platform.runLater(
                                        () -> {

                                            setRoleManagementDisabled(false);

                                            showError(
                                                    "فشل تعطيل الدور",
                                                    e
                                            );
                                        }
                                );
                            }

                        },
                        "FAS-Role-Disabler"
                );

        thread.setDaemon(true);

        thread.start();
    }

    // =====================================================
    // تفعيل الدور
    // =====================================================

    private void enableSelectedRole() {

        RolePermissionAPI.RoleResponse selectedRole =
                disabledRolesList
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedRole == null ||
                selectedRole.getRoleId() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "تنبيه",
                    "يرجى اختيار دور معطل أولًا."
            );

            return;
        }

        String roleName =
                selectedRole.getRoleName();

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "تفعيل الدور"
        );

        confirmation.setHeaderText(
                "تفعيل الدور: "
                        + roleName
        );

        confirmation.setContentText(
                "سيعود الدور إلى قائمة الأدوار النشطة."
        );

        confirmation.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        ButtonType enable =
                new ButtonType(
                        "تفعيل"
                );

        ButtonType cancel =
                new ButtonType(
                        "إلغاء",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        confirmation.getButtonTypes()
                .setAll(
                        enable,
                        cancel
                );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty() ||
                result.get() != enable) {

            return;
        }

        Long roleId =
                selectedRole.getRoleId();

        enableRoleButton.setDisable(
                true
        );

        setStatus(
                "جاري تفعيل الدور...",
                false
        );

        Thread thread =
                new Thread(
                        () -> {

                            try {

                                api.enableRole(
                                        roleId
                                );

                                Platform.runLater(
                                        () -> {

                                            setStatus(
                                                    "تم تفعيل الدور بنجاح.",
                                                    false
                                            );

                                            loadAllRoles();
                                        }
                                );

                            } catch (
                                    IOException |
                                    InterruptedException e
                            ) {

                                if (e instanceof InterruptedException) {

                                    Thread.currentThread()
                                            .interrupt();
                                }

                                Platform.runLater(
                                        () -> {

                                            enableRoleButton.setDisable(
                                                    false
                                            );

                                            showError(
                                                    "فشل تفعيل الدور",
                                                    e
                                            );
                                        }
                                );
                            }

                        },
                        "FAS-Role-Enabler"
                );

        thread.setDaemon(true);

        thread.start();
    }

    // =====================================================
    // نافذة الدور
    // =====================================================

    private RoleDialogResult showRoleDialog(
            String title,
            String header,
            String currentName,
            String currentDescription
    ) {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.initModality(
                Modality.APPLICATION_MODAL
        );

        dialog.setTitle(
                title
        );

        dialog.setHeaderText(
                header
        );

        dialog.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        TextField roleNameField =
                new TextField();

        roleNameField.setPromptText(
                "اسم الدور"
        );

        if (currentName != null) {

            roleNameField.setText(
                    currentName
            );
        }

        TextArea descriptionField =
                new TextArea();

        descriptionField.setPromptText(
                "وصف الدور"
        );

        descriptionField.setPrefRowCount(
                3
        );

        if (currentDescription != null) {

            descriptionField.setText(
                    currentDescription
            );
        }

        GridPane form =
                new GridPane();

        form.setHgap(
                10
        );

        form.setVgap(
                12
        );

        form.setPadding(
                new Insets(15)
        );

        form.add(
                new Label("اسم الدور"),
                0,
                0
        );

        form.add(
                roleNameField,
                1,
                0
        );

        form.add(
                new Label("الوصف"),
                0,
                1
        );

        form.add(
                descriptionField,
                1,
                1
        );

        ColumnConstraints labelColumn =
                new ColumnConstraints();

        labelColumn.setMinWidth(
                90
        );

        ColumnConstraints fieldColumn =
                new ColumnConstraints();

        fieldColumn.setHgrow(
                Priority.ALWAYS
        );

        form.getColumnConstraints()
                .addAll(
                        labelColumn,
                        fieldColumn
                );

        dialog.getDialogPane()
                .setContent(
                        form
                );

        ButtonType save =
                new ButtonType(
                        "حفظ",
                        ButtonBar.ButtonData.OK_DONE
                );

        ButtonType cancel =
                new ButtonType(
                        "إلغاء",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .setAll(
                        save,
                        cancel
                );

        Node saveNode =
                dialog.getDialogPane()
                        .lookupButton(
                                save
                        );

        Runnable validate =
                () -> {

                    String value =
                            roleNameField.getText();

                    saveNode.setDisable(
                            value == null ||
                                    value.trim().isBlank()
                    );
                };

        roleNameField.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                validate.run()
                );

        validate.run();

        Optional<ButtonType> result =
                dialog.showAndWait();

        if (result.isEmpty() ||
                result.get() != save) {

            return null;
        }

        String roleName =
                roleNameField
                        .getText()
                        .trim();

        String description =
                descriptionField
                        .getText()
                        .trim();

        if (description.isBlank()) {
            description = null;
        }

        return new RoleDialogResult(
                roleName,
                description
        );
    }

    // =====================================================
    // تحديث أزرار الدور النشط
    // =====================================================

    private void updateActiveRoleButtons(
            RolePermissionAPI.RoleResponse role
    ) {

        boolean selected =
                role != null;

        editRoleButton.setDisable(
                !selected
        );

        disableRoleButton.setDisable(
                !selected
        );

        enableRoleButton.setDisable(
                true
        );
    }

    // =====================================================
    // تحديث أزرار الدور المعطل
    // =====================================================

    private void updateDisabledRoleButtons(
            RolePermissionAPI.RoleResponse role
    ) {

        boolean selected =
                role != null;

        editRoleButton.setDisable(
                true
        );

        disableRoleButton.setDisable(
                true
        );

        enableRoleButton.setDisable(
                !selected
        );
    }

    // =====================================================
    // إزالة اختيار الدور النشط
    // =====================================================

    private void clearActiveRoleSelection() {

        rolesList
                .getSelectionModel()
                .clearSelection();

        editRoleButton.setDisable(
                true
        );

        disableRoleButton.setDisable(
                true
        );
    }

    // =====================================================
    // إزالة اختيار الدور المعطل
    // =====================================================

    private void clearDisabledRoleSelection() {

        disabledRolesList
                .getSelectionModel()
                .clearSelection();

        enableRoleButton.setDisable(
                true
        );
    }

    // =====================================================
    // تعطيل عناصر إدارة الأدوار
    // =====================================================

    private void setRoleManagementDisabled(
            boolean disabled
    ) {

        addRoleButton.setDisable(
                disabled
        );

        reloadButton.setDisable(
                disabled
        );

        rolesList.setDisable(
                disabled
        );

        disabledRolesList.setDisable(
                disabled
        );

        if (disabled) {

            editRoleButton.setDisable(true);
            disableRoleButton.setDisable(true);
            enableRoleButton.setDisable(true);

        } else {

            RolePermissionAPI.RoleResponse active =
                    rolesList
                            .getSelectionModel()
                            .getSelectedItem();

            RolePermissionAPI.RoleResponse inactive =
                    disabledRolesList
                            .getSelectionModel()
                            .getSelectedItem();

            if (active != null) {

                editRoleButton.setDisable(false);
                disableRoleButton.setDisable(false);
                enableRoleButton.setDisable(true);

            } else if (inactive != null) {

                editRoleButton.setDisable(true);
                disableRoleButton.setDisable(true);
                enableRoleButton.setDisable(false);

            } else {

                editRoleButton.setDisable(true);
                disableRoleButton.setDisable(true);
                enableRoleButton.setDisable(true);
            }
        }
    }

    // =====================================================
    // تنظيف الصلاحيات
    // =====================================================

    private void clearPermissions() {

        clearPermissionControls();

        permissionsBox
                .getChildren()
                .setAll(
                        createMessageLabel(
                                "جاري تحميل الوظائف..."
                        )
                );
    }

    // =====================================================
    // تنظيف عناصر الصلاحيات
    // =====================================================

    private void clearPermissionControls() {

        permissionCheckBoxes.clear();

        permissionsBox
                .getChildren()
                .clear();

        savePermissionsButton.setDisable(
                true
        );

        selectAllButton.setDisable(
                true
        );

        clearAllButton.setDisable(
                true
        );

        loadingIndicator.setVisible(
                true
        );

        loadingIndicator.setManaged(
                true
        );
    }

    // =====================================================
    // إلغاء تحميل قديم
    // =====================================================

    private void invalidatePermissionLoad() {

        permissionLoadVersion.incrementAndGet();
    }

    // =====================================================
    // رسالة
    // =====================================================

    private Label createMessageLabel(
            String message
    ) {

        Label label =
                new Label(
                        message
                );

        label.setWrapText(
                true
        );

        label.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #78909c;
            -fx-padding: 15px;
        """);

        return label;
    }

    // =====================================================
    // لا توجد صلاحيات
    // =====================================================

    private Label createEmptyPermissionsMessage() {

        return createMessageLabel(
                "لا توجد صلاحيات نشطة معرفة في النظام."
        );
    }

    // =====================================================
    // الحالة
    // =====================================================

    private void setStatus(
            String message,
            boolean error
    ) {

        statusLabel.setText(
                message
        );

        statusLabel.setStyle(
                error
                        ? """
                            -fx-font-size: 13px;
                            -fx-font-weight: bold;
                            -fx-text-fill: #c62828;
                        """
                        : """
                            -fx-font-size: 13px;
                            -fx-font-weight: bold;
                            -fx-text-fill: #2e7d32;
                        """
        );
    }

    // =====================================================
    // الخطأ
    // =====================================================

    private void showError(
            String title,
            Exception e
    ) {

        setStatus(
                title,
                true
        );

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
                e != null &&
                        e.getMessage() != null &&
                        !e.getMessage().isBlank()
                        ? e.getMessage()
                        : "حدث خطأ غير معروف."
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        alert.showAndWait();
    }

    // =====================================================
    // تنبيه
    // =====================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        type
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

    // =====================================================
    // نتيجة نافذة الدور
    // =====================================================

    private record RoleDialogResult(
            String roleName,
            String description
    ) {
    }
}