package fr.free.nrw.commons.contributions;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import fr.free.nrw.commons.CommonsApplication;
import fr.free.nrw.commons.Media;
import fr.free.nrw.commons.R;

public class MediaListAdapter extends BaseAdapter {
    private ArrayList<Media> mediaList;
    private Activity activity;

    public MediaListAdapter(Activity activity, ArrayList<Media> mediaList) {
        this.mediaList = mediaList;
        this.activity = activity;
    }

    public void updateMediaList(ArrayList<Media> newMediaList) {
        // FIXME: Hack for now, replace with something more efficient later on
        for(Media newMedia: newMediaList) {
            boolean isDuplicate = false;
            for(Media oldMedia: mediaList ) {
                if(newMedia.getFilename().equals(oldMedia.getFilename())) {
                    isDuplicate = true;
                    break;
                }
            }
            if(!isDuplicate) {
                mediaList.add(0, newMedia);
            }
        }
    }
    @Override
    public int getCount() {
        return mediaList.size();
    }

    @Override
    public Object getItem(int i) {
        return mediaList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = activity.getLayoutInflater().inflate(R.layout.layout_contribution, null, false);
            view.setTag(new ContributionViewHolder(view));
        }

        Media m = (Media) getItem(i);
        ContributionViewHolder holder = (ContributionViewHolder) view.getTag();
        holder.imageView.setMedia(m, ((CommonsApplication)activity.getApplicationContext()).getImageLoader());
        holder.titleView.setText(m.getDisplayTitle());
        return view;
    }
}
