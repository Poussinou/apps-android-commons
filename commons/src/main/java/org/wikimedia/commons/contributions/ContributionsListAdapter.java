package org.wikimedia.commons.contributions;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import org.wikimedia.commons.CommonsApplication;
import org.wikimedia.commons.MediaWikiImageView;
import org.wikimedia.commons.R;
import org.wikimedia.commons.Utils;

class ContributionsListAdapter extends CursorAdapter {

    private DisplayImageOptions contributionDisplayOptions = Utils.getGenericDisplayOptions().build();;
    private SherlockFragment fragment;

    public ContributionsListAdapter(SherlockFragment fragment, Cursor c, int flags) {
        super(fragment.getActivity(), c, flags);
        this.fragment = fragment;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View parent = fragment.getActivity().getLayoutInflater().inflate(R.layout.layout_contribution, viewGroup, false);
        parent.setTag(new ContributionViewHolder(parent));
        return parent;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ContributionViewHolder views = (ContributionViewHolder)view.getTag();
        Contribution contribution = Contribution.fromCursor(cursor);

        String actualUrl = TextUtils.isEmpty(contribution.getImageUrl()) ? contribution.getLocalUri().toString() : contribution.getThumbnailUrl(320);

        if(views.url == null || !views.url.equals(actualUrl)) {
            if(actualUrl.startsWith("http")) {
                MediaWikiImageView mwImageView = (MediaWikiImageView)views.imageView;
                mwImageView.setMedia(contribution, ((CommonsApplication) fragment.getActivity().getApplicationContext()).getImageLoader());
                // FIXME: For transparent images
            } else {
                com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(actualUrl, views.imageView, contributionDisplayOptions, new SimpleImageLoadingListener() {

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if(loadedImage.hasAlpha()) {
                            views.imageView.setBackgroundResource(android.R.color.white);
                        }
                        views.seqNumView.setVisibility(View.GONE);
                    }

                });
            }
            views.url = actualUrl;
        }

        BitmapDrawable actualImageDrawable = (BitmapDrawable)views.imageView.getDrawable();
        if(actualImageDrawable != null && actualImageDrawable.getBitmap() != null && actualImageDrawable.getBitmap().hasAlpha()) {
            views.imageView.setBackgroundResource(android.R.color.white);
        } else {
            views.imageView.setBackgroundDrawable(null);
        }

        views.titleView.setText(contribution.getDisplayTitle());

        views.seqNumView.setText(String.valueOf(cursor.getPosition() + 1));
        views.seqNumView.setVisibility(View.VISIBLE);

        switch(contribution.getState()) {
            case Contribution.STATE_COMPLETED:
                views.stateView.setVisibility(View.GONE);
                views.progressView.setVisibility(View.GONE);
                views.stateView.setText("");
                break;
            case Contribution.STATE_QUEUED:
                views.stateView.setVisibility(View.VISIBLE);
                views.progressView.setVisibility(View.GONE);
                views.stateView.setText(R.string.contribution_state_queued);
                break;
            case Contribution.STATE_IN_PROGRESS:
                views.stateView.setVisibility(View.GONE);
                views.progressView.setVisibility(View.VISIBLE);
                long total = contribution.getDataLength();
                long transferred = contribution.getTransferred();
                if(transferred == 0 || transferred >= total) {
                    views.progressView.setIndeterminate(true);
                } else {
                    views.progressView.setProgress((int)(((double)transferred / (double)total) * 100));
                }
                break;
            case Contribution.STATE_FAILED:
                views.stateView.setVisibility(View.VISIBLE);
                views.stateView.setText(R.string.contribution_state_failed);
                views.progressView.setVisibility(View.GONE);
                break;
        }

    }
}