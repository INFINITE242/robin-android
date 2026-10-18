
package com.infinite242.robin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private TextView status;
    private EditText apiKeyInput;
    private SharedPreferences preferences;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean listening = false;

    private String languageCode = "en-IN";
    private Locale selectedLocale = new Locale("en", "IN");
    private final ExecutorService network = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        preferences = getSharedPreferences("RobinSettings", MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(20, 20, 20, 20);
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

        apiKeyInput = new EditText(this);
        apiKeyInput.setSingleLine(true);
        apiKeyInput.setHint("Gemini API key");
        apiKeyInput.setHintTextColor(Color.GRAY);
        apiKeyInput.setTextColor(Color.WHITE);
        apiKeyInput.setInputType(129);
        apiKeyInput.setText(preferences.getString("gemini_api_key", ""));

        Button saveKey = new Button(this);
        saveKey.setText("SAVE API KEY");
        saveKey.setOnClickListener(v -> {
            String key = apiKeyInput.getText().toString().trim();
            if (key.isEmpty()) {
                status.setText("Enter your Gemini API key first.");
            } else {
                preferences.edit().putString("gemini_api_key", key).apply();
                status.setText("API key saved on this phone.");
                Toast.makeText(this, "Key saved", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout languages = new LinearLayout(this);
        languages.setGravity(Gravity.CENTER);
        addLanguageButton(languages, "English", "en-IN",
                new Locale("en", "IN"));
        addLanguageButton(languages, "हिन्दी", "hi-IN",
                new Locale("hi", "IN"));
        addLanguageButton(languages, "ಕನ್ನಡ", "kn-IN",
                new Locale("kn", "IN"));

        status = new TextView(this);
        status.setText("Save your Gemini API key, choose a language, then tap LISTEN.");
        status.setTextSize(17);
        status.setTextColor(Color.LTGRAY);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 20, 0, 20);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.addView(status);

        Button listen = new Button(this);
        listen.setText("🎤  LISTEN");
        listen.setOnClickListener(v -> startListening());

        layout.addView(face);
        layout.addView(title);
        layout.addView(apiKeyInput,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(saveKey);
        layout.addView(languages);
        layout.addView(scroll,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
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
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 10);
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
                int result = tts.setLanguage(locale);
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
            @Override
            public void onReadyForSpeech(Bundle params) {
                listening = true;
                status.setText("I'm listening...");
            }

            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                listening = false;
                status.setText("Thinking...");
            }

            @Override
            public void onError(int error) {
                listening = false;
                status.setText("Couldn't recognize speech. Please try again.");
            }

            @Override
            public void onResults(Bundle results) {
                listening = false;
                ArrayList<String> matches =
                        results.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    askGemini(matches.get(0));
                } else {
                    status.setText("I didn't catch that. Please try again.");
                }
            }

            @Override public void onPartialResults(Bundle results) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 10);
            return;
        }

        if (recognizer == null) {
            createRecognizer();
        }
        if (recognizer == null || listening) return;

        String key = preferences.getString("gemini_api_key", "");
        if (key.isEmpty()) {
            status.setText("Enter and save your Gemini API key first.");
            return;
        }

        if (tts != null) tts.stop();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Robin");
        recognizer.startListening(intent);
    }

    private void askGemini(String heard) {
        String key = preferences.getString("gemini_api_key", "");
        if (key.isEmpty()) {
            status.setText("Please save your Gemini API key first.");
            return;
        }

        status.setText("You: " + heard + "\n\nRobin is thinking...");
        network.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String endpoint =
                        "https://generativelanguage.googleapis.com/v1beta/models/"
                        + "gemini-3.8-flash:generateContent?key=" + key;

                URL url = new URL(endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(30000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject part = new JSONObject();
                String languageInstruction;
                if (languageCode.startsWith("hi")) {
                    languageInstruction = "Reply in natural Hindi.";
                } else if (languageCode.startsWith("kn")) {
                    languageInstruction = "Reply in natural Kannada.";
                } else {
                    languageInstruction = "Reply in natural Indian English.";
                }

                part.put("text",
                        "You are Robin, a friendly voice assistant. "
                        + languageInstruction
                        + " Keep spoken answers clear and fairly brief. "
                        + "The user said: " + heard);

                JSONArray parts = new JSONArray();
                parts.put(part);
                JSONObject content = new JSONObject();
                content.put("parts", parts);
                JSONArray contents = new JSONArray();
                contents.put(content);
                JSONObject body = new JSONObject();
                body.put("contents", contents);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = connection.getResponseCode();
                InputStream stream = code >= 200 && code < 300
                        ? connection.getInputStream() : connection.getErrorStream();

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                if (code < 200 || code >= 300) {
                    throw new Exception("Gemini HTTP " + code + ": " + response);
                }

                JSONObject json = new JSONObject(response.toString());
                JSONArray candidates = json.getJSONArray("candidates");
                JSONObject replyContent = candidates.getJSONObject(0)
                        .getJSONObject("content");
                JSONArray replyParts = replyContent.getJSONArray("parts");
                String answer = replyParts.getJSONObject(0).getString("text").trim();

                runOnUiThread(() -> {
                    status.setText("You: " + heard + "\n\nRobin: " + answer);
                    if (tts != null && ttsReady) {
                        tts.speak(answer, TextToSpeech.QUEUE_FLUSH,
                                null, "robin_reply");
                    } else {
                        status.append("\n\nText-to-speech voice unavailable.");
                    }
                });
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null) message = "Unknown connection error";
                final String error = message.length() > 500
                        ? message.substring(0, 500) : message;
                runOnUiThread(() ->
                        status.setText("Gemini error:\n" + error
                                + "\n\nCheck internet, API key, and model access."));
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        network.shutdownNow();
        super.onDestroy();
    }
}
