package de.jakobstoeck.speechtotext;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.Random;

/**
 * Created by jakobstoeck on 9/22/17.
 */

public class SpeechRecognitionService extends IntentService {
    public SpeechRecognitionService() {
        super(SpeechRecognitionService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        if (intent.getType().indexOf("audio/") == 0) {
            handleSentAudio(intent);
        } else if (intent.getType().equals("text/plain")) {
            handleSentText(intent);
        }
    }

    void handleSentAudio(Intent intent) {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri != null) {
            String response = SpeechRecognizerJsonClient.recognizeFile(uri, this);
            showNotification("Speech to Text", response);
        }
    }

    void handleSentText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            showNotification("Speech to Text", sharedText);
        }
    }

    void showNotification(String title, String body) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(title)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentText(body);
        Random r = new Random();
        int mNotificationId = r.nextInt();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
