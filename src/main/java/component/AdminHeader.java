package component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class AdminHeader extends HBox {

    private final Label titleLabel;
    private final Label userLabel;

    public AdminHeader(String title) {

        setAlignment(Pos.CENTER_LEFT);
        setSpacing(20);
        setPadding(new Insets(15, 20, 15, 20));

        setStyle("""
            -fx-background-color: white;
            -fx-border-color: #e0e0e0;
            -fx-border-width: 0 0 1 0;
        """);

        titleLabel = new Label(title);

        titleLabel.setStyle("""
            -fx-font-size: 22px;
            -fx-font-weight: bold;
            -fx-text-fill: #263238;
        """);

        userLabel = new Label("FAS Admin");

        userLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #607D8B;
        """);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
                titleLabel,
                spacer,
                userLabel
        );
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setUsername(String username) {
        userLabel.setText(username);
    }
}