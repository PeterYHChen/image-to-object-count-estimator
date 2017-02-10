package com.example.peter.berryestimator;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.internal.IMapFragmentDelegate;

public class WarningDialogFragment extends DialogFragment {

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";

    private String title;
    private String message;
    private ImageRecord imageRecord;

    private OnAcceptListener mListener;

    public static WarningDialogFragment newInstance(String title, String msg, ImageRecord imageRecord) {
        WarningDialogFragment fragment = new WarningDialogFragment();

        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, msg);
        args.putParcelable(CreateImageRecordActivity.IMAGE_RECORD, imageRecord);
        fragment.setArguments(args);

        return fragment;
    }

    //    Mandatory empty constructor for the fragment manager to instantiate the
//    fragment (e.g. upon screen orientation changes).
    public WarningDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getArguments().getString(TITLE);
        message = getArguments().getString(MESSAGE);
        imageRecord = getArguments().getParcelable(CreateImageRecordActivity.IMAGE_RECORD);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // com.gc.materialdesign.widgets
        final com.gc.materialdesign.widgets.Dialog dialog = new com.gc.materialdesign.widgets.Dialog(getActivity(), title, message);
        dialog.addCancelButton("CANCEL");
        dialog.setOnCancelButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onAcceptWarning(imageRecord);
                }
            }
        });
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("----------", "on attach");
        try {
            mListener = (OnAcceptListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("----------", "on detach");
        mListener = null;
    }

    public interface OnAcceptListener {
        void onAcceptWarning(ImageRecord imageRecord);
    }
}
