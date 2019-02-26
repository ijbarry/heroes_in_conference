/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see http://www.gnu.org/licenses/
 */
package uk.ac.cam.cl.kilo;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 * ProfileAugemenation.java
 *
 * @author India-Jane Barry
 */
public class ProfileAugmentation {
  private static final String CLASSIFIER = "res/classifiers/haarcascade_frontalface_alt.xml",
      HATS = "res/hats/",
      MASKS = "res/masks/",
      TEXT = "res/text/";
  private static ProfileAugmentation instance;

  private CascadeClassifier faceCascade;
  private List<Mat> hats = new ArrayList<>(), masks = new ArrayList<>(), texts = new ArrayList<>();

  private ProfileAugmentation() {
    // Load the libraries
    nu.pattern.OpenCV.loadShared();
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    faceCascade = new CascadeClassifier(CLASSIFIER);
    // Initialise the hats
    for (File file : new File(HATS).listFiles()) {
      if (file.isFile()) hats.add(Imgcodecs.imread(file.getPath()));
    }
    for (File file : new File(MASKS).listFiles()) {
      if (file.isFile()) masks.add(Imgcodecs.imread(file.getPath()));
    }
    for (File file : new File(TEXT).listFiles()) {
      if (file.isFile()) texts.add(Imgcodecs.imread(file.getPath()));
    }
  }

  /** @return the profile augmentation instance */
  public static ProfileAugmentation getInstance() {
    if (instance == null) instance = new ProfileAugmentation();
    return instance;
  }

  /**
   * Augments the image at the given path.
   *
   * @param path the path for the image to enhance
   */
  public void augment(Path path) {
    // Read the photo
    Mat photo = Imgcodecs.imread(path.toString());
    // Detect the faces
    Rect[] faces = detectFrontalFace(photo);
    // Add hats/masks/text
    if (faces.length > 0) {
      for (Rect face : faces) addHat(photo, face);
    } else addText(photo);
    // Write the result back
    imwrite(path.toString(), photo);
  }

  private void addText(Mat photo) {
    Mat text = texts.get(new Random().nextInt(texts.size()));

    double oldTextHeight = text.height();
    double oldTextWidth = text.width();

    double newTextHeight = oldTextHeight * photo.width() / oldTextWidth;
    double newTextWidth = photo.width();

    Size resizeTo = new Size(newTextWidth, newTextHeight);
    Mat resizedText = new Mat();
    Imgproc.resize(text, resizedText, resizeTo);
    Rect roi = new Rect(0, 0, (int) resizeTo.width, (int) resizeTo.height);
    Mat destinationROI = photo.submat(roi);
    resizedText.copyTo(destinationROI, resizedText);
  }

  private void addHat(Mat photo, Rect face) {
    Mat hat = texts.get(new Random().nextInt(hats.size()));

    double hatHeight = hat.height();
    double hatWidth = hat.width();
    int targetWidth = face.width;
    int targetHeight = (int) (hatHeight * targetWidth / hatWidth);
    int roiX = face.x;
    int roiY = (int) (face.y - 0.6 * targetHeight);

    if (roiX < 0) roiX = 0;
    if (roiY < 0) roiY = 0;

    if (hatHeight + roiY > face.height) addMask(photo, face);
    else {
      Mat resizedBeret = new Mat();
      Imgproc.resize(hat, resizedBeret, new Size(targetWidth, targetHeight));
      Rect roi = new Rect(roiX, roiY, targetWidth, targetHeight);
      Mat destinationROI = photo.submat(roi);
      resizedBeret.copyTo(destinationROI, resizedBeret);
    }
  }

  private void addMask(Mat photo, Rect face) {
    Mat mask = texts.get(new Random().nextInt(masks.size()));
    Mat resizedMask = new Mat();
    Imgproc.resize(mask, resizedMask, face.size());
    Mat destinationROI = photo.submat(face);
    resizedMask.copyTo(destinationROI, resizedMask);
  }

  private Rect[] detectFrontalFace(Mat photo) {
    if (photo.empty()) return new Rect[0];

    Mat greyPhoto = new Mat();
    Imgproc.cvtColor(photo, greyPhoto, Imgproc.COLOR_BGR2GRAY);

    Imgproc.equalizeHist(greyPhoto, greyPhoto);
    MatOfRect face = new MatOfRect();

    faceCascade.detectMultiScale(
        greyPhoto, face, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE, new Size(50, 50), new Size());

    Rect[] faceArray = face.toArray();
    return faceArray;
  }
}
