package com.example.android.asymmetricfingerprintdialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabActivity extends AppCompatActivity {

    private LinearLayout newsContainer;
    private LinearLayout favoriteContainer;
    private LinearLayout allContainer;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    newsContainer.setVisibility(LinearLayout.VISIBLE);
                    favoriteContainer.setVisibility(LinearLayout.GONE);
                    allContainer.setVisibility(LinearLayout.GONE);
                    return true;
                case R.id.navigation_dashboard:
                    newsContainer.setVisibility(LinearLayout.GONE);
                    favoriteContainer.setVisibility(LinearLayout.VISIBLE);
                    allContainer.setVisibility(LinearLayout.GONE);
                    return true;
                case R.id.navigation_notifications:
                    newsContainer.setVisibility(LinearLayout.GONE);
                    favoriteContainer.setVisibility(LinearLayout.GONE);
                    allContainer.setVisibility(LinearLayout.VISIBLE);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        newsContainer = (LinearLayout) findViewById(R.id.newsContainer);
        favoriteContainer = (LinearLayout) findViewById(R.id.favoriteContainer);
        allContainer = (LinearLayout) findViewById(R.id.allContainer);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
