package com.waoap.addressbook.utils;

import com.waoap.addressbook.MainApplication;
import com.waoap.addressbook.model.Person;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Dialog extends javafx.scene.control.Dialog<Person> {
    private final TextField nameField;

    private final ArrayList<TextField> telephoneFields;

    private final Button moreTelephoneButton;

    private final TextField emailField;

    private final TextField addressField;

    private final TextField noteField;

    /**
     * 一个以联系人为数据的对话框
     *
     * @param nameField           姓名编辑框
     * @param telephoneFields     电话编辑框（可多个）
     * @param moreTelephoneButton 添加更多电话编辑框按钮
     * @param emailField          邮箱编辑框
     * @param addressField        住址编辑框
     * @param noteField           备注框
     */
    private Dialog(TextField nameField, ArrayList<TextField> telephoneFields, Button moreTelephoneButton, TextField emailField, TextField addressField, TextField noteField) {
        this.nameField = nameField;
        this.telephoneFields = telephoneFields;
        this.moreTelephoneButton = moreTelephoneButton;
        this.emailField = emailField;
        this.addressField = addressField;
        this.noteField = noteField;
    }

    public static Dialog getInstance() {
        // 创建用于接受/显示联系人基本信息的输入框，以及对应的标签
        Label nameLabel = new Label("姓名：");
        TextField nameField = new TextField();
        nameField.setPrefWidth(150);
        nameField.setTextFormatter(MTextFormatter.getNameFormatter());

        Label telephoneLabel = new Label("电话：");
        ArrayList<TextField> telephoneFields = new ArrayList<>();
        telephoneFields.add(new TextField());
        telephoneFields.get(0).setTextFormatter(MTextFormatter.getTelephoneFormatter());
        // 实现添加更多电话号码的按钮
        Button moreTelephoneButton = new Button("+");

        Label emailLabel = new Label("邮箱：");
        TextField emailField = new TextField();
        emailField.setTextFormatter(MTextFormatter.getEmailFormatter());

        Label addressLabel = new Label("住址：");
        TextField addressField = new TextField();
        addressField.setTextFormatter(MTextFormatter.getCommonFormatter());

        Label noteLabel = new Label("备注：");
        TextField noteField = new TextField();
        noteField.setTextFormatter(MTextFormatter.getCommonFormatter());

        // 把上述组件添加入网格布局
        GridPane dialogRoot = new GridPane();
        dialogRoot.setVgap(10);
        dialogRoot.setHgap(20);
        dialogRoot.add(nameLabel, 1, 1);
        dialogRoot.add(nameField, 2, 1);
        dialogRoot.add(telephoneLabel, 1, 2);
        dialogRoot.add(telephoneFields.get(0), 2, 2);
        dialogRoot.add(moreTelephoneButton, 3, 2);
        dialogRoot.add(emailLabel, 1, 3);
        dialogRoot.add(emailField, 2, 3);
        dialogRoot.add(addressLabel, 1, 4);
        dialogRoot.add(addressField, 2, 4);
        dialogRoot.add(noteLabel, 1, 5);
        dialogRoot.add(noteField, 2, 5);

        // 创建对话框
        Dialog dialog = new Dialog(nameField, telephoneFields, moreTelephoneButton, emailField, addressField, noteField);
        try {
            ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResource("icon.png")).openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        dialog.getDialogPane().setContent(dialogRoot);

        // 添加更多电话号码事件
        moreTelephoneButton.setOnAction(event1 -> {
            Label newLabel = new Label("电话：");
            TextField newField = new TextField();
            newField.setTextFormatter(MTextFormatter.getTelephoneFormatter());

            dialogRoot.add(newLabel, 1, 2 + telephoneFields.size());
            dialogRoot.add(newField, 2, 2 + telephoneFields.size());
            // 把其余的组件下移一格，给新加的电话输入框腾位置
            // 新输入框添在 dialogRoot 孩子的末尾，因此应该移动的是
            // 除去前 4 个组件（姓名 2 个，原有的电话 2 个）
            // 以及最后 telephoneFields.size() 个组件，也即除去
            // 后来添加的组件后，剩下的所有组件
            for (int i = 4; i < dialogRoot.getChildren().size() - 2 * telephoneFields.size(); i++) {
                Node node = dialogRoot.getChildren().get(i);
                GridPane.setRowIndex(node, GridPane.getRowIndex(node) + 1);
            }
            // 增加新组件的同时增加弹窗的高度
            dialog.setHeight(dialogRoot.getHeight() + 130);

            telephoneFields.add(newField);
        });

        return dialog;
    }

    public TextField getNameField() {
        return nameField;
    }

    public ArrayList<TextField> getTelephoneFields() {
        return telephoneFields;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public TextField getAddressField() {
        return addressField;
    }

    public TextField getNoteField() {
        return noteField;
    }

    /**
     * 使对话框里的值不可修改（禁用文本框和按钮）
     *
     * @param value 是否禁用
     */
    public void setDisableAll(boolean value) {
        ((GridPane) this.getDialogPane().getContent()).getChildren().forEach(node -> {
            if (node instanceof TextInputControl) {
                ((TextField) node).setEditable(!value);
            }
            if (node instanceof Button) {
                node.setDisable(value);
            }
        });
    }

    /**
     * 预加载某个联系人的信息
     *
     * @param contact 预加载的联系人
     */
    public void preloadFrom(Person contact) {
        nameField.setText(contact.getName());
        telephoneFields.get(0).setText(contact.getTelephones().get(0));
        for (int i = 1; i < contact.getTelephones().size(); i++) {
            moreTelephoneButton.fire();
            telephoneFields.get(i).setText(contact.getTelephones().get(i));
        }
        addressField.setText(contact.getAddress());
        emailField.setText(contact.getEmail());
        noteField.setText(contact.getNote());
    }
}
