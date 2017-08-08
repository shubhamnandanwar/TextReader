package com.clabs.textreader;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private TessBaseAPI mTess;
    private String dataPath = "";

    private TextToSpeech tts;

    private static final int CAMERA_REQUEST = 1888;
    private TextView textView;
    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.ocr_text_view);
        Button button = (Button) findViewById(R.id.capture_image_button);

        tts = new TextToSpeech(this, this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        dataPath = getFilesDir() + "/tesseract/";
        checkFile(new File(dataPath + "tessdata/"));
        String lang = "eng";
        mTess = new TessBaseAPI();
        mTess.init(dataPath, lang);
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void copyFiles() {
        try {
            String filepath = dataPath + "/tessdata/eng.traineddata";

            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkFile(File dir) {
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        if (dir.exists()) {
            String dataFilePath = dataPath + "/tessdata/eng.traineddata";
            File datafile = new File(dataFilePath);
            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    public void processImage(Bitmap image) {
        mTess.setImage(image);
        text = mTess.getUTF8Text();
        textView.setText(text);
        speakOut();
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            processImage(photo);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut();
            }

        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

    private void speakOut() {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
