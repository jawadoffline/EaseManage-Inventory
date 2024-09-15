module com.example.easemanageinventory {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens com.example.easemanageinventory.Model to javafx.base;
    opens com.example.easemanageinventory to javafx.fxml;
    exports com.example.easemanageinventory;
    exports com.example.easemanageinventory.Controller;
    exports com.example.easemanageinventory.Model;
    opens com.example.easemanageinventory.Controller to javafx.fxml;
}