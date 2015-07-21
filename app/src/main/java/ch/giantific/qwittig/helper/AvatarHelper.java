package ch.giantific.qwittig.helper;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import ch.giantific.qwittig.data.models.ImageAvatar;

/**
 * Created by fabio on 10.12.14.
 */
public class AvatarHelper extends Fragment {

    private static final String BUNDLE_URI = "uri";
    private HelperInteractionListener mListener;
    private Uri mImageUri;

    public static AvatarHelper newInstance(Uri uri) {
        AvatarHelper fragment = new AvatarHelper();
        Bundle args = new Bundle();
        args.putParcelable(BUNDLE_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        if (getArguments() != null) {
            mImageUri = getArguments().getParcelable(BUNDLE_URI);
        }

        AvatarWorkerTask avatarWorkerTask = new AvatarWorkerTask();
        avatarWorkerTask.execute(mImageUri);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onPostExecute(ImageAvatar avatar);
    }

    private class AvatarWorkerTask extends AsyncTask<Uri, Void, ImageAvatar> {

        // TODO: maybe remove old image from ImageView in onPreExecute

        @Override
        protected ImageAvatar doInBackground(Uri... params) {
            Uri uri = params[0];
            return new ImageAvatar(getActivity(), uri); // TODO: getActivity() could be null
        }

        @Override
        protected void onPostExecute(ImageAvatar avatar) {
            if (mListener != null) {
                mListener.onPostExecute(avatar);
            }
        }
    }
}
