package com.example.peter.berryestimator;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gc.materialdesign.views.Slider;
import com.github.clans.fab.FloatingActionButton;

import java.util.Date;

public class RecordInfoDialogFragment extends DialogFragment{

    private OnRecordInfoFragmentInteractionListener mListener;

    private ImageRecord imageRecord;

    // database
    private DBManager dbManager;

    private ImageButton closeButton;
    private ImageView imageVIew;
    private ImageView densityImageView;
    private Slider transparentSlider;

    private ImageView expandedImageVIew;
    private ImageView expandedDensityImageView;

    private TextView titleTextView;
    private TextView imageTakenDateTextView;
    private TextView createdDateTextView;
    private TextView targetTypeTextView;
    private TextView locationTextView;
    private TextView actualCountTextView;
    private TextView estimateTextView;

    private FloatingActionButton fabEditButton;

    public static RecordInfoDialogFragment newInstance(ImageRecord imageRecord) {
        RecordInfoDialogFragment fragment = new RecordInfoDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(CreateImageRecordActivity.IMAGE_RECORD, imageRecord);
        fragment.setArguments(args);

        return fragment;
    }

//    Mandatory empty constructor for the fragment manager to instantiate the
//    fragment (e.g. upon screen orientation changes).
    public RecordInfoDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyUtils.startTimelog();

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            imageRecord = savedInstanceState.getParcelable(CreateImageRecordActivity.IMAGE_RECORD);
        } else {
            imageRecord = getArguments().getParcelable(CreateImageRecordActivity.IMAGE_RECORD);
        }

        dbManager = new DBManager(getActivity());

        // portrait mode
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Light);
        } else {
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_record_info, container, false);

        closeButton = (ImageButton)view.findViewById(R.id.close_button);
        imageVIew = (ImageView)view.findViewById(R.id.record_image);
        densityImageView = (ImageView)view.findViewById(R.id.record_density_image);
        transparentSlider = (Slider)view.findViewById(R.id.transparent_slider);

        expandedImageVIew = (ImageView)view.findViewById(R.id.record_expanded_image);
        expandedDensityImageView = (ImageView)view.findViewById(R.id.record_expanded_density_image);

        titleTextView = (TextView)view.findViewById(R.id.record_title);
        imageTakenDateTextView = (TextView)view.findViewById(R.id.record_image_taken_date);
        createdDateTextView = (TextView)view.findViewById(R.id.record_created_date);
        targetTypeTextView = (TextView)view.findViewById(R.id.record_target_type);
        locationTextView = (TextView)view.findViewById(R.id.record_image_location);
        actualCountTextView = (TextView)view.findViewById(R.id.record_actual_count);
        estimateTextView = (TextView)view.findViewById(R.id.record_estimate);

        fabEditButton = (FloatingActionButton)view.findViewById(R.id.fab_edit);

        updateRecordInfoDialog(imageRecord);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MyUtils.endTimelog("record information interface");
    }

    public void updateRecordInfoDialog(final ImageRecord imageRecord) {
        this.imageRecord = imageRecord;

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Cursor cursor = dbManager.findRowCursor(imageRecord);
        String imageString = null;
        String densityImageString = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            imageString = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE));
            densityImageString = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DENSITY_IMAGE));
        }
        cursor.close();

        imageVIew.setImageBitmap(MyUtils.decodeBitmapFromString(imageString));
        densityImageView.setImageBitmap(MyUtils.decodeBitmapFromString(densityImageString));

        // TODO: try view.add to dynamically add views
        if (densityImageString == null || densityImageString.isEmpty()) {
            expandedImageVIew.setImageBitmap(null);
            expandedDensityImageView.setImageBitmap(null);
            transparentSlider.setVisibility(View.GONE);

        } else {
            expandedImageVIew.setImageBitmap(MyUtils.decodeBitmapFromString(imageString));
            expandedDensityImageView.setImageBitmap(MyUtils.decodeBitmapFromString(densityImageString));
            transparentSlider.setVisibility(View.VISIBLE);
        }

        View.OnClickListener onImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDisplayImage(imageRecord);
            }
        };

        imageVIew.setOnClickListener(onImageClickListener);
        densityImageView.setOnClickListener(onImageClickListener);
        expandedImageVIew.setOnClickListener(onImageClickListener);
        expandedDensityImageView.setOnClickListener(onImageClickListener);

        transparentSlider.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int i) {
                densityImageView.setAlpha(i);
            }
        });

        transparentSlider.setValue(120);
        densityImageView.setAlpha(120);

        titleTextView.setText(imageRecord.getTitle());
        imageTakenDateTextView.setText("Image taken at " + MyUtils.getTimeString(imageRecord.getImageTakenDate())
                + " on " + MyUtils.getDateString(imageRecord.getImageTakenDate(), true));
        // TODO: change created date to last modified date
        createdDateTextView.setText("Record created at " + MyUtils.getTimeString(imageRecord.getCreatedDate())
                + " on " + MyUtils.getDateString(imageRecord.getCreatedDate(), true));
        targetTypeTextView.setText(imageRecord.getTargetType());
        locationTextView.setText(imageRecord.getImageLocation());
        actualCountTextView.setText("actual count: " + imageRecord.getActualCount());
        estimateTextView.setText("estimate: " + imageRecord.getEstimate());

        // set visibility of data
        imageTakenDateTextView.setVisibility(imageRecord.getImageTakenDate() < 0 ? View.GONE : View.VISIBLE);
        locationTextView.setVisibility(imageRecord.getImageLocation().isEmpty() ? View.GONE : View.VISIBLE);
        actualCountTextView.setVisibility(imageRecord.getActualCount() < 0 ? View.GONE : View.VISIBLE);
        estimateTextView.setVisibility(imageRecord.getEstimate() < 0 ? View.GONE : View.VISIBLE);

        fabEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onEditImageRecord(imageRecord, CreateImageRecordActivity.IMAGE_RECORD_EDIT);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(CreateImageRecordActivity.IMAGE_RECORD, imageRecord);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("------", "dialog is dismissed");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbManager.closeDB();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("----------", "on attach");
        try {
            mListener = (OnRecordInfoFragmentInteractionListener) activity;
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

    public interface OnRecordInfoFragmentInteractionListener {
        void onEditImageRecord(ImageRecord imageRecord, int action);
        void onDisplayImage(ImageRecord imageRecord);
    }
}
