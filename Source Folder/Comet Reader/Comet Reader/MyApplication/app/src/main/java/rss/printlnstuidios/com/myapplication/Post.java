package rss.printlnstuidios.com.myapplication;

import android.os.Debug;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Justin Hill on 12/25/2014.
 *
 * Stores the data for an individual post from a feed
 */
public class Post implements Comparable {

    //variables to store the data
    public String title;
    public String description;
    public String url;
    public String date;
    public String source;
    public String image;
    public String text;
    public boolean read = false;
    public Feed feed;
    public boolean star = false;


    /**
     * Constructor to set the variables equal to the content parsed from the Rss feed
     */
    public Post(String title, String description, String url, String date, String image, String source, Feed feed, boolean read, String text, boolean star)
    {
        this.title = title;
        this.description = description;
        this.date = date;
        this.image = image;
        this.url = url;
        this.source = source;
        this.feed = feed;
        this.read = read;

        if(text!=null&&!text.equals(""))
        {
            this.text = text;
        }
        else
        {
            this.text = description;
        }
        this.star = star;
    }

    /**
     * Returns if two posts have the same title and date
     */
    @Override
    public boolean equals(Object o)
    {
        return (title.equals(((Post)o).title)&&date.equals(((Post)o).date));
    }

    /**
     * Determines which of two posts comes first in chronological order
     */
    @Override
    public int compareTo(Object another) {
        DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        DateFormat atomDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        try {
            if(date.equals(""))
            {
                return 1;
            }
            else if(((Post)another).date.equals(""))
            {
                return -1;
            }
            Date thisDate, thatDate;

            if(date.contains(","))
                thisDate = formatter.parse(date);
            else
            {
                thisDate = atomDate.parse(date.substring(0,16));
            }
            if(((Post)another).date.contains(","))
                thatDate = formatter.parse(((Post)another).date);
            else
                thatDate = atomDate.parse(((Post)another).date.substring(0,16));

            if(thisDate.getTime()>thatDate.getTime())
            {

                return -1;
            }
            else
            {
                return 1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns the data necessary to save the post as a string
     */
    public String getSaveData()
    {
        description = description.replace("\n","<br />");
        return title + "qzpsfaaa" + description + "qzpsfaaa" + url + "qzpsfaaa" + date + "qzpsfaaa" + source + "qzpsfaaa" + image + "qzpsfaaa" + text + "qzpsfaaa" + read + "qzpsfaaa" + star;
    }
}
