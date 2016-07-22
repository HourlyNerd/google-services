package gcm.play.android.samples.com.gcmquickstart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by jordanwinch on 7/21/16.
 */
public class ChatActivity extends AppCompatActivity {
    private EditText currentMessage;
    private ImageButton sendButton;
    private ListView messagesListView;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadUnshownMessagesAndScroll();
        }
    };

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    private BaseAdapter adapter;
    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ct_logo);
        }
        int orange = ResourcesCompat.getColor(getResources(), R.color.orange, null);
        Drawable sendIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_send_white_48dp, null);
        sendIcon.setColorFilter(orange, PorterDuff.Mode.SRC_ATOP);
        currentMessage  = (EditText)findViewById(R.id.current_message_edittext);
        currentMessage.getBackground().setColorFilter(orange, PorterDuff.Mode.SRC_IN);
        currentMessage.setHint("Type to chat...");
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
        new AsyncGcmSender().execute(currentMsg, user);

        displayAndSaveCurrentUserMessage(currentMsg);
        adapter.notifyDataSetChanged();
        scrollToBottomOfChat();
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

        Map<String, ?> allMsgs = sharedPrefsMsgs.getAll();
        Map<String, ?> allSenders = sharedPrefsSenders.getAll();

        for (int i = 0; i < allMsgs.size(); i++) {
            String index = Integer.valueOf(i).toString();
            String sender = (String)allSenders.get(index);
            String msg = (String)allMsgs.get(index);

            Log.i("CATALANT", "loading message #" + index + "...");
            displayMessage(msg, sender);
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

            displayMessage(message, sender);
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
    private void displayAndSaveCurrentUserMessage(String msg) {
        String username = UserManager.getUserName(this);
        MyGcmListenerService.addMessageToStorage(this, msg, username);
        updateReadMessageCount();
        displayMessage(msg, MessageType.SENT_BY_ME);
    }

    private void displayMessage(String msg, String sender) {
        String username = UserManager.getUserName(this);
        if (sender != null && sender.equals(username)) {
            displayMessage(msg, MessageType.SENT_BY_ME);
        } else if (sender != null) {
            displayMessage(msg, MessageType.SENT_BY_OTHER);
        } else {
            // umm warn or something?
            displayMessage(msg, MessageType.SENT_BY_OTHER);
        }
    }

    private void displayMessage(String msg, MessageType messageType) {
        if (msg == null || msg.length() < 1 || messageType == null) { return; }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.messageType = messageType;
        chatMessage.chatMessage = msg;
        chatMessages.add(chatMessage);
    }

    private void scrollToBottomOfChat() {
        messagesListView.smoothScrollToPosition(adapter.getCount()-1);
    }
}