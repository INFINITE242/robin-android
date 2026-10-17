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

    private String languageCode = "en-IN";
    private Locale selectedLocale = new Locale("en", "IN");

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
        status.setText("Choose a language, then tap LISTEN.");
        status.setTextSize(17);
        status.setTextColor(Color.LTGRAY);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 24, 0, 24);

        LinearLayout languages = new LinearLayout(this);
        languages.setGravity(Gravity.CENTER);

        addLanguageButton(languages, "English", "en-IN", new Locale("en", "IN"));
        addLanguageButton(languages, "हिन्दी", "hi-IN", new Locale("hi", "IN"));
        addLanguageButton(languages, "ಕನ್ನಡ", "kn-IN", new Locale("kn", "IN"));

        Button listen = new Button(this);
        listen.setText("🎤  LISTEN");
        listen.setOnClickListener(v -> startListening());

        layout.addView(face);
        layout.addView(title);
        layout.addView(languages);
        layout.addView(status);
        layout.addView(listen);
        setContentView(layout);

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                int voiceResult = tts.setLanguage(selectedLocale);
                ttsReady = voiceResult != TextToSpeech.LANG_MISSING_DATA
                        && voiceResult != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO}, 10);
        }

        createRecognizer();
    }

    private void addLanguageButton(LinearLayout row, String label,
                                   String code, Locale locale) {
        Button button = new Button(this);
        button.setText(label);
        button.setOnClickListener(v -> {
            languageCode = code;
            selectedLocale = locale;

            if (tts != null && ttsReady) {
                int result = tts.setLanguage(selectedLocale);
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED;
            }

            status.setText("Language: " + label + ". Tap LISTEN.");
        });
        row.addView(button);
    }

    private void createRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.setText("Speech recognition isn't available on this phone.");
            return;
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            public void onReadyForSpeech(Bundle params) {
                status.setText("I'm listening...");
            }

            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float rmsdB) {}
            public void onBufferReceived(byte[] buffer) {}

            public void onEndOfSpeech() {
                status.setText("Processing speech...");
            }

            public void onError(int error) {
                status.setText("Couldn't recognize speech. Check language support and try again.");
            }

            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && !matches.isEmpty()) {
                    String heard = matches.get(0);
                    status.setText("You said:\n" + heard + "\n\nRobin: " + heard);

                    if (ttsReady && tts != null) {
                        tts.speak(heard, TextToSpeech.QUEUE_FLUSH, null, "robin_reply");
                    } else {
                        status.append("\n\nA voice for this language isn't available.");
                    }
                } else {
                    status.setText("I didn't catch that. Please try again.");
                }
            }

            public void onPartialResults(Bundle partialResults) {}
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO}, 10);
            return;
        }

        if (recognizer == null) {
            createRecognizer();
        }
        if (recognizer == null) return;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Robin");
        recognizer.startListening(intent);
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
