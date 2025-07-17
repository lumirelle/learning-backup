package com.waoap.trafficinfodesk.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class InputRowPane extends GridPane {
    public InputRowPane(Label label) {
        setMaxWidth(550);
        setMinWidth(550);
        // 占位
        Label emptyLabel = new Label();
        emptyLabel.setMinWidth(150);
        emptyLabel.setMaxWidth(150);
        label.setMaxWidth(400);
        label.setMinWidth(400);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setTextFill(Color.GREEN);
        add(emptyLabel, 0, 0, 1, 1);
        add(label, 2, 0, 4, 1);
    }
}
