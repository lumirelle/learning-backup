package com.waoap.trafficinfodesk.ui;

import com.waoap.trafficinfodesk.data.City;
import com.waoap.trafficinfodesk.data.Train;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

public class QuerySolution extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("query-solution-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setResizable(false);
        primaryStage.setTitle("智能查询");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(Welcome.class.getResource("icon.png")).openStream()));
        primaryStage.show();
    }

    @FXML
    protected ListView<GridPane> contactView;

    @FXML
    protected TextField contactField;

    @FXML
    public void initialize() {
        contactView.getItems().add(new OutputRowPane(new Label("功能介绍：")));
        contactView.getItems().add(new OutputRowPane(new Label("1.查询 <城市> <可达|有车可达>")));
        contactView.getItems().add(new OutputRowPane(new Label("\t查询某城市可到达|有车可到达的城市列表。")));
        contactView.getItems().add(new OutputRowPane(new Label("2.<出发城市> <目标城市> <时间|花费|中转>")));
        contactView.getItems().add(new OutputRowPane(new Label("\t查询从出发城市到目标城市的最优决策。")));
        contactView.getItems().add(new OutputRowPane(new Label("参数以空格分隔。")));

        contactField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // 获取输入
                String userInput = contactField.getText();
                contactField.setText("");
                // 显示用户的输入
                contactView.getItems().add(new InputRowPane(new Label(userInput)));
                // 显示分析的结果
                contactView.getItems().add(new OutputRowPane(new Label(analyse(userInput))));
                // 滚动到最后一行
                contactView.scrollTo(contactView.getItems().size() - 1);
            }
        });
    }

    private static String analyse(String userInput) {
        String[] userInputParts = userInput.split(" +");
        if (userInputParts.length != 3) {
            return "你给的参数个数好像不太对呀？";
        }

        // 查询功能
        if (userInputParts[0].equals("查询")) {
            if (!Main.data.containCity(userInputParts[1])) {
                return "好像没有" + userInputParts[1] + "这个城市哦？";
            }

            StringBuilder stringBuilder = new StringBuilder();
            switch (userInputParts[2]) {
                case "可达" -> Main.data.getCity(userInputParts[1]).getTrainsOfRouteCities().forEach((s, trains) -> {
                    stringBuilder.append(s).append(" ");
                });
                case "有车可达" ->
                        Main.data.getCity(userInputParts[1]).getTrainsOfRouteCities().forEach((s, trains) -> {
                            if (trains.size() != 0) {
                                stringBuilder.append(s).append(" ");
                            }
                        });
                default -> stringBuilder.append("认真看功能介绍！（故作懊恼）");
            }

            if (stringBuilder.isEmpty()) {
                return "空空如也~";
            }
            return stringBuilder.toString();
        }

        // 最优决策功能
        if (!Main.data.containCity(userInputParts[0])) {
            return "好像没有" + userInputParts[0] + "这个城市哦？";
        }
        if (!Main.data.getCity(userInputParts[0]).getTrainsOfRouteCities().containsKey(userInputParts[1])) {
            return "好像" + userInputParts[0] + "没有到" + userInputParts[1] + "的路欸！";
        }
        ArrayList<ArrayList<Train>> paths;
        switch (userInputParts[2]) {
            case "时间" -> {
                paths = findPaths(userInputParts[0], userInputParts[1], SolutionType.SHORTEST_TIME);
                paths.sort((o1, o2) -> {
                    int[] costMinutes = {0, 0};
                    int[] transitWait = {0, 0};
                    o1.forEach(train -> {
                        costMinutes[0] += train.getCostTime();
                        if (transitWait[0] != 0) {
                            costMinutes[0] += train.getDepartureHour() * 60 + train.getDepartureMinute() - transitWait[0];
                        }
                        transitWait[0] = train.getArrivalHour() * 60 + train.getArrivalMinute();
                    });
                    o2.forEach(train -> {
                        costMinutes[1] += train.getCostTime();
                        if (transitWait[1] != 0) {
                            costMinutes[1] += train.getDepartureHour() * 60 + train.getDepartureMinute() - transitWait[0];

                        }
                        transitWait[1] = train.getArrivalHour() * 60 + train.getArrivalMinute();
                    });
                    return Integer.compare(costMinutes[0], costMinutes[1]);
                });
            }
            case "花费" -> {
                paths = findPaths(userInputParts[0], userInputParts[1], SolutionType.LOWEST_COST);
                paths.sort((o1, o2) -> {
                    double[] prices = {0, 0};
                    o1.forEach(train -> prices[0] += train.getPrice());
                    o2.forEach(train -> prices[1] += train.getPrice());
                    return Double.compare(prices[0], prices[1]);
                });
            }
            case "中转" -> {
                paths = findPaths(userInputParts[0], userInputParts[1], SolutionType.LOWEST_COST);
                paths.sort(Comparator.comparingInt(ArrayList::size));
            }
            default -> {
                return "给了仨决策选项害不满足呢！要啥自行车(￣y▽,￣)╭ ";
            }
        }
        if (paths.isEmpty()) {
            return "空空如也~";
        } else {
            StringBuilder stringBuilder = new StringBuilder("路线为：");
            paths.get(0).forEach(train -> stringBuilder.append(train.getName()).append(" "));
            double[] price = {0};
            int[] costTime = {0};
            paths.get(0).forEach(train -> {
                price[0] += train.getPrice();
                costTime[0] += train.getCostTime();
            });
            stringBuilder.append("，价格为：").append(price[0])
                    .append("，花费时间：")
                    .append(costTime[0] / 1000 / 60 / 24 > 0 ? costTime[0] / 1000 / 60 / 24 + "天" : "")
                    .append(costTime[0] / 1000 / 60 % 24).append("时")
                    .append(costTime[0] / 1000 % 60).append("分");
            return stringBuilder.toString();
        }
    }

    /**
     * 采用深度优先遍历来查找路径
     *
     * @param departureCityName 出发城市名
     * @param arrivalCityName   到达城市名
     * @param solutionType      决策方案类型
     * @return 所有路径
     */
    private static ArrayList<ArrayList<Train>> findPaths(String departureCityName, String arrivalCityName, SolutionType solutionType) {
        HashMap<String, Boolean> visited = new HashMap<>();
        visited.put(departureCityName, true);
        ArrayList<ArrayList<Train>> paths = new ArrayList<>();
        ArrayList<Train> path = new ArrayList<>();
        City departureCity = Main.data.getCity(departureCityName);
        dfs(departureCity, arrivalCityName, visited, paths, path, solutionType);
        return paths;
    }

    /**
     * 深度优先遍历城市
     *
     * @param city            当前遍历城市
     * @param arrivalCityName 目标城市名
     * @param visited         访问状态哈希表
     * @param paths           全部路径
     * @param path            当前走过路径
     * @param solutionType    决策类型
     */
    @SuppressWarnings("unchecked")
    private static void dfs(City city, String arrivalCityName, HashMap<String, Boolean> visited, ArrayList<ArrayList<Train>> paths, ArrayList<Train> path, SolutionType solutionType) {
        city.getTrainsOfRouteCities().forEach((s, trains) -> {
            // 有班次且没有访问过
            if (trains.size() != 0 && (!visited.containsKey(s) || !visited.get(s))) {
                switch (solutionType) {
                    case LOWEST_COST -> {
                        trains.sort(Comparator.comparingDouble(Train::getPrice));
                        path.add(trains.get(0));
                    }
                    case SHORTEST_TIME -> {
                        trains.sort(Comparator.comparingInt(Train::getCostTime));
                        path.add(trains.get(0));
                    }
                }
                // 到达目标城市
                if (s.equals(arrivalCityName)) {
                    paths.add((ArrayList<Train>) path.clone());
                } else {
                    visited.put(s, true);
                    dfs(Main.data.getCity(s), arrivalCityName, visited, paths, path, solutionType);
                }
                path.remove(path.size() - 1);
            }
        });
    }
}
