package com.example.parallelimages;

import org.opencv.core.*;


import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
    private static void removeDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File aFile : files) {
                    removeDirectory(aFile);
                }
            }
            dir.delete();
        } else {
            dir.delete();
        }
    }
    private static void cleanDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File aFile : files) {
                    removeDirectory(aFile);
                }
            }
        }
    }

    private static List<List<String>> assignTasks(List<String> imagePaths, int threadsCount){
        int partitionSize = imagePaths.size() / threadsCount;
        int firstCycleLimit = partitionSize * threadsCount;
        List<List<String>> imagePathsPerThread = new ArrayList<>();
        for (int i = 0; i < firstCycleLimit; i += partitionSize) {
            imagePathsPerThread.add(new ArrayList<>(imagePaths.subList(i,
                    Math.min(i + partitionSize, imagePaths.size()))));
        }
        for (int i = firstCycleLimit, j = 0; i < imagePaths.size(); i++, j++) {
            imagePathsPerThread.get(j).add(imagePaths.get(i));
        }
        return imagePathsPerThread;
    }

    public static class Detector implements Runnable {

        private static volatile int result;

        private final String classifier_xml_path;

        private final List<String> imagePaths;

        private final String outputFolderName;

        public Detector(String outputFolderName, String classifierXmlPath, List<String> imagePaths) {
            classifier_xml_path = classifierXmlPath;
            this.imagePaths = imagePaths;
            this.outputFolderName = outputFolderName;
        }

        @Override
        public void run() {
            CascadeClassifier faceDetector = new CascadeClassifier(classifier_xml_path);

            Rect[] detectedFaces;
            long t0 = System.nanoTime();
            for (String imagePath : imagePaths) {
                Mat image = Imgcodecs.imread(imagePath);
                Mat grayImage = new Mat();
                Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
                MatOfRect faceDetections = new MatOfRect();
                faceDetector.detectMultiScale(grayImage, faceDetections);
                for (Rect rect : faceDetections.toArray()) {
                    Imgproc.rectangle(
                            image, new Point(rect.x, rect.y),
                            new Point(rect.x + rect.width,
                                    rect.y + rect.height),
                            new Scalar(0, 255, 0));
                }
                String filename = UUID.randomUUID().toString() + ".jpg";
                Imgcodecs.imwrite(outputFolderName + filename, image);
            }
        }
    }


    public List<MatOfRect> detect(int threadsCount) {
        final File folder = new File("./src/main/resources/images/input");
        cleanDirectory(new File("./src/main/resources/images/output/"));
        // Загрузка изображений
        List<String> imagePaths = listFilesForFolder(folder); // Список путей к изображениям

        threadsCount = Math.min(threadsCount, imagePaths.size());

        List<List<String>> imagePathsPerThread = assignTasks(imagePaths, threadsCount);

        // Количество потоков не превышает количество картинок
        Thread[] threads = new Thread[threadsCount];
        long t0 = System.nanoTime();
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(new Detector("./src/main/resources/images/output/", "./src/main/resources/haarcascade_frontalface_default.xml", imagePathsPerThread.get(i)));
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