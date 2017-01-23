package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Thibaut on 21/01/2017.
 */

public class MainSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /**
         * Management of menu buttons
         */
        // BACK BUTTON
        // TODO : Update the code so that after clicking the back-button, the Main Activity is displayed as it was left
        final ImageView back = (ImageView) findViewById(R.id.iv_back_settings);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToStartMainActivity = new Intent(MainSettings.this, MainActivity.class);
                startActivity(intentToStartMainActivity);
            }
        });
    }
}
