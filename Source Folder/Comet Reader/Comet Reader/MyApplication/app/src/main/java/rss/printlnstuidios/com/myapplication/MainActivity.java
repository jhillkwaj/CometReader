package rss.printlnstuidios.com.myapplication;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Justin Hill on 12/2/2014.
 *
 * Represents the users main list of posts where then can click on one to view it and select to view
 * them by feed or my read/unread/stared state
 */
public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    //Elements in the view that the user interacts with
    private ListView contentListView;
    private static Spinner activeFeeds;
    private Spinner viewSelect;
    private ContentListAdapter contentAdapter;
    //List of all the users feeds and adapter to display them
    private static List<String> feedsList =  new ArrayList<String>();
    ArrayAdapter<String> adapter;
    //What state of posts the user wants to view
    public int whatPostsToView = 0;

    private static Post p;
    //Auto refresh timer
    private static Timer timer;
    private int timerSpeed = 100000;
    //Reference to mainActivity to use in different threads.
    MainActivity mainActivity = this;

    /**
     * Sets up the main view and makes the necessary reference and listeners to the interactive UI
     * elements in the view.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //make a reference to the content list and set a click listener to this class
        contentListView = (ListView) findViewById(R.id.main_feed);
        contentListView.setOnItemClickListener(this);

        //make a reference to the spinner which allows the user to select what feeds they want to view
        activeFeeds = (Spinner) findViewById(R.id.select_feeds);
        activeFeeds.setOnItemSelectedListener(this);

        //make a reference to the spinner which allows the user to select what posts they want to view
        viewSelect = (Spinner) findViewById(R.id.view_select);
        viewSelect.setOnItemSelectedListener(this);

        //Add two options two the Spinner of what feeds to view
        if(feedsList.size()==0) {
            feedsList.add("All Feeds");
            feedsList.add("Add New");
        }
        //puts feeds list into the active feeds spinner
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, feedsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activeFeeds.setAdapter(adapter);


        contentAdapter = new ContentListAdapter(this, getLayoutInflater());
        contentListView.setAdapter(contentAdapter);

        //Load Feeds
        GetContent.findContent(this);
        //start the auto refresh timer
        timer = new Timer();
        timer.schedule(new timeUpdate(),0, timerSpeed);

    }



    /**
     * Updates the view when new posts are added.
     */
    public synchronized void foundContent()
    {
        ArrayList<Post> postsFromOpenFeeds = GetContent.postsFromOpenFeeds;
        //remove duplicates
        for(int i = 0; i < postsFromOpenFeeds.size(); i++) {
            for (int j = i + 1; j < postsFromOpenFeeds.size(); j++) {
                if (postsFromOpenFeeds.get(j).equals(postsFromOpenFeeds.get(i))) {
                    postsFromOpenFeeds.remove(j);
                    j--;
                }
            }
        }
        GetContent.postsFromOpenFeeds = postsFromOpenFeeds;
        //Sorts the posts chronologically
        Collections.sort(postsFromOpenFeeds);
        GetContent.postsFromOpenFeeds = postsFromOpenFeeds;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contentAdapter.notifyDataSetChanged();
                contentAdapter.update();
            }
        });


        //update the list of feeds
        if(GetContent.allFeeds.size()>=feedsList.size()-1)
        {
            for(Feed f : GetContent.allFeeds)
            {
                 if(f.allPosts!=null&&f.allPosts.size()>0&&!feedsList.contains(f.allPosts.get(0).source))
                 {
                     feedsList.add(feedsList.size()-1,f.allPosts.get(0).source);
                     adapter.notifyDataSetChanged();
                 }
            }
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Adds items to the action bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_refresh)
        {
            GetContent.findContent();
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Open the View Post Activity when the user clicks on a post
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        p = GetContent.postsFromOpenFeeds.get(position);
        GetContent.readPost(position);
        Intent viewPost = new Intent(this, ViewPostActivity.class);
        viewPost.putExtra("Post", GetContent.getContent(position));
        this.startActivity(viewPost);
    }

    /**
     * Called when an item is selected in on of the two spinners that allow the user to selct
     * what posts tol view
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(viewSelect.getId()==parent.getId()) //change in view star/read/unread
        {
            whatPostsToView = position;
            GetContent.foundNewContent();
        }
        else //change in active feeds
        {
            GetContent.setActive(feedsList.get(position));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            GetContent.save(mainActivity);
        }
    }

    /**
     * Overrides the noting selected method so nothing happens when nothing is selected in a spinner
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * Toggles if a posts is starred
     */
    public static void starPost()
    {
        p.star = !p.star;
    }

    /**
     * Sets the left spinner to view posts from All Feeds. Called when the user adds a new feed.
     */
    public static void resetSpinner()
    {
        activeFeeds.setSelection(0);
    }

    /**
     * Periodically refresh the page
     */
    class timeUpdate extends TimerTask {

        @Override
        public void run() {
            GetContent.findContent();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            GetContent.save(mainActivity);
        }
    };
}
