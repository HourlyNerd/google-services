package gcm.play.android.samples.com.gcmquickstart;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jordanwinch on 7/21/16.
 */
public class UserManager {

    public static String getUserName(Context context) {
        if (context == null) { throw new NullPointerException("context cannot be null!"); }
        SharedPreferences userPrefs = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
        return userPrefs.getString("username", null);
    }

    public static void setUserName(Context context, String username) {
        setUserName(context, username, true);
    }

    public static void setUserName(Context context, String username, boolean async) {
        if (context == null) { throw new NullPointerException("context cannot be null!"); }
        SharedPreferences debugUserPrefs = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
        if (async) {
            debugUserPrefs.edit().putString("username", username).apply();
        } else {
            debugUserPrefs.edit().putString("username", username).commit();
        }
    }

}
