/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;

/**
 * Displays the note of a purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class PurchaseNoteFragment extends Fragment {

    private static final String BUNDLE_NOTE = "BUNDLE_NOTE";
    private FragmentInteractionListener mListener;
    private String mNote;
    private TextView mTextViewNote;

    public PurchaseNoteFragment() {
    }

    /**
     * Returns a new instance of {@link PurchaseNoteFragment}.
     *
     * @param note the note to display
     * @return a new instance of {@link PurchaseNoteFragment}
     */
    public static PurchaseNoteFragment newInstance(String note) {
        Bundle args = new Bundle();
        args.putString(BUNDLE_NOTE, note);
        PurchaseNoteFragment fragment = new PurchaseNoteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ActivityListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            mNote = args.getString(BUNDLE_NOTE, "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_show_note, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextViewNote = (TextView) view.findViewById(R.id.tv_purchase_note);
        setNote(mNote);
    }

    private void setNote(@NonNull String note) {
        mTextViewNote.setText(note);
    }

    /**
     * Updates the note {@link TextView} and displays a message to the user that it was changed.
     *
     * @param note the new note to set
     */
    public void updateNote(@NonNull String note) {
        setNote(note);
        Snackbar.make(mTextViewNote, R.string.toast_note_edited, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_purchase_edit_note_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_purchase_edit_note_edit:
                mListener.editNote();
                return true;
            case R.id.action_purchase_edit_note_delete:
                mListener.deleteNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface FragmentInteractionListener {
        /**
         * Handles the call to delete the note from the purchase.
         */
        void deleteNote();

        /**
         * Handles the call to edit the note from the purchase.
         */
        void editNote();
    }
}
