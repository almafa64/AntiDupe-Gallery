package com.cyberegylet.antiDupeGallery;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.cyberegylet.antiDupeGallery.backend.Test;

public class Main extends Activity
{
    static {
        System.loadLibrary("backend");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        TextView a = findViewById(R.id.textView);
        a.setText(String.valueOf(Test.test()));
    }
}
