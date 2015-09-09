package rss.printlnstuidios.com.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Justin Hill on 12/24/2014.
 *
 * This class acts as an adapter class for the ListView in the MainActivity that shows the users posts
 */
public class ContentListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    /**
     * Establishes the context cor the Content Adapter
     */
    public ContentListAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
    }

    /**
     * Returns the number of posts in the list
     */
    @Override
    public int getCount() {
        return GetContent.getLength();
    }

    /**
     * Returns a specific post from the list
     */
    @Override
    public Object getItem(int position) {
        return GetContent.getContent(position);
    }

    /**
     * Returns an id for a posts. This id is equal to its position in the list.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Updates the ListView when the posts in it change
     */
    public void update() {
        this.notifyDataSetChanged();
    }

    /**
     * Gets and stores the elements of the posts
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        //see if we need to create a new view
        if (convertView == null) {


            convertView = inflater.inflate(R.layout.feed_box, null);

            // create holders to help conserve memory over just pouting the text directly in the list
            holder = new ViewHolder();
            holder.imageImageView = (ImageView) convertView.findViewById(R.id.img);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.text_title);
            holder.textTextView = (TextView) convertView.findViewById(R.id.text_post);
            holder.sourceTextView = (TextView) convertView.findViewById(R.id.text_source);


            convertView.setTag(holder);
        } else {


            holder = (ViewHolder) convertView.getTag();
        }

        // Get the text and image url data for the post
        String[] data = GetContent.getContent(position);

        // See if there in an image that goes along with the post
        if (data[2]!=null&&data[2]!="") {
            try {
                Picasso.with(context).load(data[2]).placeholder(R.drawable.ic_launcher).into(holder.imageImageView);
            }catch(Exception e)
            { holder.imageImageView.setImageResource(R.drawable.ic_launcher); }

        }
        else
        {
            // If there is no image then use a default image
            holder.imageImageView.setImageResource(R.drawable.ic_launcher);
        }

        // Put each of the parts of the post into the correct textviews
        String title = "";
        String text = "";
        String source = "";
        String url = "";

        if (data[0]!=null)
        {
           title = data[0];
        }

        if (data[1]!=null)
        {
            text = data[1];
        }

        if (data[3]!=null)
        {
            source = data[3];
        }

        if (data[4]!=null)
        {
            url = data[4];
        }

        if(data[5].equals("read"))
        {
            holder.read = true;

        }else{
            holder.read = false;
        }

        holder.starred = data[7].equals("star");



        // Display the text
        holder.titleTextView.setText(title);
        if (holder.read) {
            if(!holder.starred)
                holder.titleTextView.setTypeface(null, Typeface.NORMAL);
            else{
                holder.titleTextView.setTypeface(null, Typeface.NORMAL);
            }
        }
        else
            holder.titleTextView.setTypeface(null, Typeface.BOLD);
        //format any html in the post's text
        if(text.contains("/>")||text.contains("</"))
        {holder.textTextView.setText(Html.fromHtml(text));}
        else
        {holder.textTextView.setText(text);}
        holder.sourceTextView.setText(source);
        holder.url = url;
        //return the new view
        return convertView;
    }

    /**
     * Created by Justin Hill on 12/24/2014.
     *
     * This class holds the elements of a post in the ContentAdapter
     */
    private static class ViewHolder {
        public ImageView imageImageView;
        public TextView titleTextView;
        public TextView textTextView;
        public TextView sourceTextView;
        public String url;
        public boolean read;
        public boolean starred;
    }
}
