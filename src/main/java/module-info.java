module com.example.expense {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;
    requires java.xml.crypto;
    requires javafx.base;
    requires jxl;

    opens com.example.expense to javafx.fxml;
    exports com.example.expense;
}