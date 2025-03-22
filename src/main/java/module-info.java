module com.example.daaproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.daaproject to javafx.fxml;
    exports com.example.daaproject;
}