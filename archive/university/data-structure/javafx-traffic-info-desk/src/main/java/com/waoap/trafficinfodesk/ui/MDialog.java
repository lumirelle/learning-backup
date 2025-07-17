package com.waoap.trafficinfodesk.ui;

import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MDialog<E> extends Dialog<E> {
    private final GridPane rootPane;

    public MDialog() {
        try {
            ((Stage) this.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Objects.requireNonNull(Welcome.class.getResource("icon.png")).openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        rootPane = new GridPane();
        rootPane.setHgap(15);
        rootPane.setVgap(20);

        this.getDialogPane().setContent(rootPane);
    }

    public GridPane getRootPane() {
        return rootPane;
    }
}
