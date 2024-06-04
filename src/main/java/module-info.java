module com.example.parallelimages {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;


    opens com.example.parallelimages to javafx.fxml;
    exports com.example.parallelimages;
}