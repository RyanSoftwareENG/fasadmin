package app;

import ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage mainStage;

    @Override
    public void start(Stage stage) {

        mainStage = stage;

        LoginView loginView = new LoginView();

        Scene scene = new Scene(
                loginView,
                1000,
                700
        );

        mainStage.setTitle("FAS Admin");
        mainStage.setScene(scene);
        mainStage.setMinWidth(900);
        mainStage.setMinHeight(600);
        mainStage.show();
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}