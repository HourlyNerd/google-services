/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gcm.play.android.samples.com.gcmquickstart;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {

    public static final String NEW_MESSAGE_BROADCAST = "NEW_MSG_BROADCAST";

    // ohhh god the hackery - move this to its own class to encapsulate this shit
    public static final String SHARED_PREFS_MSGS = "chatMessages";
    public static final String SHARED_PREFS_SENDERS = "chatParticipants";
    public static final String SHARED_PREFS_ACTIONS = "chatActions";

    private static final String TAG = "CATALANT";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String sender = data.getString("sender");
        String action = data.getString("action");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Real (Sender) From: " + sender);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Action: " + action);

        String thisUser = UserManager.getUserName(this);

        if (thisUser.equals(sender)) {
            Log.d(TAG, "Current user " + thisUser + " received message from self... ignoring.");
        } else {
            Log.d(TAG, "Current user " + thisUser + " got message from " + sender + ", processing it.");
        }

        if(null == sender || // for backwards compatibility, could remove sometime
                !sender.equals(thisUser)) /* ignore messages we sent to ourselves via our awesome broadcast architecture */ {

            if (from.startsWith("/topics/")) {
                // message received from some topic.
            } else {
                // normal downstream message.
            }

            // add message to local persistent storage
            addMessageToStorage(this, message, sender, action);

            // let any running apps know that this data has changed
            Intent broadcast = new Intent();
            broadcast.setAction(NEW_MESSAGE_BROADCAST);
            sendBroadcast(broadcast);

            // [START_EXCLUDE]
            /**
             * Production applications would usually process the message here.
             * Eg: - Syncing with server.
             *     - Store message in local database.
             *     - Update UI.
             *
             * In some cases it may be useful to show a notification indicating to the user
             * that a message was received.
             */

            sendNotification(message);
        }
    }
    // [END receive_message]

    // dear god this shouldn't be static or live here
    public static void addMessageToStorage(Context context, String msg, String from, @Nullable String action) {
        // Soooooo we should really store this in a local DB but this is just hack day bitches!!
        // you're lucky we're even persisting shit at all!
        // anyway this is really hacky.
        SharedPreferences chatMessages = context.getSharedPreferences(SHARED_PREFS_MSGS, Context.MODE_PRIVATE);
        SharedPreferences chatParticipants = context.getSharedPreferences(SHARED_PREFS_SENDERS, Context.MODE_PRIVATE);

        int numChatMessages = chatMessages.getAll().size();
        int numChatParticipants = chatParticipants.getAll().size();

        if (numChatMessages != numChatParticipants) {
            /// ohhhhhhh shit
            throw new RuntimeException("ooohhh shit this hacky sharedprefs thing broke");
        }

        // keys are sequential ints
        chatMessages.edit().putString(Integer.toString(numChatMessages), msg).apply();
        chatParticipants.edit().putString(Integer.toString(numChatParticipants), from).apply();
        if (action != null) {
            // maybe we don't populate all of these? only the index values that are not null
            SharedPreferences chatActions = context.getSharedPreferences(SHARED_PREFS_ACTIONS, Context.MODE_PRIVATE);
            chatActions.edit().putString(Integer.toString(numChatMessages), action).apply();
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
