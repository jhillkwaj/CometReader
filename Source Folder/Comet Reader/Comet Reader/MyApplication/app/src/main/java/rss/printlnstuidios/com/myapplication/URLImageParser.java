package rss.printlnstuidios.com.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * Created by Justin Hill on 12/30/2014.
 *
 * Loads an image from a URL. THis is used to view images linked to within the html content of a post
 */
public class URLImageParser implements Html.ImageGetter {
    Context c;
    View container;

    /***
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     */
    public URLImageParser(View t, Context c) {
        this.c = c;
        this.container = t;
    }

    /***
     * Returns a URLDrawable object constructed from the image source
     */
    public Drawable getDrawable(String source) {
        URLDrawable url = new URLDrawable();
        // get the image source
        ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask( url);
        asyncTask.execute(source);
        // return reference to URL
        return url;
    }

    /***
     * Loads the drawable
     */
    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        URLDrawable url;
        public ImageGetterAsyncTask(URLDrawable d) {
            this.url = d;
        }

        /***
         * Loads the drawable
         */
        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        /***
         * Sets the reference equal to the result of the Http call
         */
        @Override
        protected void onPostExecute(Drawable result) {
            if(result!=null) {
                // set the image bounds
                url.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());
                // change the reference
                url.drawable = result;
                // redraw the image
                URLImageParser.this.container.invalidate();
            }
        }

        /***
         * Get the Drawable from URL
         */
        public Drawable fetchDrawable(String urlString) {
            try {
                InputStream is = fetch(urlString);
                Drawable drawable = Drawable.createFromStream(is, "src");
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                return drawable;
            } catch (Exception e) {
                return null;
            }
        }

        /***
         * Get the ImageStream from the URL using an http request
         */
        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlString);
            HttpResponse response = httpClient.execute(request);
            return response.getEntity().getContent();
        }
    }

    /***
     * Returns a URLDrawable object constructed from the image source
     */
    public static class URLDrawable extends BitmapDrawable{
        // Your image drawable
        protected Drawable drawable;

        /***
         * Draws the image and refreshes it
         */
        @Override
        public void draw(Canvas canvas) {
            if(drawable != null) {
                drawable.draw(canvas);
            }
        }
    }

}
