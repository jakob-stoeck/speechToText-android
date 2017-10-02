package de.jakobstoeck.speechtotext;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("de.jakobstoeck.speechtotext", appContext.getPackageName());
    }

    private long start;

    @Before
    public void start() {
        start = System.nanoTime();
    }

    @After
    public void end() {
        final long finish = System.nanoTime() - start;
        System.out.println(String.format("It took so many milliseconds: %d\n", TimeUnit.MILLISECONDS.convert(finish, TimeUnit.NANOSECONDS)));
    }

    private Uri getUriFromResourceId(int resourceId) {
        final Resources resources = InstrumentationRegistry.getTargetContext().getResources();
        Uri uri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resourceId))
                .appendPath(resources.getResourceTypeName(resourceId))
                .appendPath(resources.getResourceEntryName(resourceId))
                .build();
        return uri;
    }

    @Test
    public void recognizeFileTestJson() throws IOException {
        final Uri uri = getUriFromResourceId(R.raw.test);
        final InputStream in = InstrumentationRegistry.getContext().getContentResolver().openInputStream(uri);
        String result = SpeechRecognizerJsonClient.recognizeFile(in);
        in.close();
        assertEquals("hallo das ist ein Test", result);
    }

    @Test
    public void recognizeFileTestRpc() throws TimeoutException, InterruptedException, IOException {
        final CountDownLatch speechWasRecognized = new CountDownLatch(1);

        // audio file to test
        final Uri uri = getUriFromResourceId(R.raw.test);
        final String[] transcribedText = new String[1];

        // callback listener when service responds
        SpeechService.Listener listener = new SpeechService.Listener() {
            @Override
            public void onSpeechRecognized(String text, boolean isFinal) {
                if (isFinal) {
                    transcribedText[0] = text;
                    speechWasRecognized.countDown();
                }
            }
        };

        // bind and start service
        Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), SpeechService.class);
        serviceIntent.setTypeAndNormalize("audio/ogg");
        serviceIntent.putExtra(Intent.EXTRA_STREAM, uri);
        IBinder binder = mServiceRule.bindService(serviceIntent);
        assertNotNull(binder);

        SpeechService sv = SpeechService.from(binder);
        sv.addListener(listener);
        sv.recognizeUri((Uri) serviceIntent.getExtras().get(Intent.EXTRA_STREAM), serviceIntent.getType());
        assertTrue(speechWasRecognized.await(10, TimeUnit.SECONDS));
        assertEquals("hallo das ist ein Test", transcribedText[0]);
    }
}
