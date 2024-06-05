package com.example.parallelimages;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.opencv.core.MatOfRect;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        FaceDetector faceDetector = new FaceDetector();
        List<MatOfRect> list = faceDetector.detect(5);
        String str = "nothing(";
        if (list != null){
            str = Integer.toString(list.size());
        }
        welcomeText.setText(str);
    }
}