package component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StatCard extends VBox {

    public StatCard(
            String title,
            String value
    ) {

        setAlignment(Pos.CENTER);
        setSpacing(8);

        setPrefSize(220, 130);

        setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-border-color: #e0e0e0;
            -fx-padding: 20;
        """);

        Label titleLabel = new Label(title);

        titleLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #666666;
        """);

        Label valueLabel = new Label(value);

        valueLabel.setStyle("""
            -fx-font-size: 28px;
            -fx-font-weight: bold;
            -fx-text-fill: #2E7D32;
        """);

        getChildren().addAll(
                titleLabel,
                valueLabel
        );
    }
}