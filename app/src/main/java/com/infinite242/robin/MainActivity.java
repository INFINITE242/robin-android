package com.infinite242.robin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView status;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(24, 24, 24, 24);
        layout.setBackgroundColor(Color.rgb(12, 18, 32));

        TextView face = new TextView(this);
        face.setText("◉     ◉");
        face.setTextSize(42);
        face.setTextColor(Color.rgb(80, 230, 200));
        face.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("ROBIN");
        title.setTextSize(30);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("Hello! Tap below and talk to me.");
        status.setTextSize(17);
        status.setTextColor(Color.LTGRAY);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 24, 0, 24);

        Button listen = new Button(this);
        listen.setText("🎤  LISTEN");
        listen.setOnClickListener(v -> startListening());

        layout.addView(face);
        layout.addView(title);
        layout.addView(status);
        layout.addView(listen);
        setContentView(layout);

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault());
                ttsReady = true;
            }
        });

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO}, 10);
        }

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                public void onReadyForSpeech(Bundle params) {
                    status.setText("I'm listening...");
                }
                public void onBeginningOfSpeech() {}
                public void onRmsChanged(float rmsdB) {}
                public void onBufferReceived(byte[] buffer) {}
                public void onEndOfSpeech() {
                    status.setText("Thinking...");
                }
                public void onError(int error) {
                    status.setText("Couldn't hear that. Tap LISTEN to try again.");
                }
                public void onResults(Bundle results) {
                    ArrayList<String> matches =
                        results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        reply(matches.get(0));
                    } else {
                        status.setText("I didn't catch that. Please try again.");
                    }
                }
                public void onPartialResults(Bundle partialResults) {}
                public void onEvent(int eventType, Bundle params) {}
            });
        } else {
            status.setText("Speech recognition isn't available on this phone.");
        }
    }

    private void startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO}, 10);
            return;
        }
        if (recognizer == null) {
            status.setText("Speech recognition isn't available.");
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Robin");
        recognizer.startListening(intent);
    }

    private void reply(String heard) {
        String response = "I heard you say: " + heard;
        status.setText("You said:\n" + heard + "\n\n" + response);
        if (ttsReady) {
            tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, "robin_reply");
        }
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
