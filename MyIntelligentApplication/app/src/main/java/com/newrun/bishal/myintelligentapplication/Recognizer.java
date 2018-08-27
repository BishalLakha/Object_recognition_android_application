package com.newrun.bishal.myintelligentapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class Recognizer {

    static {
        System.loadLibrary("tensorflow_inference");
    }

    private Context context;

    //PATH TO OUR MODEL FILE AND NAMES OF THE INPUT AND OUTPUT NODES
    private String MODEL_PATH = "file:///android_asset/squeezenet.pb";
    private String INPUT_NAME = "input_1";
    private String OUTPUT_NAME = "output_1";
    private TensorFlowInferenceInterface tf;

    //ARRAY TO HOLD THE PREDICTIONS AND FLOAT VALUES TO HOLD THE IMAGE DATA
    float[] PREDICTIONS = new float[1000];
    private float[] floatValues;
    private int[] INPUT_SIZE = {224, 224, 3};

    public Recognizer(Context context) {
        this.context = context;

        initRecognizer();
    }

    private void initRecognizer() {
        //initialize tensorflow with the AssetManager and the Model
        tf = new TensorFlowInferenceInterface(context.getAssets(), MODEL_PATH);
    }

    public Object[] argmax(float[] array) {
        int best = -1;
        float best_confidence = 0.0f;

        for (int i = 0; i < array.length; i++) {
            float value = array[i];
            if (value > best_confidence) {
                best_confidence = value;
                best = i;
            }
        }
        return new Object[]{best, best_confidence};
    }

    public void predict(final Bitmap bitmap, final OnPredictedCallback predictionCallback) {

        //Runs inference in background thread
        new AsyncTask<Integer, Integer, Integer>() {

            @Override
            protected Integer doInBackground(Integer... params) {

                //Resize the image into 224 x 224
                Bitmap resized_image = ImageUtils.processBitmap(bitmap, 224);

                //Normalize the pixels
                floatValues = ImageUtils.normalizeBitmap(resized_image, 224, 127.5f, 1.0f);
                //Pass input into the tensorflow
                tf.feed(INPUT_NAME, floatValues, 1, 224, 224, 3);
                //compute predictions
                tf.run(new String[]{OUTPUT_NAME});
                //copy the output into the PREDICTIONS array
                tf.fetch(OUTPUT_NAME, PREDICTIONS);
                //Obtained highest prediction
                Object[] results = argmax(PREDICTIONS);

                int class_index = (Integer) results[0];
                float confidence = (Float) results[1];

                try {

                    final String conf = String.valueOf(confidence * 100).substring(0, 5);
                    //Convert predicted class index into actual label name
                    final String label = ImageUtils.getLabel(context.getAssets().open("labels.json"), class_index);

                    // TODO: 8/27/2018 Callback to main
                    predictionCallback.onPredictionComplete(conf, label);

                } catch (Exception e) {

                }

                return 0;
            }

        }.execute(0);
    }

    public interface OnPredictedCallback {
        void onPredictionComplete(String confidence, String label);
    }
}
