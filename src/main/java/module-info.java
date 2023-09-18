module com.example.algotest {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.datatransfer;
    requires java.desktop;
    requires okhttp3;


    opens GUI to javafx.fxml;
    exports GUI;
}