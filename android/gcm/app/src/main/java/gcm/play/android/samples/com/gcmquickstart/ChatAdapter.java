package gcm.play.android.samples.com.gcmquickstart;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordanwinch on 7/21/16.
 */
public class ChatAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Context context;
//    String[] data;
    ArrayList<ChatMessage> data;

    public ChatAdapter(Context context, ArrayList<ChatMessage> data) {
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
        this.context = context;
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

        if (!(vi instanceof LinearLayout)) {
            throw new RuntimeException("Hacky hack day! Row root must be a linear layout");
        }
        LinearLayout vg = (LinearLayout) vi;

        TextView text = (TextView) vg.findViewById(R.id.message_textview);
        // hacky - should be the same (not on hackday tho)
        ImageView lefthandIcon = (ImageView) vg.findViewById(R.id.left_user_icon_imageview);
        ImageView righthandIcon = (ImageView) vg.findViewById(R.id.right_user_icon_imageview);

        ChatMessage message = data.get(position);
        text.setText(message.chatMessage);
        if (message.messageType == MessageType.SENT_BY_ME) {
            // my message - left align
            vg.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);

            lefthandIcon.setImageDrawable(context.getDrawable(R.drawable.jordan_round));

            lefthandIcon.setVisibility(View.VISIBLE);
            righthandIcon.setVisibility(View.GONE);
        } else {
            vg.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);

            righthandIcon.setImageDrawable(context.getDrawable(R.drawable.mark_round));

            lefthandIcon.setVisibility(View.GONE);
            righthandIcon.setVisibility(View.VISIBLE);
        }
        return vi;
    }
}
