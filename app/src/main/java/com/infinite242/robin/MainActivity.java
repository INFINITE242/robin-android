package com.infinite242.robin;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
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

        TextView status = new TextView(this);
        status.setText("Your AI companion is waking up...");
        status.setTextSize(16);
        status.setTextColor(Color.LTGRAY);
        status.setGravity(Gravity.CENTER);

        layout.addView(face);
        layout.addView(title);
        layout.addView(status);
        setContentView(layout);
    }
}
