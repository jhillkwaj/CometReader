package rss.printlnstuidios.com.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Justin Hill on 2/23/2015.
 *
 * Displays some tutorial screens the first time the user launches the app/
 */
public class Tutorial extends ActionBarActivity implements View.OnClickListener{

    Button button;

    /**
     * Sets each of the elements on the Activity to the elements of the post and establishes the
     * necessary listeners and intents.
     */
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("start Tutorial","start Tutorial");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_tutorial);

        //references to Activity component
        button = (Button) findViewById(R.id.button);
        //Sets of listener for open in browser button
        button.setOnClickListener(this);
    }

    /**
     * Sets up the menu on the Action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Called if an item in the Action bar is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * Launches the AddFeedActivity
     */
    @Override
    public void onClick(View v) {
        Intent addFeed = new Intent(this, AddFeedActivity.class);
        this.startActivity(addFeed);
    }
}
