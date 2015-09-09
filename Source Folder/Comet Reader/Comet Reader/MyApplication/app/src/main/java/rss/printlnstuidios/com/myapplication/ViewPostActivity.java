package rss.printlnstuidios.com.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
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
 * Created by Justin Hill on 12/29/2014.
 *
 * Activity used for viewing a specific post and sharing it, saving it, and viewing it in a web browser.
 */
public class ViewPostActivity extends ActionBarActivity implements View.OnClickListener {

    //Elements of the Activity UI
    Button button;
    ImageView image;
    TextView imageText;
    TextView postText;
    String[] post;
    String postString = "";
    //The URL of the post. Used for opening it and shairing it.
    String postURL = "The Website";
    //if the post it starred
    boolean saved = false;

    LinearLayout layout;
    boolean addButton = false;
    //used for shareing the post
    private android.support.v7.widget.ShareActionProvider mShareActionProvider;

    /**
     * Sets each of the elements on the Activity to the elements of the post and establishes the
     * necessary listeners and intents.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_post);

        //references to Activity components
        button = (Button) findViewById(R.id.button);
        image = (ImageView) findViewById(R.id.top_image);
        imageText = (TextView) findViewById(R.id.imageViewText);
        postText = (TextView) findViewById(R.id.postText);
        layout = (LinearLayout) findViewById(R.id.relative_layout);
        //Sets of listener for open in browser button
        button.setOnClickListener(this);

        post = this.getIntent().getStringArrayExtra("Post");


        // Get the text and image url data for the post

        // See if there in an image that goes along with the post
        if (post[2]!=null&&!post[2].equals("")) {
            try {
                Picasso.with(this).load(post[2]).placeholder(R.drawable.ic_launcher).into(image);
            } catch (Exception e) {
                e.printStackTrace();
                image.setImageResource(R.drawable.ic_launcher);
            }
        }
        else
        {
            // If there is no image then use a default image
            image.setImageResource(R.drawable.ic_launcher);
        }
        Log.d("Text",post[6]);
        imageText.setText(post[0]);
        postURL = post[4];

        String text = post[6];
        postString = post[6];
        saved = post[7].equals("star");

        try {

            while (text.contains("<img") || text.contains("< img")) { //only runs if there is an image in the text
                addButton = true;
                layout.removeView(button);
                //write the text before the image
                if (text.contains("<img")) {
                    postText.setText(Html.fromHtml(text.substring(0, text.indexOf("<img"))));
                    text = text.substring(text.indexOf("<img"));
                } else if (text.contains("< img")) {
                    postText.setText(Html.fromHtml(text.substring(0, text.indexOf("< img"))));
                    text = text.substring(text.indexOf("< img"));
                }

                //draw the image
                String imageUrl = text.substring(text.indexOf("src=\"") + 5);
                imageUrl = imageUrl.substring(0, imageUrl.indexOf("\""));

                int lastId = postText.getId();
                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                imageView.setLayoutParams(params);

                //load the image
                try {
                    Picasso.with(this).load(imageUrl).placeholder(R.drawable.ic_launcher).into(imageView);
                } catch (Exception e) {
                    e.printStackTrace();
                    imageView.setImageResource(R.drawable.ic_launcher);
                }
                //add the image
                layout.addView(imageView);
                lastId = imageView.getId();


                //set up a new textView
                text = text.substring(text.indexOf(">") +1);

                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                postText = new TextView(this);
                postText.setLayoutParams(textParams);
                postText.setTextColor(Color.BLACK);
                layout.addView(postText);
            }
        }catch(Exception e)
        {  }
        if(text.length()>0) {
            postText.setText(Html.fromHtml(text));
        }

        if(addButton) {
            layout.addView(button);
        }

    }

    /**
     * Sets up the menu on the Action bar for sharing and starring the post
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, imageText.getText());
        String post = ""+Html.fromHtml(postString)+"\n\n"+"Read More At " + postURL;
        i.putExtra(Intent.EXTRA_TEXT, post);
        mShareActionProvider.setShareIntent(i);
        if(saved)
        { menu.findItem(R.id.star_post).setIcon(R.drawable.btn_star_on_normal_holo_light);}
        return true;
    }

    /**
     * Called if one of the items is selected from the options menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.menu_item_share) //This is handled by the share intent that was set up earlier
        {}
        else if(id == R.id.star_post)
        {
            //Toggle the star from blue to gray
            if(saved)
                item.setIcon(R.drawable.btn_rating_star_off_disabled_holo_dark);
            else
                item.setIcon(R.drawable.btn_star_on_normal_holo_light);
            //Record that the post has been starred
            saved=!saved;
            MainActivity.starPost();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Open the post in a web browser if the user click open in browser
     */
    @Override
    public void onClick(View v) {
        try{
            Intent openBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(post[4]));
            startActivity(openBrowser);
        }
        catch (Exception e)
        {
            //Display an error if something goes wrong
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Couldn't Open URL").setMessage("Sorry but I couldn't open this post in a web browser. The URL may be invalid or you man not have an internet connection.").setPositiveButton("OK", null).show();
        }
    }
}
