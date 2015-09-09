package rss.printlnstuidios.com.myapplication;

import android.app.AlertDialog;
import android.os.Looper;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Justin Hill on 12/25/2014.
 *
 * This class represents one of the users feeds. It gets the posts from that feed and stores them.
 */
public class Feed {
    //For storing posts while the app is running
    public ArrayList<Post> allPosts = new ArrayList<>();
    public ArrayList<Post> unreadPosts = new ArrayList<>();
    //For parsing the Html and Xml of the Feed
    private XmlPullParserFactory xmlFactoryObject;
    //The URL of the feed
    private String feedUrl = "";
    //A refrence to the mainActivity for multithreading
    private MainActivity mainActivity;
    //if new posts have been found
    private boolean foundNewPosts = false;

    /**
     * Constructs a feed. isNew tells us if this is a new feed the user is adding or one being loaded from memory.
     */
    public Feed(String data, boolean isNew)
    {
        if(isNew) {
            feedUrl = data;
        }
        else //we are using data that we get from a save file
        {

            String[] postData = data.split("qzpsfaaa");
            feedUrl = postData[0];
            //load each element from memory.
            if (feedUrl.length() > 2) {
                for (int i = 1; i + 9 <= postData.length; i += 9) {
                    //Construct post objects from the data in memory
                    Post post;
                    if (postData[i + 7].equals("true") && postData[i + 8].equals("true")) {
                        post = new Post(postData[i], postData[i + 1], postData[i + 2], postData[i + 3], postData[i + 5], postData[i + 4], this, true, postData[i + 6], true);
                    } else if (postData[i + 7].equals("false") && postData[i + 8].equals("true")) {
                        post = new Post(postData[i], postData[i + 1], postData[i + 2], postData[i + 3], postData[i + 5], postData[i + 4], this, false, postData[i + 6], true);
                        //if the post is unread add it to the list of unread posts in this feed
                        unreadPosts.add(post);
                    } else if (postData[i + 7].equals("true") && postData[i + 8].equals("false")) {
                        post = new Post(postData[i], postData[i + 1], postData[i + 2], postData[i + 3], postData[i + 5], postData[i + 4], this, true, postData[i + 6], false);
                    } else {
                        post = new Post(postData[i], postData[i + 1], postData[i + 2], postData[i + 3], postData[i + 5], postData[i + 4], this, false, postData[i + 6], false);
                        //if the post is unread add it to the list of unread posts in this feed
                        unreadPosts.add(post);
                    }
                    //Add the posts to the list of allPosts in this feed
                    allPosts.add(post);
                }

            } else {
                //If the URL is to short to be valid
                GetContent.allFeeds.remove(this);
            }
        }
    }


    /**
     * Parses the Rss feed into Post objects
     */
    private void getRss(XmlPullParser xmlParser) {
        //Parse the RSS feed
        int event;
        //Variables to store each element of the feed
        String title = "";
        String url = "";
        String image = "";
        String text = null;
        String date = "";
        String description = "";
        String longDescription = "";
        String defaultImage = "";
        String source = "";
        boolean item = false;
        boolean inImage = false;
        try {
            event = xmlParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) { //Until the end of the document
                String name=xmlParser.getName();
                switch (event){ //identify the type of tag
                    case XmlPullParser.START_TAG:
                        if(name.contains("img")) { //if there is an image. Save it.
                            text = xmlParser.getText();
                            if(text.contains("<img")||text.contains("< img"))
                            {
                                image = text;
                                image=image.substring(image.indexOf("src=")+5);
                                image=image.substring(0,image.indexOf("\""));
                            }
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = xmlParser.getText();
                        //Make sure the document does not contain these characters. This would prevent the post from saving.
                        if(text.contains("qzpsfaaa"))
                        { text=text.replace("qzpsfaaa","qzpsfuaaa");}
                        if(text.contains("<img")||text.contains("< img"))
                        {
                            //seperate the image URL from the rest of the text in the tag
                            image = text;
                            image=image.substring(image.indexOf("src=")+5);
                            image=image.substring(0,image.indexOf("\""));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        //if we get to the end of a tag. Save what was in the tag to the proper variable.
                        if(name.equals("title")||name.equals("link"))
                        {
                            //if we have everything we need for the post. Save it
                            if((!title.equals("")&&!url.equals("")))
                            {
                                if(item) {
                                    Post p;
                                    if (image != "") {
                                        p = new Post(title, description, url, date, image, source, this, false, longDescription, false);
                                    } else {
                                        p = new Post(title, description, url, date, defaultImage, source, this, false, longDescription, false);
                                    }

                                    if(!allPosts.contains(p))
                                    {
                                        foundNewPosts = true;
                                        unreadPosts.add(p);
                                        allPosts.add(p);
                                    }

                                }
                                else if(source.equals(""))
                                {
                                    source = title;
                                }

                                title = description = url = image = date = longDescription = "";
                                item = false;
                            }
                        } //Save the post title
                        if(name.equals("title")){
                            title = text;
                        } //save the URL of the post
                        else if(name.equals("link")){
                            url = text;
                        } //save the description of the post
                        else if(name.equals("description")||name.equals("summary")){
                            description = text;
                            while(description.contains("/>"))
                            {
                                description = description.substring(description.indexOf("/>")+2);
                            }
                        } //save the image associated with the post
                        else if(name.equals("image")){
                            inImage = true;
                        } //save the URL of an image in the post.
                        else if((name.equals("url")||name.equals("link"))&&inImage){
                            inImage = false;
                            defaultImage = text;
                        }
                        //save the date of the post. Necessary for chronological ordering.
                        else if(name.equals("pubDate")||name.equals("dc:date")||name.equals("updated")){
                            date = text;
                        } //record if we are inside an item tag
                        else if(name.equals("item")||name.equals("entry")){
                            item=true;
                        } //if a longer description is available. Save it for the post preview.
                        else if(name.equals("content:encoded")||name.equals("content")){
                            longDescription = text;
                        }
                        else{
                        }
                        break;
                }
                event = xmlParser.next();
            }
            //Add the last post to the list of posts if we have all the elements at the end
            if(!title.equals("")&&!url.equals(""))
            {
                Post p;
                if (image != "") {
                    p = new Post(title, description, url, date, image, source, this, false, longDescription, false);
                } else {
                    p = new Post(title, description, url, date, defaultImage, source, this, false, longDescription, false);
                }

                if(!allPosts.contains(p))
                {
                    foundNewPosts = true;
                    unreadPosts.add(p);
                    allPosts.add(p);
                }
            }

        } catch (Exception e) {
            //Alert the user if something goes wrong.
            AlertDialog.Builder alert = new AlertDialog.Builder(this.mainActivity);
            alert.setTitle("Something went wrong. Can't connect to page").setMessage("Sorry but" +
                    " I load a feed page:" + feedUrl  +  " Make sure you have an internet " +
                    "connection. If that doesn't work then try liking directly to the RSS or " +
                    "Feedburner page.").setPositiveButton("OK", null).show();
        }


    }

    /**
     * Establishes the connection to the Rss feed to get posts from it.
     */
    private void fetchXML(final boolean openFeed){
        //runs on a new thread in the background
        final Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    if(feedUrl!=null&&!feedUrl.equals("")&&!feedUrl.equals(" ")) {
                        Looper.prepare();
                        //Establishes a connection to the Feed URL
                        URL url = new URL(feedUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(10000);
                        conn.setConnectTimeout(15000);
                        conn.setRequestMethod("GET");
                        conn.setDoInput(true);
                        conn.connect();
                        //Reads the XML
                        InputStream stream = conn.getInputStream();
                        xmlFactoryObject = XmlPullParserFactory.newInstance();
                        XmlPullParser parser = xmlFactoryObject.newPullParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                        parser.setInput(stream, null);
                        //Parses the feed
                        getRss(parser);
                        stream.close();
                        //if new posts were found, update the users feed
                        if (foundNewPosts && openFeed) {
                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    GetContent.foundNewContent();
                                }
                            });
                        }
                    }
                } catch (final MalformedURLException e)
                {
                    //if the URL is not valid then display an error message
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
                            alert.setTitle("Malformed URL").setMessage("The URL " + feedUrl + " seems to be malformed. Try refreshing the page").setPositiveButton("OK", null).show();
                            if(feedUrl==null||feedUrl.equals("")||feedUrl.equals(" ")) {
                                GetContent.allFeeds.remove(this);
                                GetContent.foundNewContent();
                            }
                        }
                    });
                }
                catch (Exception e) {
                    //display an error if the URL can't be read
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
                            alert.setTitle("Bad URL").setMessage("The URL " + feedUrl + " can't be axcessed. Try refreshing the page").setPositiveButton("OK", null).show();
                            if(feedUrl==null||feedUrl.equals("")||feedUrl.equals(" ")) {
                                GetContent.allFeeds.remove(this);
                                GetContent.foundNewContent();
                            }
                        }
                    });
                }
            }
        });
        //run the thread
        thread.start();
    }

    /**
     * Updates the feed
     */
    public void update(MainActivity mainActivity, boolean openFeed)
    {
        foundNewPosts = false;
        this.mainActivity = mainActivity;
        fetchXML(openFeed);
    }

    /**
     * Saves the feeds data by first saving the url and then the data from each post
     */
    public String getSaveData()
    {
        if(allPosts.size()>0) {
            String data = feedUrl;
            for (Post p : allPosts) {
                data += "qzpsfaaa" + p.getSaveData();
            }
            return data;
        }else return "";
    }

    /**
     * Returns the URL for the feed
     */
    public String getFeedURL()
    {
        return feedUrl;
    }
}
