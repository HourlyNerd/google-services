package gcm.play.android.samples.com.gcmquickstart;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordanwinch on 7/21/16.
 */
public class ChatAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
//    String[] data;
    ArrayList<ChatMessage> data;

    public ChatAdapter(Context context, ArrayList<ChatMessage> data) {
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.listview_item, null);
        TextView text = (TextView) vi.findViewById(R.id.message_text);

        ChatMessage message = data.get(position);
        text.setText(message.chatMessage);
        if (message.messageType == MessageType.SENT_BY_ME) {
            text.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        } else {
            text.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        }
        return vi;
    }
}
