package com.example.peter.berryestimator;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnMainFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    private static final String IMAGE_PATH = "image_path";

    private OnMainFragmentInteractionListener mListener;

    private Uri imageUri;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainFragment.
     */
    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            imageUri = Uri.parse(savedInstanceState.getString(IMAGE_PATH, ""));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mFragmentView;
        mFragmentView =inflater.inflate(R.layout.fragment_main, container, false);

        Button mTakePictureButton = (Button) mFragmentView.findViewById(R.id.capture_image_button);
        Button mPickPictureButton = (Button) mFragmentView.findViewById(R.id.pick_image_button);

        mTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create a file to save the image
                imageUri = getOutputImageFilePath();
                if(imageUri != null){
                    // invoke camera intent
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // save image into the uri, but the imageUri is not passed internally
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                    startActivityForResult(cameraIntent, MyUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
            }
        });

        mPickPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(MyUtils.getConfiguredGalleryIntent(), MyUtils.PICK_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });

        return mFragmentView;
    }


    /** Create a File for saving an image or video */
    private Uri getOutputImageFilePath(){
        // check that the SDCard is mounted
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){

            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "berry_estimator");

            // Create the storage directory if it does not exist
            if (! mediaStorageDir.exists()){
                if (! mediaStorageDir.mkdirs()){
                    showTempInfo("Failed to create directory");
                    return null;
                }
            }

            // TODO: change the initial default file name
            // Create the initial image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".png");

            return Uri.fromFile(mediaFile);

        } else {
            Log.d("MyThesisApp", "SD card state: " + Environment.getExternalStorageState());
            showTempInfo("SD card not mounted");
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MyUtils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                // here imageUri is the absolute path
                if (imageUri != null){
                    showTempInfo("Image saved to:\n" + imageUri.toString());
                    Log.d("image saved in", imageUri.toString());

                    requestCreateImageRecord(imageUri.toString());
                }

            } else if (resultCode != Activity.RESULT_CANCELED){
                // Image capture failed, advise user
                showTempInfo("Image capture failed");
            }

        } else if (requestCode == MyUtils.PICK_IMAGE_ACTIVITY_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    imageUri = data.getData();
                    if (imageUri != null) {
                        // here imageUri is the content path
                        Log.d("Image retrieved from", imageUri.toString());

                        requestCreateImageRecord(MyUtils.getAbsImagePath(getActivity(), imageUri));
                    }
                } else {
                    Log.e("------", "Pick image from gallery error");
                }
            }
        }
    }

    private void requestCreateImageRecord(String absImagePath){
        ImageRecord imageRecord = new ImageRecord();

        Log.d("absolute path", absImagePath);
        imageRecord.setImagePath(absImagePath);
        imageRecord.setImageTakenDate(MyUtils.getLastModifiedDate(imageRecord.getImagePath()));

        if (mListener != null) {
            mListener.onEditImageRecord(imageRecord, CreateImageRecordActivity.IMAGE_RECORD_CREATE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (imageUri != null) {
            outState.putString(IMAGE_PATH, imageUri.toString());
        }
    }

    private void showTempInfo(String s){
        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMainFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMainFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnMainFragmentInteractionListener {
        void onEditImageRecord(ImageRecord imageRecord, int action);
    }

}
