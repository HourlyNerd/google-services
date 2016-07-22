package gcm.play.android.samples.com.gcmquickstart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by jordanwinch on 7/21/16.
 */
public class ChatActivity extends AppCompatActivity {

    public static final String ACTION_PROPOSE_PROJECT = "PROPOSE_PROJECT";
    public static final String ACTION_ACCEPT_PROJECT = "ACCEPT_PROJECT";

    private EditText currentMessage;
    private ImageButton sendButton;
    private ListView messagesListView;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadUnshownMessagesAndScroll();
        }
    };

    private BaseAdapter adapter;
    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ActionBar actionBar = getSupportActionBar();
        // begin hackery
        // if you're not you then you're me
        String username = UserManager.getUserName(this);
        String me = username.equals("jordan") ? "Jordan Winch" : "Mark Roper";
        String you = username.equals("jordan") ? "Mark Roper" : "Jordan Winch";
        String yourType = username.equals("jordan") ? "Business" : "Expert";

        String titleString = "Chat with " + you;
        // end hackery
        if (actionBar != null) {
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ct_logo);
            actionBar.setTitle("   " + titleString);
        }
        int orange = ResourcesCompat.getColor(getResources(), R.color.orange, null);
        Drawable sendIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_send_white_48dp, null);
        sendIcon.setColorFilter(orange, PorterDuff.Mode.SRC_ATOP);
        currentMessage  = (EditText)findViewById(R.id.current_message_edittext);
        currentMessage.getBackground().setColorFilter(orange, PorterDuff.Mode.SRC_IN);
        currentMessage.setHint("Send message to " + yourType);
        adapter = new ChatAdapter(this, chatMessages);
        messagesListView = (ListView) findViewById(R.id.chat_listview);
        messagesListView.setAdapter(adapter);
        messagesListView.setDivider(null);
        sendButton = (ImageButton) findViewById(R.id.send_message_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              onSendClicked();
                                          }
                                      });

        Button startProjectButton = (Button) findViewById(R.id.start_project_button);
        startProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = "I'd like to work with you.";
                String user = getUser();
                new AsyncGcmSender().execute(msg, user, ACTION_PROPOSE_PROJECT);
            }
        });
        startProjectButton.setText("Start a project with this " + yourType);

        loadMessages();
    }

    private void onSendClicked() {
        String currentMsg = currentMessage.getText().toString();
        currentMessage.setText("");

        // TODO should acknowledge the difference here between locally-added notifications
        // and ones that have definitely reached the server and come back...
        // we could show the message in grey until we receive (the same) message back
        // as a push notification, in which case it turns black
        String user = getUser();
        new AsyncGcmSender().execute(currentMsg, user, null);

        displayAndSaveCurrentUserMessage(currentMsg);
    }

    private String getUser() {
        return UserManager.getUserName(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyGcmListenerService.NEW_MESSAGE_BROADCAST);
        registerReceiver(receiver, filter);

        adapter.notifyDataSetChanged();
        scrollToBottomOfChat();
    }

    int highestIdOfMessageShown = 0;

    private void loadMessages() {
        SharedPreferences sharedPrefsMsgs = getSharedPreferences(MyGcmListenerService.SHARED_PREFS_MSGS, Context.MODE_PRIVATE);
        SharedPreferences sharedPrefsSenders = getSharedPreferences(MyGcmListenerService.SHARED_PREFS_SENDERS, Context.MODE_PRIVATE);
        SharedPreferences sharedPrefsActions = getSharedPreferences(MyGcmListenerService.SHARED_PREFS_ACTIONS, Context.MODE_PRIVATE);

        Map<String, ?> allMsgs = sharedPrefsMsgs.getAll();
        Map<String, ?> allSenders = sharedPrefsSenders.getAll();

        for (int i = 0; i < allMsgs.size(); i++) {
            String index = Integer.valueOf(i).toString();
            String msg = sharedPrefsMsgs.getString(index, null);
            String sender = sharedPrefsSenders.getString(index, null);
            String action = sharedPrefsActions.getString(index, null);

            Log.i("CATALANT", "loading message #" + index + "..." + "(msg:" + msg + ", sender:" + sender + ", action:" + action + ")");
            displayMessage(msg, sender, action);
        }

        updateReadMessageCount();
    }

    private void updateReadMessageCount() {
        int totalMsgs = getSharedPreferences(MyGcmListenerService.SHARED_PREFS_MSGS, Context.MODE_PRIVATE).getAll().size() - 1;
        if (totalMsgs > highestIdOfMessageShown) {
            highestIdOfMessageShown = totalMsgs;
        }
    }

    private void loadUnshownMessagesAndScroll() {
        SharedPreferences sharedPrefsMsgs = getSharedPreferences(MyGcmListenerService.SHARED_PREFS_MSGS, Context.MODE_PRIVATE);
        SharedPreferences sharedPrefsSenders = getSharedPreferences(MyGcmListenerService.SHARED_PREFS_SENDERS, Context.MODE_PRIVATE);
        SharedPreferences sharedPrefsActions = getSharedPreferences(MyGcmListenerService.SHARED_PREFS_ACTIONS, Context.MODE_PRIVATE);

        int highestExistingMessageId = sharedPrefsMsgs.getAll().size() - 1;
        Log.i("CATALANT", "I see a total of " + highestExistingMessageId + " messages in storage" );
        Log.i("CATALANT", "highestExistingMessageId seen: " + highestIdOfMessageShown );
        int numOfNewMessages = highestExistingMessageId - highestIdOfMessageShown;

        // if (at first) there were 5 messages, highestId is 4
        // then a new message arrives...
        // now there are 6 total messages, and we need to update if highestIdOfMessageShown is not 5 (or, total messages - 1)
        if (numOfNewMessages > 0) {
            Log.i("CATALANT", "we have " + numOfNewMessages + " new/unread messages we must render.");
        } else {
            Log.i("CATALANT", "no new messages!");
        }

        for (int i = (highestExistingMessageId); i <= highestExistingMessageId; i++) {
            Log.i("CATALANT", "rendering message " + i);
            String message = sharedPrefsMsgs.getString(Integer.toString(i), null);
            String sender = sharedPrefsSenders.getString(Integer.toString(i), null);
            String action = sharedPrefsActions.getString(Integer.toString(i), null);

            displayMessage(message, sender, action);
        }
        adapter.notifyDataSetChanged();
        updateReadMessageCount();
        scrollToBottomOfChat();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    // save the message, then add it to the local view
    // this method does call notifyDataSetChanged
    private void displayAndSaveCurrentUserMessage(String msg) {
        String username = UserManager.getUserName(this);
        MyGcmListenerService.addMessageToStorage(this, msg, username, null);

        displayMessage(msg, username, null);

        adapter.notifyDataSetChanged();
        updateReadMessageCount();
        scrollToBottomOfChat();

    }

    // displays one message to the chat box. if the sender is the current user,
    // it will receive special treatment
    // this method does NOT call notifyDataSetChanged, and you should do this yourself
    // after calling it. This way you can load a bunch of stored messages and only call it once
    private void displayMessage(String msg, String sender, @Nullable String action) {
        if (msg == null || msg.length() < 1) { return; }

        ChatMessage chatMessage = new ChatMessage();

        chatMessage.chatMessage = msg;
        chatMessage.sender = sender;
        chatMessage.action = action;
        chatMessages.add(chatMessage);
    }

    private void scrollToBottomOfChat() {
        messagesListView.smoothScrollToPosition(adapter.getCount()-1);
    }
}