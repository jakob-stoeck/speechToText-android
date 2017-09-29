package de.jakobstoeck.speechtotext;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);

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

        Intent intent = getIntent();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(intent.getAction()) && type != null) {
            // was launched by a share action
            // do recognition and return
            Intent serviceIntent = new Intent(this, SpeechService.class);
            serviceIntent.setType(intent.getType());
            serviceIntent.putExtras(intent);
            if (!type.startsWith("audio/")) {
                // XXX just for testing purposes, remove before publishing together with intent-filter
                Resources resources = getResources();
                int resourceId = R.raw.test;
                Uri uri = new Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(resources.getResourcePackageName(resourceId))
                        .appendPath(resources.getResourceTypeName(resourceId))
                        .appendPath(resources.getResourceEntryName(resourceId))
                        .build();
                serviceIntent.setType("audio/ogg");
                serviceIntent.putExtra(Intent.EXTRA_STREAM, uri);
            }
            startService(serviceIntent);
            setResult(Activity.RESULT_OK);
            finish();
        }
    }
}
