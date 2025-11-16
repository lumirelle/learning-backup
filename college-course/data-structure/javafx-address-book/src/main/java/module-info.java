module com.waoap.addressbook {
    requires javafx.controls;
    requires javafx.fxml;
    requires pinyin4j;

    opens com.waoap.addressbook to javafx.fxml;
    exports com.waoap.addressbook;
    exports com.waoap.addressbook.utils;
    exports com.waoap.addressbook.model;
    opens com.waoap.addressbook.model to javafx.fxml;
}