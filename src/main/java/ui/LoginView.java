package ui;

import api.FasAdminAPI;
import app.Main;
import dto.AdminLoginResponse;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import api.AdminApiManager;
public class LoginView extends VBox {

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button loginButton = new Button("تسجيل الدخول");

    private final FasAdminAPI api =
            AdminApiManager
                    .getInstance()
                    .getFasAdminAPI();
    public LoginView() {

        setAlignment(Pos.CENTER);
        setSpacing(18);

        setStyle("""
            -fx-background-color: #f4f6f8;
        """);

        Label title = new Label("FAS Admin");

        title.setStyle("""
            -fx-font-size: 32px;
            -fx-font-weight: bold;
            -fx-text-fill: #2E7D32;
        """);

        Label subtitle =
                new Label("لوحة إدارة نظام FAS");

        subtitle.setStyle("""
            -fx-font-size: 15px;
            -fx-text-fill: #666666;
        """);

        usernameField.setPromptText("اسم المستخدم");
        usernameField.setPrefWidth(320);
        usernameField.setPrefHeight(42);

        passwordField.setPromptText("كلمة المرور");
        passwordField.setPrefWidth(320);
        passwordField.setPrefHeight(42);

        loginButton.setPrefWidth(320);
        loginButton.setPrefHeight(45);

        loginButton.setStyle("""
            -fx-background-color: #2E7D32;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 15px;
            -fx-background-radius: 7;
        """);

        loginButton.setOnAction(e -> login());

        passwordField.setOnAction(e -> login());

        getChildren().addAll(
                title,
                subtitle,
                usernameField,
                passwordField,
                loginButton
        );
    }

    private void login() {

        String username =
                usernameField.getText().trim();

        String password =
                passwordField.getText();

        if (username.isEmpty() ||
                password.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "بيانات ناقصة",
                    "يرجى إدخال اسم المستخدم وكلمة المرور."
            );

            return;
        }

        setLoginState(true);

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        return api.login(
                                username,
                                password
                        );

                    } catch (IOException |
                             InterruptedException ex) {

                        if (ex instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }

                        throw new RuntimeException(ex);
                    }
                })
                .thenAccept(response ->
                        Platform.runLater(() ->
                                handleLoginResponse(response)
                        )
                )
                .exceptionally(ex -> {

                    Platform.runLater(() -> {

                        setLoginState(false);

                        Throwable cause =
                                ex.getCause() != null
                                        ? ex.getCause()
                                        : ex;

                        showAlert(
                                Alert.AlertType.ERROR,
                                "خطأ في الاتصال",
                                "تعذر الاتصال بسيرفر FAS.\n\n"
                                        + cause.getMessage()
                        );
                    });

                    return null;
                });
    }

    private void handleLoginResponse(
            AdminLoginResponse response
    ) {

        setLoginState(false);

        if (response == null) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "خطأ",
                    "السيرفر أعاد استجابة غير صالحة."
            );

            return;
        }

        if (!response.isSuccess()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "فشل تسجيل الدخول",
                    response.getMessage()
            );

            passwordField.clear();

            return;
        }

        /*
         * api.login() قام بالفعل بإنشاء
         * AdminSession عند النجاح.
         */

        Main.getMainStage().setScene(
                new Scene(
                        new MainAdminView(),
                        1200,
                        750
                )
        );
    }

    private void setLoginState(
            boolean loading
    ) {

        loginButton.setDisable(loading);

        usernameField.setDisable(loading);
        passwordField.setDisable(loading);

        if (loading) {

            loginButton.setText(
                    "جارٍ تسجيل الدخول..."
            );

        } else {

            loginButton.setText(
                    "تسجيل الدخول"
            );
        }
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(
                message == null
                        ? "حدث خطأ غير معروف."
                        : message
        );

        alert.showAndWait();
    }
}