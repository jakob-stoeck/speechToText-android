package de.jakobstoeck.speechtotext;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jakobstoeck on 9/20/17.
 */

class SpeechRecognizerJsonClient {
    private static final String TAG = "SpeechRecognizerJson";
    static String recognizeFile(Uri uri, Context context) {
        try {
            // get stream from file
            InputStream in = context.getContentResolver().openInputStream(uri);
            return recognizeFile(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static String recognizeFile(InputStream in) {
        // upload stream to google cloud speech recognition
        try {
            // request
            String key = "AIzaSyBfLWNF5Ygz2s9MQDNBWK9pY8ZdcAcj2x4";
            URL url = new URL("https://speech.googleapis.com/v1/speech:recognize?key=" + key);
            HttpURLConnection client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            client.setDoOutput(true);
            client.setChunkedStreamingMode(0); // since we’re streaming we don’t know the size beforehand
            OutputStream out = new BufferedOutputStream(client.getOutputStream());
            // new speechrequest
            String preMsg = "{\"audio\": {\"content\": \"";
            String postMsg = "\"},\"config\": {\"languageCode\": \"de-DE\",\"encoding\": \"OGG_OPUS\",\"sampleRateHertz\": 16000}}";
            base64encode(in, out, preMsg, postMsg);
            if (in != null) {
                in.close();
            }
            out.flush();
            out.close();
            // response
            ByteArrayOutputStream respOut = new ByteArrayOutputStream();
            copy(client.getInputStream(), respOut);
            String response = respOut.toString("UTF-8");
            String ret;
            if (client.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ret = new JSONObject(response).getJSONArray("results").getJSONObject(0).getJSONArray("alternatives").getJSONObject(0).getString("transcript");
            }
            else {
                Log.e(TAG, client.getResponseMessage());
                ret = client.getResponseMessage();
            }
            client.disconnect();
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void base64encode(InputStream in, OutputStream out, String pre, String post) throws IOException {
        out.write(pre.getBytes());
        Base64OutputStream b64out = new Base64OutputStream(out, Base64.NO_WRAP | Base64.NO_CLOSE);
        copy(in, b64out);
        b64out.flush();
        b64out.close();
        out.write(post.getBytes());
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        int result = bin.read();
        while (result != -1) {
            out.write((byte) result);
            result = bin.read();
        }
    }
}
