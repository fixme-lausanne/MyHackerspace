package io.spaceapi.community.myhackerspace;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        Button openRepoButton = findViewById(R.id.about_open_repo_button);
        openRepoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView repository = (TextView)findViewById(R.id.about_repository);
                String url = (String) repository.getText();
                openWebPage(url);
            }
        });
        TextView version = findViewById(R.id.about_version_text);
        version.setText(BuildConfig.VERSION_NAME + " (" + Integer.toString(BuildConfig.VERSION_CODE) + ")");
    }

    public static AboutLayout create(Context context) {
        // use xml layout, see https://stackoverflow.com/a/13889257
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        AboutLayout about = (AboutLayout) layoutInflater.inflate(R.layout.about, null, false);
        about.init();
        return about;
    }

    public void openWebPage(String url) {
        // from https://stackoverflow.com/a/43981160
        Uri webpage = Uri.parse(url);

        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(intent);
        }else{
//Page not found
        }
    }

}
