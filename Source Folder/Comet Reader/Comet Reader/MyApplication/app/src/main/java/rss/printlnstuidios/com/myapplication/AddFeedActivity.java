package rss.printlnstuidios.com.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin Hill on 12/26/2014.
 *
 * This Activity represents the Add Feed Screen. It allows the user to enter a URL of a feed
 * to add to to select from a list of feeds. It will display an error of the user enters an
 * invalid URL.
 */
public class AddFeedActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    //The three objects that appear on screen to the user. A button an Edit test field to enter a
    //URL and a spinner to select a feed
    private Button enterButton;
    private EditText editTextUrl;
    private Spinner selectFeedSpinner;

    /**
     * This method is called when the Activity is created. It finds the  three objects as sets
     * references to them equal to the objects defined above. It also establishes an OnClick Listener
     * for the button and an OnItemSelectedListener for the spinner
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_feed);

        enterButton = (Button) findViewById(R.id.button);
        editTextUrl = (EditText) findViewById(R.id.editText);

        enterButton.setOnClickListener(this);

        selectFeedSpinner = (Spinner) findViewById(R.id.tryFeed);
        selectFeedSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Creates the options menu in the Action bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }


    /**
     * Called if an item is selected in the options menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Called if the button defined above is clicked. It will call the addFeed() method with the
     * URL entered in the EditText box.
     */
    @Override
    public void onClick(View v) {
        addFeed(checkUrl(editTextUrl.getText().toString()),"", this);
}
    /**
     * Sees if the URL is valid and adds it the the list of feeds if it is.
     */
    private void addFeed(final String feedUrl, String name, final Context context) {
        final String thisFeedUrl = feedUrl;
        final Activity thisActivity = this;

        //create a new thread so it can run in parallel
        Thread thread = new Thread(new Runnable() {
            public void run()
            {
                run(thisFeedUrl);
            }

            public void run(String linkPost) {
                //Establish a connection to that url
                URL url = null;
                HttpURLConnection conn = null;
                try {
                    String text;
                    XmlPullParserFactory xmlFactoryObject;
                    url = new URL(linkPost);

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    //read the html from that page
                    InputStream stream = conn.getInputStream();
                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser xmlParser = xmlFactoryObject.newPullParser();
                    xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    int event;
                    xmlParser.setInput(stream, null);
                    event = xmlParser.getEventType();
                    boolean title = false;
                    boolean link = false;
                    boolean post = false;
                    //reads the html line by line until the end of the document
                    while (event != XmlPullParser.END_DOCUMENT) {
                        String tagName = xmlParser.getName();
                        switch (event) { //called when a tag is found when reading the html
                            case XmlPullParser.START_TAG:

                                break;
                            case XmlPullParser.TEXT:


                                break;
                            case XmlPullParser.END_TAG:
                            //record what tags have been found so we know if the document to formatted correctly
                                if (tagName.equals("title")) {
                                    title = true;
                                } else if (tagName.equals("link")) {
                                    link = true;
                                } else if (tagName.equals("entry") || tagName.equals("item")) {
                                    post = true;
                                }
                                break;
                        }
                        event = xmlParser.next();
                    }
                    if (title && link && post) {
                    //if all the necessary tags were found add the feed to the list of the users feeds
                        GetContent.allFeeds.add(new Feed(linkPost, true));

                        thisActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //take the user back the the MainActivity and let them know there feed was added
                                Toast.makeText(thisActivity, "Successfully Added Feed", Toast.LENGTH_LONG).show();
                                Intent main = new Intent(context, MainActivity.class);
                                context.startActivity(main);
                                GetContent.findContent();
                            }
                        });
                    }
                    else{
                        thisActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //display an error letting the user know that all the necessary tags could not be found.
                                AlertDialog.Builder alert = new AlertDialog.Builder(thisActivity);
                                alert.setTitle("Couldn't Parse Feed").setMessage("Sorry but I couldn't parse this page. Make sure you entered the URL correctly and that you have an internet connection. If that doesn't work then try liking directly to the RSS or Feedburner page.").setPositiveButton("OK", null).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    //if the document could not be parsed than look to see if there is a link for a valid
                    //RSS feed in the html for the page and try to parse that
                    try{
                        HttpClient client = new DefaultHttpClient();
                        HttpGet request = new HttpGet(thisFeedUrl);
                        HttpResponse response = client.execute(request);
                        String text = "";
                        InputStream in = response.getEntity().getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder str = new StringBuilder();
                        String line = null;
                        while((line = reader.readLine()) != null)
                        {
                            str.append(line);
                        }
                        in.close();
                        text = str.toString();
                        if (text.contains("<link rel=\"alternate\"") && text.contains("href=\"")) {
                            //if the page has a link the a RSS feed than try to read that
                            text = text.substring(text.indexOf("<link rel=\"alternate\""));
                            text = text.substring(text.indexOf("href=\"") + 6);
                            text = text.substring(0, text.indexOf("\""));
                            Looper.prepare();
                            run(checkUrl(text));
                        }
                        else
                        {
                            //if no RSS feed could be found display an error message
                            thisActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(thisActivity);
                                    alert.setTitle("No RSS Feed Found").setMessage("Sorry but I couldn't find an RSS feed for this page. Make sure you entered the URL correctly and that you have an internet connection. If that doesn't work then try liking directly to the RSS or Feedburner page.").setPositiveButton("OK", null).show();
                                }
                            });
                        }

                    } catch (MalformedURLException ex)
                    {
                        //if the URL given is not valid display an error message
                        AlertDialog.Builder alert = new AlertDialog.Builder(thisActivity);
                        alert.setTitle("Malformed URL").setMessage("The URL " + feedUrl + " seems to be malformed. Try refreshing the page").setPositiveButton("OK", null).show();
                    }
                    catch(Exception ex) {
                        //if there is some other type of error than display an error message
                        thisActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder alert = new AlertDialog.Builder(thisActivity);
                                alert.setTitle("Something went wrong. Can't connect to page").setMessage("Sorry but I load this page. Make sure you entered the URL correctly and that you have an internet connection. If that doesn't work then try liking directly to the RSS or Feedburner page.").setPositiveButton("OK", null).show();
                            }
                        });
                    }
                }
            }
        });
        //Run this thread we just created
        thread.start();
    }


    /**
     * Check to see if the URL is properly formatted. Returns a formatted URL if it is not.
     */
    private String checkUrl(String url)
    {
        //check to see if the URL contains http://. Add it if it does not
        if(!url.contains("http://")||url.contains("https://"))
        {
            if(url.indexOf("//")==0||url.indexOf("/")==0)
            {
                url = "http:" + url;
            }
            else if(url.indexOf(":")==0)
            {
                url = "http" + url;
            }
            else
            {
                url = "http://" + url;
            }
        }
        //remove any spaces in the URL
        url = url.replaceAll("\\s","");
        //return the fixed URL
        return url;
    }

    /**
     * Set the URL text box equal to the proper URL if the user selects recommended feed in the spinner
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){ //the URL depends on what position is selected in the spinner
            case 0:
                editTextUrl.setText("");
                break;
            case 1:
                editTextUrl.setText("http://www.michiganbpa.org/Rss.aspx?ContentID=1422910");
                break;
            case 2:
                editTextUrl.setText("http://www.engadget.com/");
                break;
            case 3:
                editTextUrl.setText("http://www.candyblog.net/");
                break;
            case 4:
                editTextUrl.setText("http://feeds.ign.com/ign/all");
                break;
            case 5:
                editTextUrl.setText("http://www.npr.org/rss/rss.php?id=1001");
                break;
            case 6:
                editTextUrl.setText("http://www.reddit.com/.rss");
                break;
            case 7:
                editTextUrl.setText("http://www.reddit.com/r/til/.rss");
                break;
            case 8:
                editTextUrl.setText("http://www.reddit.com/r/technology/.rss");
                break;
            case 9:
                editTextUrl.setText("http://feeds.feedburner.com/TechCrunch/");
                break;
            case 10:
                editTextUrl.setText("http://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml");
                break;
        }
    }

    /**
     * If nothing is selected in the spinner, do nothing.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
