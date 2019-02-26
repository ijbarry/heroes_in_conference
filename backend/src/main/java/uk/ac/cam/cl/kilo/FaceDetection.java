package uk.ac.cam.cl.kilo;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class FaceDetection {
    static String[] hats = {"resources/Black_wizard_hat.png",
            "resources/Cosmic_tiara.png",
            "resources/Chefs_hat.png",
            "resources/Black_beret.png",
            "resources/Blue_beret.png",
            "resources/Nurse_hat.png",
            "resources/red_party_hat.png",
            };

    static String[] masks = {"resources/Goblin_mask.png",
            "resources/Black_unicorn_mask.png",
            "resources/White_unicorn_mask.png"
    };

    static String[] texts = {"resources/buying-gf.png"
    };

    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String path = args[0];
        Mat photo = Imgcodecs.imread(path);

        Rect[] FrontfacesArray = detectFrontalFace(photo);
        if(FrontfacesArray.length >0) {
            addHat(photo,FrontfacesArray,path);
        }
        else{
            addText(photo,path);
        }

    }

    static void addText(Mat photo,String PhotoPath){
        String path = texts[new Random().nextInt(1)];
        Mat text = Imgcodecs.imread(path);

        double oldTextHeight = text.height();
        double oldTextWidth = text.width();

        double newTextHeight = oldTextHeight * photo.width()/oldTextWidth;
        double newTextWidth = photo.width();

        Size resizeTo = new Size(newTextWidth,newTextHeight);
        Mat resizedText = new Mat();
        Imgproc.resize(text,resizedText, resizeTo);
        Rect roi = new Rect(0,0,(int)resizeTo.width,(int)resizeTo.height);
        Mat destinationROI = photo.submat(roi);
        resizedText.copyTo(destinationROI,resizedText);

        imwrite( PhotoPath, photo );

    }

    static void addHat(Mat photo,Rect[] faces, String PhotoPath){

        List<Rect> MaskFaces = new ArrayList<Rect>();

        for (int i = 0; i < faces.length; i++) {
            String path = hats[new Random().nextInt(7)];
            Mat hat = Imgcodecs.imread(path);
            double Height = hat.height();
            double Width = hat.width();
            int hatWidth =  faces[i].width;
            int hatHeight = (int) (Height * hatWidth/Width);
            int roiX = faces[i].x;
            int roiY = (int) (faces[i].y - 0.6 * hatHeight);

            if(roiX<0){
                roiX =0;
            }
            if(roiY<0){
                roiY =0;
            }

            if(hatHeight+roiY>faces[i].height){
                MaskFaces.add(faces[i]);
            }

            else {
                double resizeTo = faces[i].width;
                Mat resizedBeret = new Mat();
                Imgproc.resize(hat, resizedBeret, new Size(hatWidth, hatHeight));
                Rect roi = new Rect(roiX, roiY, hatWidth, hatHeight);
                Mat destinationROI = photo.submat(roi);
                resizedBeret.copyTo(destinationROI, resizedBeret);
            }
            if(!MaskFaces.isEmpty()){
                Rect[] maskFaces = (Rect[]) MaskFaces.toArray();
                addMask(photo,maskFaces,path);
            }
        }

        imwrite( PhotoPath, photo );

    }

    static void addMask(Mat photo,Rect[] faces,String PhotoPath){

        for (int i = 0; i < faces.length; i++) {
            String path = masks[new Random().nextInt(3)];
            Mat mask = Imgcodecs.imread(path);

            Mat resizedMask = new Mat();

            int height_temp = faces[i].height;
            int width_temp = faces[i].width;

            int x = faces[i].x;
            int y = (int) (faces[i].y - height_temp*0.3);
            int w = faces[i].width;
            int h = (int)(height_temp*1.6);
            Imgproc.resize(mask,resizedMask, faces[i].size());

            Mat destinationROI = photo.submat(faces[i]);
            resizedMask.copyTo(destinationROI,resizedMask);
        }

        imwrite( PhotoPath, photo );

    }

    static Rect[] detectFrontalFace(Mat photo) throws NullPointerException{
        CascadeClassifier faceCascade = new CascadeClassifier("resources/haarcascade_frontalface_alt.xml");

        if(photo.empty() ) {
            throw new NullPointerException("photo is empty");
        }

        Mat greyPhoto = new Mat();
        Imgproc.cvtColor( photo, greyPhoto, Imgproc.COLOR_BGR2GRAY );

        Imgproc.equalizeHist(greyPhoto, greyPhoto);
        MatOfRect faces = new MatOfRect();

        faceCascade.detectMultiScale(greyPhoto, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(50,50), new Size());


        Rect[] facesArray = faces.toArray();
        return facesArray;

    }

}
