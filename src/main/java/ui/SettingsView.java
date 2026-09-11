package ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SettingsView extends VBox {

    public SettingsView() {

        setSpacing(20);
        setPadding(new Insets(25));

        Label title =
                new Label("إعدادات FAS Admin");

        title.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
        """);

        VBox security =
                createSection(
                        "الأمان",
                        new CheckBox(
                                "تسجيل نشاط الإدارة"
                        ),
                        new CheckBox(
                                "تسجيل محاولات الدخول الفاشلة"
                        )
                );

        VBox updates =
                createSection(
                        "التحديثات",
                        new CheckBox(
                                "التحقق من التحديثات تلقائيًا"
                        ),
                        new CheckBox(
                                "السماح بالتحديثات الإجبارية"
                        )
                );

        VBox general =
                createSection(
                        "عام",
                        new CheckBox(
                                "تشغيل البرنامج مع بدء النظام"
                        )
                );

        Button save =
                new Button("💾 حفظ الإعدادات");

        getChildren().addAll(
                title,
                security,
                updates,
                general,
                save
        );
    }

    private VBox createSection(
            String title,
            CheckBox... options
    ) {

        VBox box =
                new VBox(10);

        Label label =
                new Label(title);

        label.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: bold;
        """);

        box.setPadding(
                new Insets(18)
        );

        box.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-color: #e0e0e0;
            -fx-border-radius: 10;
        """);

        box.getChildren().add(label);
        box.getChildren().addAll(options);

        return box;
    }
}