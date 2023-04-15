package io.spaceapi.community.myhackerspace;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class AboutLayout extends LinearLayout {

    public AboutLayout(Context context) {
        super(context);
    }

    public AboutLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AboutLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AboutLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init() {
        TextView version = findViewById(R.id.about_version_text);
        version.setText(BuildConfig.VERSION_NAME + " (" + Integer.toString(BuildConfig.VERSION_CODE) + ")");
    }

    public static AboutLayout create(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        AboutLayout about = (AboutLayout) layoutInflater.inflate(R.layout.about, null, false);
        about.init();
        return about;
    }
}
