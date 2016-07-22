package gcm.play.android.samples.com.gcmquickstart;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by mark on 7/21/16.
 */
public class BidnessNeedActivity extends AppCompatActivity {

    private EditText businessNeed;
    private Button submitButton;
    private LinearLayout footerBar;
    private LinearLayout businessLayout;
    private int height = 0;
    //TODO: HACK
    private int deviceHeight = 1500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_need);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ct_logo);

        int orange = ResourcesCompat.getColor(getResources(), R.color.orange, null);
        businessNeed = (EditText)findViewById(R.id.need_edit_view);
        businessNeed.getBackground().setColorFilter(orange, PorterDuff.Mode.SRC_IN);
        submitButton = (Button)findViewById(R.id.submit_button);
        footerBar = (LinearLayout)findViewById(R.id.footer_bar);
        businessLayout = (LinearLayout)findViewById(R.id.business_layout);
        submitButton.setText("submit");
        Log.i("CHATALANT", "Bidness activity built. Takin' care of bidness.");

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentMsg = businessNeed.getText().toString();
                new AsyncGcmSender().execute(currentMsg, UserManager.getUserName(BidnessNeedActivity.this));
                startActivity(new Intent(BidnessNeedActivity.this, WaitingForChatActivity.class));
            }
        });

        businessLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(height != 0 && height != businessLayout.getHeight() && height > businessLayout.getHeight()/2){
                    footerBar.setVisibility(View.GONE);
                } else if(footerBar.getVisibility() == View.GONE && businessLayout.getHeight() >= deviceHeight ) {
                    footerBar.setVisibility(View.VISIBLE);
                }
                height = businessLayout.getHeight();
            }
        });

    }
}
