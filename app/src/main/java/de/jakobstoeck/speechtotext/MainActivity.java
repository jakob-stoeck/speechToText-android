package de.jakobstoeck.speechtotext;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    void updateData() {
        // set text view to latest transcription
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final TextView textView = findViewById(R.id.last_message);
        textView.setText(sharedPref.getString(getString(R.string.last_message_transcription), getString(R.string.last_message)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);

        updateData();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateData();
    }
}
