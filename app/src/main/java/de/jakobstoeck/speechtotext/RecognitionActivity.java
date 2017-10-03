package de.jakobstoeck.speechtotext;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

/**
 * Created by jakobstoeck on 10/3/17.
 */

public class RecognitionActivity extends Activity {
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
        }
        finish();
    }
}
