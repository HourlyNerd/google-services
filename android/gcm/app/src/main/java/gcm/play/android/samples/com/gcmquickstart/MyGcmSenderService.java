package gcm.play.android.samples.com.gcmquickstart;

import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mark on 7/21/16.
 */
public class MyGcmSenderService {
    public static final String API_KEY = "AIzaSyDdgYN9RSyYqCfkPZM7npGTTYnONrXjFbA";

    public static void sendMessage(String message) {
        try {
            // Prepare JSON containing the GCM message content. What to send and where to send.
            JSONObject jGcmData = new JSONObject();
            JSONObject jData = new JSONObject();
            jData.put("message", message);
            jData.put("sender", UserConsts.CURRENT_USER);
            // Where to send GCM message.
            jGcmData.put("to", "/topics/global");
            // What to send in GCM message.
            jGcmData.put("data", jData);

            // Create connection to send GCM Message request.
            URL url = new URL("https://android.googleapis.com/gcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "key=" + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Send GCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jGcmData.toString().getBytes());

            // Read GCM response.
            InputStream inputStream = conn.getInputStream();
            String resp = IOUtils.toString(inputStream);
        } catch (IOException | JSONException e) {
            Log.i("SENDER", "Unable to send GCM message.");
            Log.i("SENDER", "Please ensure that API_KEY has been replaced by the server " +
                    "API key, and that the device's registration token is correct (if specified).");
        }
    }
}
