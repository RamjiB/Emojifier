package com.example.android.facedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import static android.icu.lang.UCharacter.JoiningGroup.E;


public class Emojifier {

    private static final String TAG = "Emojifier";

    private static final double SMILING_PROB_THRESHOLD = .15;
    private static final double EYE_OPEN_PROB_THRESHOLD = .5;
    private static final float EMOJI_SCALE_FACTOR = 0.9f;

    /**
     * detect number of faces in the image
     * @param context
     * @param image
     */


    static Bitmap detectFacesAndOverlayEmoji(Context context,Bitmap image){

        //Create the face detector,disable tracking and enable classification

        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        //Build the frame
        Frame frame = new Frame.Builder().setBitmap(image).build();

        //Detect the faces
        SparseArray<Face> faces = detector.detect(frame);

        //Log the number of faces
        Log.d(TAG,"detectedFaces: Number of faces = " + faces.size());

        //Initialize result bitmap to original picture
        Bitmap resultBitmap = image;

        // if no faces detected
        if (faces.size() == 0){
            Toast.makeText(context, "No faces detected", Toast.LENGTH_SHORT).show();

        }else{
            for (int i = 0; i < faces.size(); i++){
                Face face= faces.valueAt(i);

                Bitmap emojiBitmap;

                switch (whichEmoji(face)){

                    case SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.smile);
                        break;
                    case FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.frown);
                        break;
                    case LEFT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwink);
                        break;
                    case LEFT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwink);
                        break;
                    case RIGHT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwinkfrown);
                        break;
                    case CLOSED_EYE_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_frown);
                        break;
                    case CLOSED_EYE_SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_smile);
                        break;

                    default:
                        emojiBitmap = null;
                        Toast.makeText(context, "No Emoji", Toast.LENGTH_SHORT).show();
                }

                // Add the emoji bitmap to the proper position in the original image

                resultBitmap = addBitmapToFace(resultBitmap,emojiBitmap,face);
            }
        }

        //Release the detector
        detector.release();

        return resultBitmap;

    }

    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {

        // Initialise the results bitmap to a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(),backgroundBitmap.getConfig());

        //Scale the emoji so it looks better on the face
        float scaleFactor = EMOJI_SCALE_FACTOR;


        //Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *  newEmojiWidth/emojiBitmap.getWidth() * scaleFactor);

        //Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap,newEmojiWidth,newEmojiHeight,false);

        //Determine the emoji position so it best lines up with the face
        float emojiPositionX = (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() /2;
        float emojiPostionY = (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;


        //Create the canvas and draw the bitmap to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap,0,0,null);
        canvas.drawBitmap(emojiBitmap,emojiPositionX,emojiPostionY,null);

        return resultBitmap;

    }

    /**
     * Method for logging the classification probabilities
     * @param face
     */

    private static Emoji whichEmoji(Face face){

        //Log all the probabilities
        Log.d(TAG,"whichEmoji: smilingProb = " + face.getIsSmilingProbability());
        Log.d(TAG,"whichEmoji: leftEyeOpenProbability = " + face.getIsLeftEyeOpenProbability());
        Log.d(TAG,"whichEmoji: rightEyeOpenProbability = " + face.getIsRightEyeOpenProbability());

        boolean smiling = face.getIsSmilingProbability() > SMILING_PROB_THRESHOLD;
        boolean leftEyeClosed = face.getIsLeftEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;
        boolean rightEyeClosed = face.getIsRightEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;

        //Determine and log the appropriate emoji

        Emoji emoji;
        if (smiling){
            if (leftEyeClosed && !rightEyeClosed){
                emoji = Emoji.LEFT_WINK;
            }else if (!leftEyeClosed && rightEyeClosed){
                emoji = Emoji.RIGHT_WINK;
            }else if (leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_SMILE;
            }else {
                emoji = Emoji.SMILE;
            }
        }else{
            if (leftEyeClosed && !rightEyeClosed){
                emoji = Emoji.LEFT_WINK_FROWN;
            }else if (!leftEyeClosed && rightEyeClosed){
                emoji = Emoji.RIGHT_WINK_FROWN;
            }else if (leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_FROWN;
            }else {
                emoji = Emoji.FROWN;
            }
        }

        //Log the chosen Emoji
        Log.d(TAG,"whichEmoji : " + emoji.name());

        return emoji;
    }

    private enum Emoji{

        SMILE,
        FROWN,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWN,
        RIGHT_WINK_FROWN,
        CLOSED_EYE_SMILE,
        CLOSED_EYE_FROWN

    }
}





















