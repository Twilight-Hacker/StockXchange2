package com.galadar.example.stockxchange;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/*
News are for reporting Scams and scandals for the players. It is similar to MessagesActivity and MeetingActivity.
 */

public class NewsActivity extends Activity {

    boolean playSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        setTitle(getString(R.string.title_activity_news));

        String[] news = getIntent().getExtras().getStringArray("NewsArray");

        assert news!=null;

        TextView TitleView = (TextView)findViewById(R.id.NewsTitle);
        TitleView.setText(news[0]);

        TextView BodyView = (TextView)findViewById(R.id.NewsBody);
        BodyView.setText(news[1]);

    }

    public void ExitNews(View v){
        NewsActivity.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nonmain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.menu_sound:
                playSound = !playSound;
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("SoundAltered").putExtra("playSound", playSound));
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                NewsActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
