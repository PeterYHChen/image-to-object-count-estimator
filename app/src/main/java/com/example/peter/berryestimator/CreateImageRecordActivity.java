package com.example.peter.berryestimator;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.gc.materialdesign.widgets.Dialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class CreateImageRecordActivity extends ActionBarActivity {

    public static final String IMAGE_RECORD = "image_record";
    public static final String IMAGE_RECORD_POSITION = "image_record_position";
    public static final String IMAGE_RECORD_ACTION = "image_record_action";
    public static final int IMAGE_RECORD_ACTION_NOT_FOUND = -1;
    public static final int IMAGE_RECORD_CREATE = 0;
    public static final int IMAGE_RECORD_EDIT = 1;
    public static final int IMAGE_RECORD_REMOVE = 2;
    public static final String NO_TITLE = "(No title)";

    private DBManager dbManager;

    private ImageRecord imageRecord;
    private int mAction;

    private static boolean imageIsChanged = false;
    private static long timelog;

    private ImageView imageView;
    private Spinner targetTypeSpinner;
    private EditText imageLocationEditText;
    private EditText titleEditText;
    private EditText actualCountEditText;

//    private DisplayImageOptions mImageOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyUtils.startTimelog();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_image_record);

        dbManager = new DBManager(this);

        // read image record data
        imageRecord = getIntent().getParcelableExtra(IMAGE_RECORD);
        mAction = getIntent().getIntExtra(IMAGE_RECORD_ACTION, IMAGE_RECORD_ACTION_NOT_FOUND);

        // initiate and config imageloader
        initImageLoader();

        imageView = (ImageView) findViewById(R.id.record_image);
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onDisplayImage();
//            }
//        });

        displayImageInView(imageRecord);

        // set text fields and spinners
        titleEditText = (EditText) findViewById(R.id.record_title);
        targetTypeSpinner = (Spinner) findViewById(R.id.record_target_type);
        imageLocationEditText = (EditText) findViewById(R.id.record_image_location);
        actualCountEditText = (EditText) findViewById(R.id.record_actual_count);

        Button pickImageButton = (Button) findViewById(R.id.pick_image_button);
        Button deleteButton = (Button) findViewById(R.id.delete_button);

        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(MyUtils.getConfiguredGalleryIntent(), MyUtils.PICK_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecordRemoveDialog();
            }
        });

        final ArrayAdapter<CharSequence> targetTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.object_type_array, android.R.layout.simple_spinner_item);
        targetTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        targetTypeSpinner.setAdapter(targetTypeAdapter);

        // if create record event, set default value
        if(mAction == IMAGE_RECORD_CREATE){
            targetTypeSpinner.setSelection(0);
            deleteButton.setVisibility(View.GONE);

        } else {
            // if edit record event, display data from image record
            targetTypeSpinner.setSelection(targetTypeAdapter.getPosition(imageRecord.getTargetType()));

            titleEditText.setText(imageRecord.getTitle().equals(NO_TITLE) ? "" : imageRecord.getTitle());
            imageLocationEditText.setText(imageRecord.getImageLocation());
            if (imageRecord.getActualCount() >= 0) {
                actualCountEditText.setText(String.valueOf(imageRecord.getActualCount()));
            }
            deleteButton.setVisibility(View.VISIBLE);
        }

        MyUtils.endTimelog("create image record interface");
    }

    public void onDisplayImage() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(MainTabActivity.TAG_VIEW_IMAGE_DIALOG_FRAGMENT);
        if (prev != null) {
            ft.remove(prev);
            ft.commit();
        }

        ViewImageDialogFragment viewImageDialogFragment = ViewImageDialogFragment.newInstance(imageRecord);
        viewImageDialogFragment.show(getSupportFragmentManager(), MainTabActivity.TAG_VIEW_IMAGE_DIALOG_FRAGMENT);
    }

    private void showRecordRemoveDialog() {
        // com.gc.materialdesign.widgets
        final Dialog recordRemoveDialog = new Dialog(this, "Warning", "Do you want to delete this record?");
        recordRemoveDialog.addCancelButton("CANCEL");
        recordRemoveDialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReturnData(IMAGE_RECORD_REMOVE);
            }
        });

        recordRemoveDialog.setOnCancelButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordRemoveDialog.dismiss();
            }
        });
        recordRemoveDialog.show();
    }

    private void initImageLoader() {
        if(!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(getApplicationContext());
            ImageLoader.getInstance().init(config);
        }
    }

    // display image in imageView
    public void displayImageInView(ImageRecord imageRecord) {
        Log.d("image path", imageRecord.getImagePath());

        ImageLoader.getInstance().displayImage(imageRecord.getImagePath(), imageView, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imagePath, View view, Bitmap loadedImage) {
                    Log.d("loadedImage size", loadedImage.getByteCount() / 1024.0 / 1024 + "MB");
                    if(loadedImage.hasAlpha()) {
                        showTempInfo("Alpha value detected in chosen image");
                    } else {
                        showTempInfo("No alpha value detected in chosen image");
                    }
                }
            });

//        if (MyUtils.imagePathIsValid(imageRecord.getImagePath())){
//            ImageLoader.getInstance().displayImage(imageRecord.getImagePath(), imageView, new SimpleImageLoadingListener() {
//                @Override
//                public void onLoadingComplete(String imagePath, View view, Bitmap loadedImage) {
//                    Log.d("loadedImage size", loadedImage.getByteCount() / 1024.0 / 1024 + "MB");
//                    if(loadedImage.hasAlpha()) {
//                        showTempInfo("Alpha value detected in chosen image");
//                    } else {
//                        showTempInfo("No alpha value detected in chosen image");
//                    }
//                }
//            });
//
//            //TODO: decide whether to show scaled image when original one is deleted
//            // if the record was saved in db before
//        } else if (!imageRecord.getRecordId().isEmpty()){
//            Cursor cursor = dbManager.findRowCursor(imageRecord);
//            showTempInfo("image not exists, use scaled image");
//            if (cursor.getCount() > 0) {
//                cursor.moveToFirst();
//                String imageString = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE));
//                imageView.setImageBitmap(MyUtils.decodeBitmapFromString(imageString));
//            }
//            cursor.close();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MyUtils.PICK_IMAGE_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    // transform content path into absolute path and save it into record
                    Uri imageUri = data.getData();
                    String imagePath = MyUtils.getAbsImagePath(this, imageUri);

                    // if path is changed
                    if (!imageRecord.getImagePath().equals(imagePath)) {
                        imageRecord.setImagePath(imagePath);
                        imageRecord.setImageTakenDate(MyUtils.getLastModifiedDate(imageRecord.getImagePath()));
                        Log.d("Image retrieved from", imageRecord.getImagePath());
                        Log.d("Image last modified", imageRecord.getImageTakenDate() + "");
                        displayImageInView(imageRecord);

                        imageIsChanged = true;

                    } else {
                        imageIsChanged = false;
                    }

                } else {
                    Log.e("------", "Pick image from gallery error");
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(IMAGE_RECORD, imageRecord);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        imageRecord = savedInstanceState.getParcelable(IMAGE_RECORD);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.closeDB();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_image_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //save image record
        if (id == R.id.action_save) {
            // retrieve values

            // TODO: change file name of the image
            String text = titleEditText.getText().toString();
            imageRecord.setTitle(text.isEmpty() ? NO_TITLE : text);

            imageRecord.setTargetType(targetTypeSpinner.getAdapter().
                    getItem(targetTypeSpinner.getSelectedItemPosition()).toString());

            imageRecord.setImageLocation(imageLocationEditText.getText().toString());

            // if count is empty, no need to save
            text = actualCountEditText.getText().toString();
            if (!text.isEmpty()) {
                imageRecord.setActualCount(Integer.parseInt(text));
            }

            if (imageRecord.getImagePath().isEmpty()){
                showTempInfo("Please choose a picture");
                return true;
            }

//            if (!MyUtils.imageExists(imageRecord.getImagePath())) {
//                showTempInfo("Image does not exist on device, please choose another picture");
//                return true;
//            }

            int width = 230;
            String compressedThumbnailString = getCompressedThumbnailString(imageRecord.getImagePath(), width);
            imageRecord.setCompressedThumbnailString(compressedThumbnailString);
            Log.d("compressed image length", imageRecord.getCompressedThumbnailString().length() / 1024.0 + "KB");

            imageRecord.setIsSynced(false);

            setReturnData(mAction);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // return data back to main tab activity
    private void setReturnData(int action){
        String imageString = null;

        switch (action){
            case IMAGE_RECORD_CREATE:
                Log.d("----------", "insert image record");
                // TODO: reduce time cost, too slow, can try new runnable to open another thread
                imageString = MyUtils.getCompressedImageString(this, imageRecord.getImagePath());
                if (imageString == null) {
                    return;
                }

                imageRecord.setRecordId(dbManager.insert(imageRecord, imageString));
                break;

            case IMAGE_RECORD_EDIT:
                Log.d("----------", "update image record");
                if (imageIsChanged){
                    // TODO: reduce time cost, compression too slow
                    imageString = MyUtils.getCompressedImageString(this, imageRecord.getImagePath());
                    if (imageString == null) {
                        return;
                    }
                    imageRecord.setEstimate(-1);

                    dbManager.update(imageRecord, imageString, "");
                } else {
                    dbManager.update(imageRecord, null, null);
                }
                break;

            case IMAGE_RECORD_REMOVE:
                Log.d("----------", "remove image record");
                dbManager.delete(imageRecord);
                break;

            default:
                Log.e("image record", "action not found");
                showTempInfo("fail to recognize action");
                return;
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra(IMAGE_RECORD, imageRecord);
        returnIntent.putExtra(IMAGE_RECORD_ACTION, action);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private String getCompressedThumbnailString(String imagePath, int width) {
        // Set compressed thumbnail of image
        ImageSize imageSize = new ImageSize(width, width);
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(imagePath, imageSize);

        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, width, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        return MyUtils.compressBitmapToString(bitmap);
    }

    private void showTempInfo(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}
