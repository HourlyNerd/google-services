package gcm.play.android.samples.com.gcmquickstart;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by jordanwinch on 7/21/16.
 */
public class ChatActivity extends AppCompatActivity {

    private EditText currentMessage;
    private ImageButton sendButton;
    private ListView messagesListView;

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    private BaseAdapter adapter;

//    private ArrayList<String> chatMessages = new ArrayList<>();
    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentMessage  = (EditText)findViewById(R.id.current_message_edittext);
        sendButton = (ImageButton) findViewById(R.id.send_message_button);
        messagesListView = (ListView) findViewById(R.id.chat_listview);

        currentMessage.setHint("type here to chat...");
//        currentMessage.setTextColor(getColor(R.color.white));

        adapter = new ChatAdapter(this, chatMessages);
        messagesListView.setAdapter(adapter);
        messagesListView.setDivider(null);

        Log.i("CHATALANT", "Chat activity built");

        sendButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              String currentMsg = currentMessage.getText().toString();
                                              if (currentMsg.toLowerCase().contains("test")) {
                                                  addCounterpartyMessage(currentMsg);
                                              } else {
                                                  addCurrentUserMessage(currentMsg);
                                              }
                                              currentMessage.setText("");
                                          }
                                      });
    }

    private void addCurrentUserMessage(String msg) {
        addMessage(msg, MessageType.SENT_BY_ME);
    }

    private void addCounterpartyMessage(String msg) {
        addMessage(msg, MessageType.SENT_BY_OTHER);
    }

    private void addMessage(String msg, MessageType messageType) {
        if (msg == null || msg.length() < 1 || messageType == null) { return; }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.messageType = messageType;
        chatMessage.chatMessage = msg;
        chatMessages.add(chatMessage);
        adapter.notifyDataSetChanged();
        messagesListView.smoothScrollToPosition(adapter.getCount()-1);
    }

}