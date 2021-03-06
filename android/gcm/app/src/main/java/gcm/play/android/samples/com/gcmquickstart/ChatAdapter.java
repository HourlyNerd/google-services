package gcm.play.android.samples.com.gcmquickstart;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
//        View vi = convertView;
        View vi = null;
        if (vi == null) {
            vi = inflater.inflate(R.layout.listview_item, null);
        }

        if (!(vi instanceof LinearLayout)) {
            throw new RuntimeException("Hacky hack day! Row root must be a linear layout");
        }
        LinearLayout vg = (LinearLayout) vi;

        ChatMessage message = data.get(position);

        if (message.action != null) {
            // special action time!

        } else {
            // pretty much a normal chat message
            TextView text = (TextView) vg.findViewById(R.id.message_textview);
            // hacky - should be the same (not on hackday tho)
            ImageView lefthandIcon = (ImageView) vg.findViewById(R.id.left_user_icon_imageview);
            ImageView righthandIcon = (ImageView) vg.findViewById(R.id.right_user_icon_imageview);

            text.setText(message.chatMessage);

            String msgSender = message.sender;
            // TODO: should probably be replaced with slightly more robust user icon management
            int iconId = msgSender.equals("jordan") ? R.drawable.jordan_round : R.drawable.mark_round;
            Drawable icon = context.getDrawable(iconId);

            String username = UserManager.getUserName(context);
            MessageType messageType = username.equals(msgSender) ? MessageType.SENT_BY_ME : MessageType.SENT_BY_OTHER;

            if (messageType == MessageType.SENT_BY_ME) {
                // my message - right align, don't show icon, dark grey background w/ white text
                vg.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                righthandIcon.setImageDrawable(icon);
                lefthandIcon.setVisibility(View.INVISIBLE);
                righthandIcon.setVisibility(View.GONE);
                text.setBackground(context.getDrawable(R.drawable.rounded_bg_dark_grey));
                text.setTextColor(context.getColor(R.color.white));
                text.setPadding(getPixelsFromDp(20), getPixelsFromDp(5), getPixelsFromDp(20), getPixelsFromDp(5));
            } else {
                // your message - left align, show icon, light grey background w/ black text
                vg.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                lefthandIcon.setImageDrawable(icon);
                lefthandIcon.setVisibility(View.VISIBLE);
                righthandIcon.setVisibility(View.INVISIBLE);
                text.setBackground(context.getDrawable(R.drawable.rounded_bg_light_grey));
                text.setTextColor(context.getColor(R.color.black));
                text.setPadding(getPixelsFromDp(20), getPixelsFromDp(5), getPixelsFromDp(20), getPixelsFromDp(5));
            }
        }

        return vi;
    }

    private int getPixelsFromDp(int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp*scale + 0.5f);
    }
}
