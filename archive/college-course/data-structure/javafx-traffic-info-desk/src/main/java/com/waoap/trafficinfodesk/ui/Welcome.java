package com.waoap.trafficinfodesk.ui;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * 欢迎界面
 */
public class Welcome extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Welcome.class.getResource("welcome-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setResizable(false);
        primaryStage.setTitle("欢迎鸭！");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(Welcome.class.getResource("icon.png")).openStream()));
        primaryStage.show();
    }


    /**
     * 主标题，用于启动程序过程中关闭欢迎界面
     */
    @FXML
    protected Label mainTitle;

    /**
     * 启动程序
     */
    @FXML
    protected void launchApp() {
        try {
            new Main().start(new Stage());
            ((Stage) mainTitle.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 展示关于信息
     */
    @FXML
    protected void aboutApp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        try {
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Objects.requireNonNull(Welcome.class.getResource("icon.png")).openStream()));
        } catch (IOException ignored) {
        }
        alert.setTitle("关于");
        alert.setHeaderText("「试上高峰窥皓月，偶开天眼觑红尘」");
        alert.setContentText("版本：\t 1.0.0 -- Alpha\n作者：\t Waoap\nGitHub： https://github.com/Waoap");
        alert.showAndWait();
    }
}
