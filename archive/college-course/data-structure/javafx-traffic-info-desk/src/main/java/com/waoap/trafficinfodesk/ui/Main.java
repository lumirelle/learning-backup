package com.waoap.trafficinfodesk.ui;

import com.waoap.trafficinfodesk.data.City;
import com.waoap.trafficinfodesk.data.Data;
import com.waoap.trafficinfodesk.data.Train;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * 主界面
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setResizable(false);
        primaryStage.setTitle("全国交通咨询台");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(Welcome.class.getResource("icon.png")).openStream()));
        primaryStage.show();

        // 读取本地数据
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(".data"));
            data = (Data) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 保存运行数据
        primaryStage.setOnCloseRequest((event) -> {
            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(".data"));
                outputStream.writeObject(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void stop() {
        // 保存数据
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(".data"));
            outputStream.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 数据集合，包含城市、列车时刻表等信息
     */
    protected static Data data = new Data();

    /**
     * 添加城市
     */
    @FXML
    protected void addCity() {
        Label cityNameLabel = new Label("城市名：");
        TextField cityNameField = new TextField();
        cityNameField.setPromptText("2 ~ 10 个字");
        cityNameField.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getControlNewText().length() > 10) {
                return null;
            } else {
                return change;
            }
        }));
        Label routeCityListLabel = new Label("可到达的城市列表：");
        TextField routeCityListField = new TextField();
        routeCityListField.setPrefWidth(500);
        routeCityListField.setPromptText("以 / 分格，格式如引号内：“福州/重庆/上海/北京”，不可含城市名");

        MDialog<City> mDialog = new MDialog<>();
        mDialog.setTitle("添加城市");
        mDialog.getRootPane().add(cityNameLabel, 1, 1);
        mDialog.getRootPane().add(cityNameField, 2, 1);
        mDialog.getRootPane().add(routeCityListLabel, 1, 2);
        mDialog.getRootPane().add(routeCityListField, 2, 2);
        mDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        mDialog.setResultConverter(dialogButton -> {
            // 点击确认按钮
            if (dialogButton == ButtonType.OK) {
                // 判断输入内容是否合规
                if (cityNameField.getText().length() >= 2 && routeCityListField.getText().matches("[\\u4E00-\\u9FA5]+(/[\\u4E00-\\u9FA5]+)*")) {
                    ArrayList<String> routeCitiesNames = new ArrayList<>(List.of(routeCityListField.getText().split("/")));
                    // 判断用户输入的可到达城市是否包括自身
                    if (!routeCitiesNames.contains(cityNameField.getText())) {
                        HashMap<String, ArrayList<Train>> routeCities2 = new HashMap<>();
                        for (String cityName : routeCitiesNames) {
                            routeCities2.put(cityName, new ArrayList<>());
                        }
                        return new City(cityNameField.getText(), routeCities2);
                    }
                }
                return new City(null, null);
            }
            return null;
        });

        Optional<City> result = mDialog.showAndWait();
        // 确认
        if (result.isPresent()) {
            // 不合规
            if (result.get().getName() == null && result.get().getTrainsOfRouteCities() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("创建失败！");
                alert.showAndWait();
            }
            // 合规
            else {
                // 已经存在，添加新可到达城市
                if (data.containCity(result.get().getName())) {
                    data.getCity(result.get().getName()).getTrainsOfRouteCities().putAll(result.get().getTrainsOfRouteCities());
                } else {
                    data.addCity(result.get());
                }
                // 反向添加
                result.get().getTrainsOfRouteCities().forEach((s, trains) -> {
                    if (data.containCity(s)) {
                        data.getCity(s).getTrainsOfRouteCities().put(result.get().getName(), new ArrayList<>());
                    } else {
                        HashMap<String, ArrayList<Train>> routeCities = new HashMap<>();
                        routeCities.put(result.get().getName(), new ArrayList<>());
                        data.addCity(new City(s, routeCities));
                    }
                });
            }
        }
    }

    /**
     * 删除城市
     */
    @FXML
    protected void removeCity() {
        Label cityNameLabel = new Label("城市名：");
        TextField cityNameField = new TextField();
        cityNameField.setPromptText("2 ~ 10 个字");
        cityNameField.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getControlNewText().length() > 10) {
                return null;
            } else {
                return change;
            }
        }));
        ListView<String> cityList = new ListView<>(FXCollections.observableArrayList(data.getCitiesNames()));

        MDialog<Boolean> mDialog = new MDialog<>();
        mDialog.setTitle("删除城市");
        mDialog.getRootPane().add(cityNameLabel, 1, 1);
        mDialog.getRootPane().add(cityNameField, 2, 1);
        mDialog.getRootPane().add(cityList, 1, 2, 2, 1);
        mDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        mDialog.setResultConverter(dialogButton -> {
            // 点击确认按钮
            if (dialogButton == ButtonType.OK) {
                // 判断输入内容是否合规
                return cityNameField.getText().length() >= 2;
            }
            return null;
        });

        Optional<Boolean> result = mDialog.showAndWait();
        // 合规
        if (result.isPresent()) {
            // 确认删除
            if (result.get()) {
                // 存在城市
                if (data.containCity(cityNameField.getText())) {
                    data.removeCity(cityNameField.getText());
                }
                // 不存在城市
                else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("不存在该城市！");
                    alert.showAndWait();
                }
            }
            // 不合规
            else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("输入内容不合规！");
                alert.showAndWait();
            }
        }
    }

    /**
     * 添加排班
     */
    @FXML
    protected void addFrequency() {
        Label trainNoLabel = new Label("列车编号");
        TextField trainNoField = new TextField();
        trainNoField.setPromptText("10 个字以下");
        trainNoField.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getControlNewText().length() > 10) {
                return null;
            } else {
                return change;
            }
        }));
        Label departureCityNameLabel = new Label("出发城市：");
        TextField departureCityNameField = new TextField();
        departureCityNameField.setPromptText("2 ~ 10 个字");
        departureCityNameField.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getControlNewText().length() > 10) {
                return null;
            } else {
                return change;
            }
        }));
        Label arrivalCityNameLabel = new Label("到达城市：");
        TextField arrivalCityNameField = new TextField();
        arrivalCityNameField.setPromptText("2 ~ 10 个字");
        arrivalCityNameField.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getControlNewText().length() > 10) {
                return null;
            } else {
                return change;
            }
        }));
        ListView<String> cityList = new ListView<>(FXCollections.observableArrayList(data.getCitiesNames()));
        Label departureTimeLabel = new Label("出发时间：");
        TextField departureTimeField = new TextField();
        departureTimeField.setPromptText("HH:MM");
        departureTimeField.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.getControlNewText().matches("\\d{0,2}(:\\d{0,2})*")) {
                return null;
            } else {
                return change;
            }
        }));
        Label arrivalTimeLabel = new Label("到达时间：");
        TextField arrivalTimeField = new TextField();
        arrivalTimeField.setPromptText("DD:HH:MM");
        arrivalTimeField.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.getControlNewText().matches("\\d{0,2}(:\\d{0,2})*")) {
                return null;
            } else {
                return change;
            }
        }));
        Label priceLabel = new Label("价格：");
        TextField priceField = new TextField();
        priceField.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.getControlNewText().matches("\\d*")) {
                return null;
            } else {
                return change;
            }
        }));
        DatePicker departureDatePicker = new DatePicker();
        departureDatePicker.setValue(LocalDate.now().minusDays(7));
        DatePicker arrivalDatePicker = new DatePicker();
        arrivalDatePicker.setValue(LocalDate.now().plusDays(7));

        MDialog<Train> mDialog = new MDialog<>();
        mDialog.setTitle("添加班次");
        mDialog.getRootPane().add(trainNoLabel, 1, 1);
        mDialog.getRootPane().add(trainNoField, 2, 1, 4, 1);
        mDialog.getRootPane().add(departureCityNameLabel, 1, 2);
        mDialog.getRootPane().add(departureCityNameField, 2, 2);
        mDialog.getRootPane().add(arrivalCityNameLabel, 4, 2);
        mDialog.getRootPane().add(arrivalCityNameField, 5, 2);
        mDialog.getRootPane().add(cityList, 1, 3, 5, 1);
        mDialog.getRootPane().add(departureTimeLabel, 1, 4);
        mDialog.getRootPane().add(departureTimeField, 2, 4);
        mDialog.getRootPane().add(arrivalTimeLabel, 4, 4);
        mDialog.getRootPane().add(arrivalTimeField, 5, 4);
        mDialog.getRootPane().add(priceLabel, 1, 5);
        mDialog.getRootPane().add(priceField, 2, 5);
        mDialog.getRootPane().add(departureDatePicker, 4, 5);
        mDialog.getRootPane().add(arrivalDatePicker, 5, 5);
        mDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        mDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                // 判断数据是否合理
                if (data.containCity(departureCityNameField.getText())
                        && data.containCity(arrivalCityNameField.getText())
                        && data.getCity(departureCityNameField.getText()).getTrainsOfRouteCities().containsKey(arrivalCityNameField.getText())
                        && departureTimeField.getText().matches("\\d{2}:\\d{2}")
                        && arrivalTimeField.getText().matches("\\d{2}:\\d{2}:\\d{2}")
                        && !departureDatePicker.getValue().isAfter(arrivalDatePicker.getValue())
                        && !departureDatePicker.getValue().isBefore(LocalDate.now().minusDays(7))
                        && !arrivalDatePicker.getValue().isAfter(LocalDate.now().plusDays(7))
                ) {
                    // 数据合理
                    // 计算排班日
                    boolean[] onWork = new boolean[15];
                    int i = LocalDate.now().getDayOfYear() - departureDatePicker.getValue().getDayOfYear();
                    if (i < 0) {
                        i += departureDatePicker.getValue().lengthOfYear();
                    }
                    for (; i >= 0; i--) {
                        // 从第八天往前
                        onWork[7 - i] = true;
                    }
                    i = arrivalDatePicker.getValue().getDayOfYear() - LocalDate.now().getDayOfYear();
                    if (i < 0) {
                        i += LocalDate.now().lengthOfYear();
                    }
                    for (; i >= 0; i--) {
                        // 从第八天往后
                        onWork[7 + i] = true;
                    }
                    return new Train(trainNoField.getText(), departureCityNameField.getText(), arrivalCityNameField.getText(), departureTimeField.getText(), arrivalTimeField.getText(), Double.parseDouble(priceField.getText()), onWork);
                } else {
                    // 数据不合理
                    return new Train(null, null, null, "0:0", "0:0:0", 0, null);
                }
            }
            return null;
        });

        Optional<Train> result = mDialog.showAndWait();
        if (result.isPresent()) {
            if (result.get().getName() != null) {
                data.getCity(departureCityNameField.getText()).getTrainsOfRouteCities().get(arrivalCityNameField.getText()).add(result.get());
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("创建失败！");
                alert.showAndWait();
            }
        }
    }

    /**
     * 删除排班
     */
    @FXML
    protected void removeFrequency() {
        Label noLabel = new Label("列车编号：");
        TextField noField = new TextField();
        TableView<Train> tableView = new TableView<>();
        TableColumn<Train, String> nameColumn = new TableColumn<>("编号");
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        TableColumn<Train, String> departureCityColumn = new TableColumn<>("出发城市");
        departureCityColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDepartureCityName()));
        TableColumn<Train, String> arrivalCityColumn = new TableColumn<>("到达城市");
        arrivalCityColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getArrivalCityName()));
        TableColumn<Train, String> departureTimeColumn = new TableColumn<>("出发时间");
        departureTimeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDepartureHour() + "时" + param.getValue().getDepartureMinute() + "分"));
        TableColumn<Train, String> arrivalTimeColumn = new TableColumn<>("到达时间");
        arrivalTimeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getArrivalDay() + "天" + param.getValue().getArrivalHour() + "时" + param.getValue().getArrivalMinute() + "分"));
        TableColumn<Train, String> priceColumn = new TableColumn<>("价格");
        priceColumn.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getPrice())));
        TableColumn<Train, String> workingTimeColumn = new TableColumn<>("近15天排班");
        workingTimeColumn.setCellValueFactory(param -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 15; i++) {
                if (param.getValue().isOnWork(i)) {
                    stringBuilder.append(LocalDate.now().getDayOfMonth() - 7 + i).append("日 ");
                }
            }
            return new SimpleStringProperty(stringBuilder.toString());
        });
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(departureCityColumn);
        tableView.getColumns().add(arrivalCityColumn);
        tableView.getColumns().add(departureTimeColumn);
        tableView.getColumns().add(arrivalTimeColumn);
        tableView.getColumns().add(priceColumn);
        tableView.getColumns().add(workingTimeColumn);
        data.getCities().forEach(city -> city.getTrainsOfRouteCities().forEach((s, trains) -> tableView.getItems().addAll(trains)));

        MDialog<Train> mDialog = new MDialog<>();
        mDialog.setTitle("删除班次");
        mDialog.getRootPane().add(noLabel, 0, 0);
        mDialog.getRootPane().add(noField, 1, 0);
        mDialog.getRootPane().add(tableView, 0, 1, 2, 1);
        mDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        mDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                for (Train train : tableView.getItems()) {
                    // 存在该列车
                    if (train.getName().equals(noField.getText())) {
                        return train;
                    }
                    // 不存在该列车
                    else {
                        return new Train(null, null, null, "0:0", "0:0:0", 0, null);
                    }
                }
            }
            return null;
        });

        Optional<Train> result = mDialog.showAndWait();
        if (result.isPresent()) {
            // 存在该列车
            if (result.get().getName() != null) {
                data.getCity(result.get().getDepartureCityName()).getTrainsOfRouteCities().get(result.get().getArrivalCityName()).remove(result.get());
            }
            // 不存在该列车
            else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("不存在该列车！");
                alert.showAndWait();
            }
        }
    }

    /**
     * 查找排班
     */
    @FXML
    protected void queryFrequency() {
        Label noLabel = new Label("列车编号：");
        TextField noField = new TextField();
        Label departureAndArrivalCityNameLabel = new Label("出发/到达城市：");
        TextField departureAndArrivalCityNameField = new TextField();
        departureAndArrivalCityNameField.setPromptText("以 / 分割");
        TableView<Train> tableView = new TableView<>();
        TableColumn<Train, String> nameColumn = new TableColumn<>("编号");
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        TableColumn<Train, String> departureCityColumn = new TableColumn<>("出发城市");
        departureCityColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDepartureCityName()));
        TableColumn<Train, String> arrivalCityColumn = new TableColumn<>("到达城市");
        arrivalCityColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getArrivalCityName()));
        TableColumn<Train, String> departureTimeColumn = new TableColumn<>("出发时间");
        departureTimeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDepartureHour() + "时" + param.getValue().getDepartureMinute() + "分"));
        TableColumn<Train, String> arrivalTimeColumn = new TableColumn<>("到达时间");
        arrivalTimeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getArrivalDay() + "天" + param.getValue().getArrivalHour() + "时" + param.getValue().getArrivalMinute() + "分"));
        TableColumn<Train, String> priceColumn = new TableColumn<>("价格");
        priceColumn.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getPrice())));
        TableColumn<Train, String> workingTimeColumn = new TableColumn<>("近15天排班");
        workingTimeColumn.setCellValueFactory(param -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 15; i++) {
                if (param.getValue().isOnWork(i)) {
                    stringBuilder.append(LocalDate.now().getDayOfMonth() - 7 + i).append("日 ");
                }
            }
            return new SimpleStringProperty(stringBuilder.toString());
        });
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(departureCityColumn);
        tableView.getColumns().add(arrivalCityColumn);
        tableView.getColumns().add(departureTimeColumn);
        tableView.getColumns().add(arrivalTimeColumn);
        tableView.getColumns().add(priceColumn);
        tableView.getColumns().add(workingTimeColumn);
        data.getCities().forEach(city -> city.getTrainsOfRouteCities().forEach((s, trains) -> tableView.getItems().addAll(FXCollections.observableArrayList(trains))));

        MDialog<ObservableList<Train>> mDialog = new MDialog<>();
        mDialog.setTitle("查询班次");
        mDialog.getRootPane().add(noLabel, 0, 0);
        mDialog.getRootPane().add(noField, 1, 0);
        mDialog.getRootPane().add(departureAndArrivalCityNameLabel, 0, 1);
        mDialog.getRootPane().add(departureAndArrivalCityNameField, 1, 1);
        mDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        mDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                ObservableList<Train> trains = FXCollections.observableArrayList();
                // 查找编号
                trains.add(new Train("Search By No", "", "", "0:0", "0:0:0", 0, null));
                for (Train train : tableView.getItems()) {
                    // 存在该列车
                    if (train.getName().equals(noField.getText())) {
                        trains.add(train);
                    }
                }
                // 查找出发/到达城市
                trains.add(new Train("Search By City", "", "", "0:0", "0:0:0", 0, null));
                // 输入合理
                if (departureAndArrivalCityNameField.getText().matches("[\\u4E00-\\u9FA5]+/[\\u4E00-\\u9FA5]+")) {
                    String[] cities = departureAndArrivalCityNameField.getText().split("/");
                    // 存在该出发/到达城市
                    if (data.containCity(cities[0]) && data.getCity(cities[0]).getTrainsOfRouteCities().containsKey(cities[1])) {
                        trains.addAll(data.getCity(cities[0]).getTrainsOfRouteCities().get(cities[1]));
                    }
                }
                return trains;
            }
            return null;
        });

        Optional<ObservableList<Train>> result = mDialog.showAndWait();
        result.ifPresent(trains -> {
            tableView.setItems(trains);
            noField.setEditable(false);
            departureAndArrivalCityNameField.setEditable(false);
            mDialog.getRootPane().add(tableView, 0, 2, 2, 1);
            mDialog.getDialogPane().getButtonTypes().remove(ButtonType.OK);
            mDialog.showAndWait();
        });
    }

    /**
     * 修改排班表
     */
    @FXML
    protected void modifyFrequency() {
        // TODO: 实现直接修改的功能
        removeFrequency();
        addFrequency();
    }

    /**
     * 咨询方案
     */
    @FXML
    protected void querySolution() {
        try {
            new QuerySolution().start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
