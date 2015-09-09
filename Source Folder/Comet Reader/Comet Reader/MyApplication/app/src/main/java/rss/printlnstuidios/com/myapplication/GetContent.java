package rss.printlnstuidios.com.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 * Created by Justin Hill on 12/24/2014.
 *
 * Manages the content from all of the feeds
 */
public class GetContent {

    //lists of all of the feeds
    public static ArrayList<Feed> allFeeds = new ArrayList<Feed>();
    private static ArrayList<Feed> openFeeds = new ArrayList<Feed>();
    public static ArrayList<Post> postsFromOpenFeeds = new ArrayList<Post>();

    //reference to the main activity for multithreading
    private static MainActivity mainActivity;
    //Used to pares Xml and Html from the feeds
    private static XmlPullParserFactory xmlFactoryObject;

    /**
     * Gets the content from saved feeds and updates all of the feeds
     */
    public static void findContent(MainActivity m)
    {
        mainActivity = m;
        //find old content
        if(allFeeds.size()==0) {
            load();
        }

        //find new content
        for(Feed f : allFeeds)
        {
            f.update(mainActivity, openFeeds.contains(f));
        }
    }

    /**
     * Returns an array containing the title, text, imageURL, source and other information about a post
     */
    public static String[] getContent(int spot)
    {
        try {
            String[] content = new String[8];


            content[0] = postsFromOpenFeeds.get(spot).title;
            content[1] = postsFromOpenFeeds.get(spot).description;
            content[2] = postsFromOpenFeeds.get(spot).image;
            content[3] = postsFromOpenFeeds.get(spot).source;
            content[4] = postsFromOpenFeeds.get(spot).url;
            content[5] = postsFromOpenFeeds.get(spot).read ? "read" : "unread";
            content[6] = postsFromOpenFeeds.get(spot).text;
            content[7] = postsFromOpenFeeds.get(spot).star ? "star" : "no star";


            return content;
        }
        catch (Exception e)
        {
            String[] content = new String[8];
            content[0] = "";
            content[1] = "";
            content[2] = "";
            content[3] = "";
            content[4] = "";
            content[5] = "";
            content[6] = "";
            content[7] = "";
            return content;
        }
    }

    /**
     * Calls findContent with a reference to the mainActivity. For use in other threads.
     */
    public static void findContent() {
        findContent(mainActivity);
    }

    /**
     * Returns the number of posts from all of the open feeds
     */
    public static int getLength()
    {
        return postsFromOpenFeeds.size();
    }

    /**
     * Adds new posts to the users feed when new posts are found
     */
    public static void foundNewContent()
    {
        postsFromOpenFeeds.clear();
                try {

        for(Feed f : openFeeds)
        {
            //User is viewing all posts
            if(mainActivity.whatPostsToView==0)
            {
                for(Post p : f.allPosts) {
                    postsFromOpenFeeds.add(p);
                }
            }
            //User is viewing stared posts
            else if(mainActivity.whatPostsToView==1)
            {
                for(Post p : f.allPosts) {
                    if(p.star)
                        postsFromOpenFeeds.add(p);
                }
            }
            //User is viewing only unread posts
            else if(mainActivity.whatPostsToView==2)
            {
                for(Post p : f.unreadPosts) {
                    postsFromOpenFeeds.add(p);
                }
            }
            //User is viewing all posts
            else
            {
                for(Post p : f.allPosts) {
                    if(!f.unreadPosts.contains(p)) {
                        postsFromOpenFeeds.add(p);
                    }
                }
            }

        }
        //Update the content in the mainActivity
        mainActivity.foundContent();
                } catch (Exception e) {
                }
    }

    /**
     * Marks a post as read and removes it from the list of unread posts.
     */
    public static void readPost(int position)
    {
        Post p = postsFromOpenFeeds.get(position);
        p.read = true;
        p.feed.unreadPosts.remove(p);
    }

    /**
     * Saves all of the users feeds locally
     */
    private static void saveFeeds(MainActivity m)
    {
        String data = "";

        //save each feed
        for (Feed f : allFeeds)
        {
            if(f.getFeedURL()!=null&&!f.getFeedURL().equals("")&&!f.getFeedURL().equals(" "))
            data+= "qzpsfaaaFEEDqzpsfaaa" + f.getSaveData();
        }

        try {
            //write the save data to a local file
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(m.openFileOutput("feed.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the saveFeed on a different thread in the background
     */
    public static void save(MainActivity m)
    {
        mainActivity = m;
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    saveFeeds(mainActivity);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Error", "" + e);
                }
            }
        });
        thread.start();
    }

    /**
     * Runs the save method with a reference to the main activity. Used when it is called from a different thread.
     */
    public static void save()
    { save(mainActivity);}

    /**
     * Loads posts and feeds locally from a previous run of the app
     */
    private static void load()
    {
        //Runs on a different thread so it can run in the background
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    //try to find the input file
                    String data = "";
                    InputStream inputStream = mainActivity.openFileInput("feed.txt");
                    //see if there is an imput file with that name
                    if ( inputStream != null ) {
                        //read in a string from it
                        InputStreamReader in = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(in);
                        String r = "";
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((r = bufferedReader.readLine()) != null) {
                            stringBuilder.append(r);
                        }

                        inputStream.close();
                        data = stringBuilder.toString();
                        Log.d("load data",data);
                        //break that string up to represent the individual feeds
                        String[] feeds = data.split("qzpsfaaaFEEDqzpsfaaa");
                        for(String feedData : feeds)
                        {
                            Log.d("newFeed","new feed");
                            //make a feed to represent each feed that we loaded
                            Feed feed = new Feed(feedData, false);
                            //add that feed to the list of all feeds
                            allFeeds.add(feed);
                            openFeeds.add(feed);
                        }
                        //Updates the users feed view if new posts were found
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GetContent.foundNewContent();
                            }
                        });
                    }
                } catch (Exception e) { //if no save start the tutorial
                    Intent tutorial = new Intent(mainActivity, Tutorial.class);
                    mainActivity.startActivity(tutorial);
                    MainActivity.resetSpinner();
                }
            }
        });
        //Runs the new thread
        thread.start();
    }

    /**
     * Switches between which feed to fiew and starts the AddFeed activity
     */
    public static void setActive(String feedName)
    {
        openFeeds.clear();
        if(feedName.equals("All Feeds")) //show posts from all feeds
        {
            for(Feed f : allFeeds)
            {
                openFeeds.add(f);
            }
        }
        else if(feedName.equals("Add New"))
        {
            //Start the add feed activity if the user wants to add a new feed
            Intent addFeed = new Intent(mainActivity, AddFeedActivity.class);
            mainActivity.startActivity(addFeed);
            MainActivity.resetSpinner();
        }
        else //only show one feed
        {
            for(Feed f : allFeeds)
            {
                if(f.allPosts!=null&&f.allPosts.size()>0&&f.allPosts.get(0).source.equals(feedName))
                openFeeds.add(f);
            }
        }
        //update the content view
        foundNewContent();
    }

}
