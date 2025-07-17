package com.waoap.trafficinfodesk.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class OutputRowPane extends GridPane {
    public OutputRowPane(Label outputTextLabel) {
        setMaxWidth(550);
        setMinWidth(550);
        outputTextLabel.setMaxWidth(400);
        outputTextLabel.setMinWidth(400);
        outputTextLabel.setTextFill(Color.RED);
        add(outputTextLabel, 0, 0, 4, 1);
    }
}
