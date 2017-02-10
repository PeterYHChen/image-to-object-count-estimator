package com.example.peter.berryestimator;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.gc.materialdesign.views.Slider;

public class ViewImageDialogFragment extends DialogFragment {
    private static final String IMAGE_RECORD = "image_record";
    private ImageRecord imageRecord;
    private DBManager dbManager;

    public static ViewImageDialogFragment newInstance(ImageRecord imageRecord) {
        ViewImageDialogFragment fragment = new ViewImageDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(IMAGE_RECORD, imageRecord);
        fragment.setArguments(args);

        return fragment;
    }

//    Mandatory empty constructor for the fragment manager to instantiate the
//    fragment (e.g. upon screen orientation changes).
    public ViewImageDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyUtils.startTimelog();

        super.onCreate(savedInstanceState);
        dbManager = new DBManager(getActivity());

        imageRecord = getArguments().getParcelable(IMAGE_RECORD);

        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Light);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_view_image, container, false);

        ImageButton closeButton = (ImageButton)view.findViewById(R.id.close_button);
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

        final TouchImageView touchImageView = (TouchImageView)view.findViewById(R.id.touch_image_view);
        final TouchImageView touchDensityImageView = (TouchImageView)view.findViewById(R.id.touch_density_image_view);
        Slider transparentSlider = (Slider)view.findViewById(R.id.transparent_slider);

        touchImageView.setImageBitmap(MyUtils.decodeBitmapFromString(imageString));
        touchDensityImageView.setImageBitmap(MyUtils.decodeBitmapFromString(densityImageString));
        touchDensityImageView.setVisibility(densityImageString == null ? View.GONE : View.VISIBLE);
        transparentSlider.setVisibility(densityImageString == null ? View.GONE : View.VISIBLE);

        // mirroring two image view
        touchDensityImageView.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {
            @Override
            public void onMove() {
                touchImageView.setZoom(touchDensityImageView);
            }
        });

        touchImageView.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {
            @Override
            public void onMove() {
                touchDensityImageView.setZoom(touchImageView);
            }
        });

        transparentSlider.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int i) {
                touchDensityImageView.setAlpha(i);
            }
        });

        transparentSlider.setValue(120);
        touchDensityImageView.setAlpha(120);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MyUtils.endTimelog("image information interface");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbManager.closeDB();
    }
}
