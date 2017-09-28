package de.jakobstoeck.speechtotext;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
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
        System.out.println("It took so many milliseconds: " + TimeUnit.MILLISECONDS.convert(finish, TimeUnit.NANOSECONDS));
    }

    @Test
    public void recognizeFileTestJson() throws IOException {
        final InputStream in = InstrumentationRegistry.getContext().getClassLoader().getResourceAsStream("test.opus");
        String result = SpeechRecognizerJsonClient.recognizeFile(in);
        in.close();
        assertEquals("oh wie schön Paris", result);
    }

    @Test
    public void recognizeFileTestRpc() throws TimeoutException, InterruptedException, IOException {
        final CountDownLatch speechWasRecognized = new CountDownLatch(1);

        // audio file to test
        final BufferedInputStream in = new BufferedInputStream(InstrumentationRegistry.getContext().getClassLoader().getResourceAsStream("test.opus"));

        // callback listener when service responds
        SpeechService.Listener listener = new SpeechService.Listener() {
            @Override
            public void onSpeechRecognized(String text, boolean isFinal) {
                if (isFinal) {
                    assertEquals("oh wie schön Paris", text);
                    speechWasRecognized.countDown();
                }
            }
        };

        // bind and start service
        Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), SpeechService.class);

        IBinder binder = mServiceRule.bindService(serviceIntent);
        assertNotNull(binder);

        SpeechService sv = SpeechService.from(binder);
        sv.addListener(listener);

        sv.recognizeInputStream(in);
        in.close();
        assertTrue(speechWasRecognized.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void recognizeFileTestStreamingRpc() throws TimeoutException, InterruptedException, IOException {
        final CountDownLatch speechWasRecognized = new CountDownLatch(1);

        // audio file to test
        final BufferedInputStream in = new BufferedInputStream(InstrumentationRegistry.getContext().getClassLoader().getResourceAsStream("test.opus"));

        // callback listener when service responds
        SpeechService.Listener listener = new SpeechService.Listener() {
            @Override
            public void onSpeechRecognized(String text, boolean isFinal) {
                if (isFinal) {
                    assertEquals("oh wie schön Paris", text);
                    speechWasRecognized.countDown();
                }
            }
        };

        // bind and start service
        Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), SpeechService.class);

        IBinder binder = mServiceRule.bindService(serviceIntent);
        assertNotNull(binder);

        SpeechService sv = SpeechService.from(binder);
        sv.addListener(listener);

        sv.startRecognizing(16000);
        final byte[] bytes = new byte[1024];
        int len;
        while ((len = in.read(bytes, 0, 1024)) > 0) {
            sv.recognize(bytes, len);
        }
        sv.finishRecognizing();
        in.close();
        assertTrue(speechWasRecognized.await(10, TimeUnit.SECONDS));
    }
}
