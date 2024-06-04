package com.example.parallelimages;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;


import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FaceDetector {
    public static List<String> listFilesForFolder(final File folder) {
        List<String> fileNames = new ArrayList<>();
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                fileNames.add(fileEntry.getPath());
            }
        }
        return fileNames;
    }

    public static class Detector implements Runnable {

        private static volatile int result;

        private final String classifier_xml_path;

        private final List<String> imagePaths;

        public Detector(File folder, String classifierXmlPath, List<String> imagePaths) {
            classifier_xml_path = classifierXmlPath;
            this.imagePaths = imagePaths;
        }

        @Override
        public void run() {
            CascadeClassifier faceDetector = new CascadeClassifier(classifier_xml_path);

            List<MatOfRect> detectedFaces = new ArrayList<>();
            long t0 = System.nanoTime();
            for (String imagePath : imagePaths) {
                Mat image = Imgcodecs.imread(imagePath);
                Mat grayImage = new Mat();
                Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
                List<MatOfRect> faces = new ArrayList<>();
                MatOfRect face = new MatOfRect();
                faceDetector.detectMultiScale(grayImage, face);
                faces.add(face);
                detectedFaces.addAll(faces);
            }

            System.out.println("Detected " + detectedFaces.size() + " faces");

        }
    }


    public List<MatOfRect> detect(int threadsCount) {
        final File folder = new File("./src/main/resources/images");

        // Загрузка изображений
        List<String> imagePaths = listFilesForFolder(folder); // Список путей к изображениям

        threadsCount = Math.min(threadsCount, imagePaths.size());

        int partitionSize = imagePaths.size() % threadsCount == 0 ? imagePaths.size() / threadsCount : imagePaths.size() / threadsCount + 1;
        System.out.println("partitionSize " + partitionSize);
        List<List<String>> imagePathsPerThread = new ArrayList<>();
        for (int i = 0; i < imagePaths.size(); i += partitionSize) {
            imagePathsPerThread.add(imagePaths.subList(i,
                    Math.min(i + partitionSize, imagePaths.size())));
        }
        System.out.println("imagePathsPerThreadsize" + imagePathsPerThread.size());
        // Количество потоков не превышает количество картинок
        Thread[] threads = new Thread[threadsCount];
        long t0 = System.nanoTime();
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(new Detector(folder, "./src/main/resources/haarcascade_frontalface_default.xml", imagePathsPerThread.get(i)));
            threads[i].start();
        }
        for (int i = 0; i < threadsCount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        long t = System.nanoTime() - t0;
        System.out.println(t / 1e9 + " Seconds required to solve this problem");
        return null;
        // ...  Дополнительная обработка результатов (например, отрисовка прямоугольников на изображениях) ...
    }
}