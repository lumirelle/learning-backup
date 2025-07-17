package com.waoap.addressbook;

import com.waoap.addressbook.model.AddressBook;
import com.waoap.addressbook.model.Person;
import com.waoap.addressbook.utils.Dialog;
import com.waoap.addressbook.utils.MTextFormatter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class MainController {
    /**
     * 关键词输入框
     */
    @FXML
    public TextField keywordFieldName;
    @FXML
    public TextField keywordFieldTelephone;
    @FXML
    public TextField keywordFieldEmail;

    /**
     * 搜索、新增按钮
     */
    @FXML
    public Button searchButton;
    @FXML
    public Button addButton;

    /**
     * 联系人姓名列表
     */
    @FXML
    public ListView<String> contactsList;

    /**
     * 日志记录框
     */
    @FXML
    public Text logField;

    /**
     * 侧边导航栏
     */
    @FXML
    public VBox navigationList;

    /**
     * 刷新联系人姓名列表，也即刷新显示
     */
    private void refreshContacts() {
        // 高频调用方法，如刷新 UI，须使用 Platform.runLater
        // 否则后续会产生难以定位的问题
        Platform.runLater(() -> {
            contactsList.getItems().clear();
            Queue<String> tmp = new LinkedList<>();
            while (!addressBook.getNames().isEmpty()) {
                contactsList.getItems().add(addressBook.getNames().peek());
                tmp.offer(addressBook.getNames().poll());
            }
            while (!tmp.isEmpty()) {
                addressBook.getNames().offer(tmp.poll());
            }
        });
    }

    /**
     * 打印用户操作日志
     *
     * @param logLevel 日志级别
     * @param message  日志信息
     */
    public void log(LogLevel logLevel, String message) {
        switch (logLevel) {
            case INFO -> logField.setStyle("-fx-fill: black;");
            case WARN -> logField.setStyle("-fx-fill: orange;");
            case ERROR -> logField.setStyle("-fx-fill: red;");
        }
        logField.setText(message + " [" + logLevel + "]");
    }

    /**
     * 指定联系人列表，刷新联系人姓名列表，也即刷新显示
     *
     * @param contacts 指定的联系人列表
     */
    private void refreshContacts(ArrayList<String> contacts) {
        // 高频调用方法，如刷新 UI，须使用 Platform.runLater
        // 否则后续会产生难以定位的问题
        Platform.runLater(() -> {
            contactsList.getItems().clear();
            contactsList.getItems().addAll(contacts);
        });
    }

    public void initialize() {
        // 初始化电话簿
        addressBook.add(new Person("#", Person.Status.NAVIGATION_TYPE));
        for (int i = 0; i < 26; i++) {
            addressBook.add(new Person(String.valueOf((char) ('A' + i)), Person.Status.NAVIGATION_TYPE));
        }
        refreshContacts();

        // 限制关键词输入框的文本格式
        keywordFieldName.setTextFormatter(MTextFormatter.getNameFormatter());
        keywordFieldTelephone.setTextFormatter(MTextFormatter.getTelephoneFormatter());
        keywordFieldEmail.setTextFormatter(MTextFormatter.getEmailFormatter());

        // 新增联系人按钮事件
        addButton.setOnAction(event -> {
            log(LogLevel.INFO, "正在新增联系人……");

            Dialog dialog = Dialog.getInstance();
            dialog.setTitle("新增联系人");

            Optional<Person> result = showAndWaitAddDialog(dialog);
            if (result.isPresent()) {
                if (result.get().getStatus() != Person.Status.ERROR) {
                    addressBook.add(result.get());
                    refreshContacts();
                    // 如果正在查找，则重置查找状态
                    if (searchButton.getText().equals("返回")) {
                        refreshContacts();
                        searchButton.setText("查找");
                        log(LogLevel.INFO, "结束查找联系人。");
                    }
                    log(LogLevel.INFO, "联系人添加成功！");
                } else {
                    log(LogLevel.WARN, "联系人添加失败！");
                }
            } else {
                log(LogLevel.INFO, "取消添加联系人。");
            }
        });

        // 查找联系人按钮事件
        searchButton.setOnAction(event -> {
            if (searchButton.getText().equals("查找")) {
                log(LogLevel.INFO, "正在查找联系人……");
                ArrayList<String> matchContacts = new ArrayList<>();
                matchContacts.add("Result of Name");
                if (!keywordFieldName.getText().isBlank()) {
                    matchContacts.addAll(addressBook.findByName(keywordFieldName.getText()));
                }
                matchContacts.add("Result of Telephone");
                if (!keywordFieldTelephone.getText().isBlank()) {
                    matchContacts.addAll(addressBook.findByTelephone(keywordFieldTelephone.getText()));
                }
                matchContacts.add("Result of Email");
                if (!keywordFieldEmail.getText().isBlank()) {
                    matchContacts.addAll(addressBook.findByEmail(keywordFieldEmail.getText()));
                }
                refreshContacts(matchContacts);
                searchButton.setText("返回");
            } else if (searchButton.getText().equals("返回")) {
                refreshContacts();
                searchButton.setText("查找");
                log(LogLevel.INFO, "结束查找联系人。");
            }
        });

        // 联系人点击事件
        contactsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // null 安全判断，以及要求点击的不是 #A-Z 这几个导航条目
            if (newValue != null && !newValue.matches("[#A-Z]|(Result of .+)")) {
                log(LogLevel.INFO, "正在查看联系人……");

                // 有重名情况时，确认用户点击的是重名中的第几个
                int index = contactsList.getSelectionModel().getSelectedIndex();
                int count = 1;
                for (int i = 0; i < index; i++) {
                    if (contactsList.getItems().get(i).matches("Sort By .+"))
                        break;
                    if (contactsList.getItems().get(i).equals(newValue)) {
                        count++;
                    }
                }

                // 不及时清除选中状态会导致连续两次选中同一个联系人条目，
                // 第二次选中时，选中监听失效
                contactsList.getSelectionModel().clearSelection();
                contactsList.getSelectionModel().selectFirst();

                Dialog dialog = Dialog.getInstance();
                dialog.setTitle("联系人信息");
                Person contact = addressBook.getNames2contacts().get(newValue).get(count - 1);
                dialog.preloadFrom(contact);
                dialog.setDisableAll(true);

                // 删除、修改、取消按钮
                ButtonType buttonTypeDelete = new ButtonType("删除", ButtonBar.ButtonData.OK_DONE);
                ButtonType buttonTypeModify = new ButtonType("修改", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(buttonTypeDelete, buttonTypeModify, ButtonType.CANCEL);
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == buttonTypeDelete) {
                        contact.setStatus(Person.Status.IN_DELETE);
                        return contact;
                    } else if (dialogButton == buttonTypeModify) {
                        contact.setStatus(Person.Status.IN_MODIFICATION);
                        return contact;
                    } else {
                        return null;
                    }
                });

                // 对话框（第一次）执行结果
                Optional<Person> result = dialog.showAndWait();
                if (result.isPresent()) {
                    Person oldContact = result.get();

                    // 用户选择删除联系人
                    if (oldContact.getStatus() == Person.Status.IN_DELETE) {
                        addressBook.delete(oldContact);
                        refreshContacts();
                        // 如果正在查找，则重置查找状态
                        if (searchButton.getText().equals("返回")) {
                            refreshContacts();
                            searchButton.setText("查找");
                            log(LogLevel.INFO, "结束查找联系人。");
                        }
                        log(LogLevel.INFO, "联系人删除成功！");
                    }
                    // 用户选择编辑联系人
                    else if (oldContact.getStatus() == Person.Status.IN_MODIFICATION) {
                        dialog.setDisableAll(false);
                        dialog.getDialogPane().getButtonTypes().clear();

                        // 对话框（第二次）执行结果
                        Optional<Person> result1 = showAndWaitAddDialog(dialog);
                        if (result1.isPresent()) {
                            Person newContact = result1.get();

                            // 修改的联系人信息合规
                            if (newContact.getStatus() != Person.Status.ERROR) {
                                // 无变化
                                if (oldContact.equalTo(newContact)) {
                                    log(LogLevel.INFO, "取消修改联系人。");
                                }
                                // 成功修改
                                else {
                                    addressBook.modify(oldContact, newContact);
                                    refreshContacts();
                                    // 如果正在查找，则重置查找状态
                                    if (searchButton.getText().equals("返回")) {
                                        refreshContacts();
                                        searchButton.setText("查找");
                                        log(LogLevel.INFO, "结束查找联系人。");
                                    }
                                    log(LogLevel.INFO, "联系人修改成功！");
                                }
                            }
                            // 修改的联系人信息不合规
                            else {
                                log(LogLevel.WARN, "联系人修改失败！");
                            }
                        } else {
                            log(LogLevel.INFO, "取消修改联系人。");
                        }
                    }
                } else {
                    log(LogLevel.INFO, "结束查看联系人。");
                }
            }
        });

        // 侧边导航栏点击事件，跳转到对应类别的联系人处
        for (int i = 0; i < navigationList.getChildren().size(); i++) {
            int finalI = i;
            navigationList.getChildren().get(i).setOnMouseClicked(event -> contactsList.scrollTo(contactsList.getItems().indexOf(((Label) navigationList.getChildren().get(finalI)).getText())));
        }

        // 关于
        logField.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                try {
                    ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResource("icon.png")).openStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                alert.setTitle("关于");
                alert.setHeaderText("「长歌吟松风，曲尽河星稀」");
                alert.setContentText("版本：\t 1.0.3 -- TEST\n作者：\t Waoap\nGitHub： https://github.com/Waoap");
                alert.showAndWait();
            }
        });

        log(LogLevel.INFO, "欢迎！(●'◡'●) 双击此打开关于界面。");
    }

    /**
     * 电话簿实例
     */
    private final AddressBook addressBook = new AddressBook();

    /**
     * 为对话框添加确认、取消按钮，并调用 {@code showAndWait()} 方法，将其结果作为返回值
     *
     * @param dialog 对话框
     * @return 对话框执行的结果，是 {@link Person} 类的对象
     * @apiNote 当对话框中数据有误时会生成状态为 {@code Person.Status.ERROR} 的联系人，而点击取消按钮以及关闭窗口都会直接返回 {@code null}
     */
    private Optional<Person> showAndWaitAddDialog(Dialog dialog) {
        ButtonType buttonTypeOk = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, ButtonType.CANCEL);

        // 设置结果格式
        dialog.setResultConverter(dialogButton -> {
            // 确认按钮事件
            if (dialogButton == buttonTypeOk) {
                String name;
                List<String> telephones = new ArrayList<>();
                String email;
                String address;
                String note;
                name = dialog.getNameField().getText();
                for (TextField telephoneField : dialog.getTelephoneFields()) {
                    telephones.add(telephoneField.getText());
                }
                email = dialog.getEmailField().getText();
                address = dialog.getAddressField().getText();
                note = dialog.getNoteField().getText();

                if (name.length() < 2) {
                    new Alert(Alert.AlertType.WARNING, "姓名长度至少为两位！").showAndWait();
                    return new Person("error", Person.Status.ERROR);
                }

                for (String telephone : telephones) {
                    if (telephone.length() != 11) {
                        new Alert(Alert.AlertType.WARNING, "电话长度必须为11位！").showAndWait();
                        return new Person("error", Person.Status.ERROR);
                    }
                }

                if (!email.matches("^\\w+(\\w|[.]\\w+)+@\\w+([.]\\w+){1,3}")) {
                    new Alert(Alert.AlertType.WARNING, "邮箱不合规！").showAndWait();
                    return new Person("error", Person.Status.ERROR);
                }

                return new Person(name, telephones, email, address, note);
            }
            // 取消按钮以及关闭窗口
            else {
                return null;
            }
        });

        return dialog.showAndWait();
    }

    /**
     * 日志级别
     */
    public enum LogLevel {
        INFO, WARN, ERROR
    }
}
