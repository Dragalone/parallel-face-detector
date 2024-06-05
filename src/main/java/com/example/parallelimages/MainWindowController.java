package com.example.parallelimages;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.MatOfRect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;

public class MainWindowController implements Initializable {

    private final File inputFolder = new File("./src/main/resources/images/input");

    private final File outputFolder = new File("./src/main/resources/images/output");

    private List<Image> images = new ArrayList<>();

    private boolean isInputState = true;

    @FXML
    private Button initialDataButton;

    @FXML
    private Button outputDataButton;

    @FXML
    private ListView<String> myListView;

    @FXML
    private ImageView imageView;

    @FXML
    private Label timeElapsed;

    @FXML
    private Spinner<Integer> threadSpinner;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updateForm(inputFolder);
        myListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                int index = myListView.getSelectionModel().getSelectedIndex();
                if(index != -1){
                    if (index < images.size()) {
                        imageView.setImage(images.get(index));
                    }
                }
            }
        });
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 14);
        valueFactory.setValue(1);
        threadSpinner.setValueFactory(valueFactory);
        initialDataButton.setDisable(true);
    }

    @FXML
    protected void onStartButtonClick() {
        FaceDetector faceDetector = new FaceDetector();
        long t0 = System.nanoTime();
        faceDetector.detect(threadSpinner.getValue());
        long t = System.nanoTime() - t0;
        timeElapsed.setText((t / 1e9) + " секунд");
        System.gc();
    }

    @FXML
    protected void onUpdateButtonClick() {
        if (isInputState) {
            updateForm(inputFolder);
        } else {
            updateForm(outputFolder);
        }
    }

    private void updateForm(File folder){
        List<String> imagesPathsList = FileUtils.listFilesForFolder(folder);
        List<String> imageNames = new ArrayList<>();
        images.clear();
        for(var imagePath : imagesPathsList){
            File file = new File(imagePath);
            imageNames.add(file.getName());
            images.add(new Image(file.toURI().toString()));
        }
        myListView.getItems().setAll(imageNames);
    }

    @FXML
    protected void onInitialDataClick() {
        outputDataButton.setDisable(false);
        initialDataButton.setDisable(true);
        isInputState = true;
        updateForm(inputFolder);
    }

    @FXML
    protected void onOutputDataClick() {
        outputDataButton.setDisable(true);
        initialDataButton.setDisable(false);
        isInputState = false;
        updateForm(outputFolder);
    }
}
