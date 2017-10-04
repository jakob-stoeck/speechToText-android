package de.jakobstoeck.speechtotext;

import android.app.Activity;
import android.content.Intent;

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
            startService(serviceIntent);
            setResult(Activity.RESULT_OK);
        }
        finish();
    }
}
