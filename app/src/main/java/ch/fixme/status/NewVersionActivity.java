package ch.fixme.status;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class NewVersionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_version);

        // Close button
        final ImageView closeButton = findViewById(R.id.closeButton);
        closeButton.setImageResource(R.drawable.ic_baseline_close_24);
        closeButton.setOnClickListener(buttonView -> finish());

        // Logos
        final ImageView oldLogo = findViewById(R.id.oldLogo);
        oldLogo.setImageResource(R.drawable.myhs);
        final ImageView arrow = findViewById(R.id.arrow);
        arrow.setImageResource(R.drawable.ic_baseline_arrow_forward_24);
        final ImageView newLogo = findViewById(R.id.newLogo);
        newLogo.setImageResource(R.drawable.newlogo);
    }
}