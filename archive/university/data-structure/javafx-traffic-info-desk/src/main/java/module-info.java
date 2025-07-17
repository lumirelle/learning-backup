module com.waoap.trafficinfodesk {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.waoap.trafficinfodesk.ui;
    exports com.waoap.trafficinfodesk.data;
    opens com.waoap.trafficinfodesk.ui to javafx.fxml;
}