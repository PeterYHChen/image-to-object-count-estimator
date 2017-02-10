package com.example.peter.berryestimator;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.Slider;
import com.gc.materialdesign.widgets.Dialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * Activities containing this fragment MUST implement the {@link OnRepositoryFragmentInteractionListener}
 * interface.
 */
public class RepositoryFragment extends Fragment implements ImageRecordListAdapter.ItemClickListener {
    private int mScrollOffset = 4;

    private OnRepositoryFragmentInteractionListener mListener;

    // database
    private DBManager dbManager;

    // list view / grid view and its adapter
    private RecyclerView mRecyclerView;
    private ImageRecordListAdapter mAdapter;

    private Gson gson;

    private EstimateRecordTask estimateRecordTask;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RepositoryFragment() {
    }

    public static RepositoryFragment newInstance() {
        return new RepositoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("", "----------create repository fragment");

        // Retain this fragment across configuration changes,
        // because we have a task to update UI, and want it always to keep reference to current context
        setRetainInstance(true);

        // initialize db and open db
        dbManager = new DBManager(getActivity());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        // if list not exist, it is the first time to query database for list
        if (!ImageRecordListAdapter.recordListExists()) {
            mAdapter = new ImageRecordListAdapter(this);
            QueryDatabaseTask queryDatabaseTask = new QueryDatabaseTask(dbManager.getQueryCursor());
            queryDatabaseTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);

        } else {
            mAdapter = new ImageRecordListAdapter(this);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("-------", "on create view");
        View view = inflater.inflate(R.layout.fragment_repository, container, false);

        // Set recycler view and  the adapter
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setAdapter(mAdapter);

        // set floating action buttons
        final FloatingActionMenu menu = (FloatingActionMenu) view.findViewById(R.id.menu);
        final FloatingActionButton fabCreate = (FloatingActionButton) view.findViewById(R.id.fab_create);
        final FloatingActionButton fabUploadAll = (FloatingActionButton) view.findViewById(R.id.fab_upload_all);
        final FloatingActionButton fabCancelAll = (FloatingActionButton) view.findViewById(R.id.fab_cancel_all);

        menu.setClosedOnTouchOutside(true);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > mScrollOffset) {
                    if (dy > 0) {
                        menu.hideMenuButton(true);
                    } else {
                        menu.showMenuButton(true);
                    }
                }
            }
        });

        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.close(true);
                if (mListener != null) {
                    mListener.onEditImageRecord(new ImageRecord(), CreateImageRecordActivity.IMAGE_RECORD_CREATE);
                }
            }
        });

        fabUploadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.close(true);
                mAdapter.setAllPending();
                estimateNextRecord();
            }
        });

        fabCancelAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.close(true);
                mAdapter.setAllIdling();
                cancelRecordEstimation();
            }
        });

        return view;
    }

    public void showSortingDialog() {
        final android.app.Dialog sortingDialog = new android.app.Dialog(getActivity());
        sortingDialog.setContentView(R.layout.dialog_sorting);
        sortingDialog.setTitle("Sort by");

        Button sortDateButton = (Button)sortingDialog.findViewById(R.id.sort_date_button);
        Button sortEstimateButton = (Button)sortingDialog.findViewById(R.id.sort_estimate_button);
        Button groupAllButton = (Button)sortingDialog.findViewById(R.id.group_all_button);
        Button groupEstimatedButton = (Button)sortingDialog.findViewById(R.id.group_estimated_button);
        Button groupNotEstimatedButton = (Button)sortingDialog.findViewById(R.id.group_not_estimated_button);

        sortDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortingDialog.dismiss();
                mAdapter.sortRecordsBy(ImageRecordListAdapter.SORT_BY_DATE);
            }
        });

        sortEstimateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortingDialog.dismiss();
                mAdapter.sortRecordsBy(ImageRecordListAdapter.SORT_BY_ESTIMATE);
            }
        });

        groupAllButton.setVisibility(View.GONE);
//        groupAllButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAdapter.groupRecordsBy(ImageRecordListAdapter.GROUP_BY_ALL);
//                sortingDialog.dismiss();
//            }
//        });

        groupEstimatedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortingDialog.dismiss();
                mAdapter.groupRecordsBy(ImageRecordListAdapter.GROUP_BY_ESTIMATED);
            }
        });

        groupNotEstimatedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortingDialog.dismiss();
                mAdapter.groupRecordsBy(ImageRecordListAdapter.GROUP_BY_NOT_ESTIMATED);
            }
        });

        sortingDialog.show();
    }

    @Override
    public void onItemClick(int position, ImageRecord imageRecord) {
        MyUtils.startTimelog();
        if (mListener != null) {
            mListener.onDisplayImageRecord(imageRecord);
        }
        // cancel estimation if it is being uploaded
        if (mAdapter.getImageRecordStatus(position) == ImageRecordListAdapter.Status.UPLOADING) {
            cancelRecordEstimation();
            mAdapter.setImageRecordStatus(imageRecord, ImageRecordListAdapter.Status.IDLING);
            showTempInfo("Cancelled estimation of record " + imageRecord.getRecordId() + imageRecord.getTitle());
        }
        MyUtils.endTimelog("button click");
    }

    @Override
    public void attemptEstimateRecord(ImageRecord imageRecord) {
        // check for internet availability
        if (!internetIsConnected()) {
            // cancel the running task
            mAdapter.setAllIdling();
            cancelRecordEstimation();
            showTempInfo("Internet is not available, please try again later");
            return;
        }

        // if task is not created or finished
        if (estimateRecordTask == null || estimateRecordTask.getStatus() == AsyncTask.Status.FINISHED) {
            mAdapter.setImageRecordStatus(imageRecord, ImageRecordListAdapter.Status.UPLOADING);
            estimateRecordTask = new EstimateRecordTask();
            estimateRecordTask.execute(imageRecord);
        }
    }

    @Override
    public void cancelRecordEstimation() {
        if (estimateRecordTask != null) {
            estimateRecordTask.cancel(true);
        }
    }

    public void estimateNextRecord() {
        ImageRecord imageRecord = mAdapter.getNextRecordToEstimate();
        if (imageRecord != null) {
            attemptEstimateRecord(imageRecord);
        }
    }

    // check for internet availability
    public boolean internetIsConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    //TODO: dialog window leaked to be fixed
    public void showRecordRemoveDialog(final int position, final ImageRecord imageRecord) {
        // com.gc.materialdesign.widgets
        final Dialog recordRemoveDialog = new Dialog(getActivity(), "Warning", "Do you want to delete this record?");

        recordRemoveDialog.addCancelButton("CANCEL");
        recordRemoveDialog.setOnCancelButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordRemoveDialog.dismiss();
            }
        });

        recordRemoveDialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getImageRecordStatus(position) == ImageRecordListAdapter.Status.UPLOADING) {
                    cancelRecordEstimation();
                }

                mAdapter.removeImageRecord(position, imageRecord);
                dbManager.delete(imageRecord);
            }
        });
        recordRemoveDialog.show();
    }

    public void addImageRecord(ImageRecord imageRecord) {
        mAdapter.addImageRecord(0, imageRecord);
        mRecyclerView.scrollToPosition(0);
    }

    public void updateImageRecord(ImageRecord imageRecord) {
        mAdapter.updateImageRecord(imageRecord, ImageRecordListAdapter.Status.IDLING);
    }

    public void removeImageRecord(ImageRecord imageRecord) {
        mAdapter.removeImageRecord(imageRecord);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("----------", "on attach");
        try {
            mListener = (OnRepositoryFragmentInteractionListener) activity;
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

    @Override
    public void onPause() {
        super.onPause();
        Log.d("----------", "on pause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("----------", "on resume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("----------", "destroy repository fragment");

        // cancel uploading to estimate task when exit program
        cancelRecordEstimation();
    }

    private void showTempInfo(String s){
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }

    // interface that interacts with parent activity
    public interface OnRepositoryFragmentInteractionListener {
        void onEditImageRecord(ImageRecord imageRecord, int action);
        void onDisplayImageRecord(ImageRecord imageRecord);
    }

    public class QueryDatabaseTask extends AsyncTask<Void, ImageRecord, Boolean> {
        Cursor cursor;
        int cnt;
        QueryDatabaseTask(Cursor cursor) {
            this.cursor = cursor;
            cnt = 0;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d("-------", "querying imageRecords from database");

            // from bottom to top, if has row
            // when cursor moves, the query is executed, so it takes longer time
            // TODO: can close and reopen cursor to avoid "window is full" warning - performance to be tested
            MyUtils.startTimelog();
            if (cursor.moveToLast()) {
                do {
                    String jsonData = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DATA));

                    ImageRecord imageRecord = gson.fromJson(jsonData, ImageRecord.class);
                    publishProgress(imageRecord);

                } while (cursor.moveToPrevious());
            }
            Log.d("------", "finish querying imageRecords from database");
            cursor.close();

            MyUtils.endTimelog("load 50 image records");

            return true;
        }

        @Override
        protected void onProgressUpdate(ImageRecord... imageRecords) {
            // once this fragment get destroyed and recreated, it loses the reference to mAdapter if running
            for(ImageRecord imageRecord : imageRecords) {
                mAdapter.addImageRecord(imageRecord);
                Log.d("-------", "adding record " + ++cnt);
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            // when fragment gets destroyed and recreated, the adapter isn't ready to add, update or remove data,
            // so a few of records will be lost, thus resetting list is necessary
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            Log.d("-------", "cancel reading data from database");
        }
    }

    public class EstimateRecordTask extends AsyncTask<ImageRecord, Integer, Boolean> {

        private ImageRecord imageRecord;
        private int position;
        private Result result;

        private static final String prefix = "http://";

        // acadia lab server
        private static final String hostAddress = "ml3gpu.acadiau.ca:1080";
        private static final String phpFile = "/index.php";

        private HttpURLConnection connection = null;

        @Override
        protected Boolean doInBackground(final ImageRecord... imageRecords) {
            imageRecord = new ImageRecord(imageRecords[0]);
            Log.d("-------", "uploading record " + imageRecord.getRecordId() + " and get estimate");

            MyUtils.startTimelog();

            String imageRecordData = "imageRecord=" + gson.toJson(imageRecord);

            String imageString = null;
            Cursor cursor = dbManager.findRowCursor(imageRecord);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                imageString = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE));
            }
            cursor.close();

            MyUtils.endTimelog("retrieve image and image record");

            String imageData = "image=" + imageString;
            String imageMd5Data = "imageMd5=" + MyUtils.getMd5(imageString);

//            Log.d("imageRecord", imageRecordData);
//            Log.d("image", imageData);
//            Log.d("image MD5", imageMd5Data);

            Log.d("record size", imageRecordData.length() / 1024 + "KB");
            Log.d("image size", imageData.length() / 1024 + "KB");

            String postData;
            // if record is synced to server before, just upload the record and md5
            if (imageRecord.isSynced()) {
                postData = imageRecordData + "&" + imageMd5Data;

            } else {
                postData = imageRecordData + "&" + imageData + "&" + imageMd5Data;
            }

            return sendDataToServer(postData);
        }
        // TODO: add get, put and remove request after adding users
        private boolean sendDataToServer(String postDataString) {
            URL url;
            boolean isSuccessReturned = true;

            try {
                // lab server
                url = new URL(prefix + hostAddress + phpFile);

                connection = (HttpURLConnection) url.openConnection();

                // Post data
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(postDataString.getBytes().length);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "UTF-8");
                connection.setUseCaches(false);

                // json configuration
//                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//                connection.setRequestProperty("Accept", "application/json");

//                // Get all request
//                connection.setDoInput(true);
//                connection.setRequestMethod("GET");

                // connection getOutputStream would do the connection
                // when transmitted, the '+' in data would be replaced with space because of url encoding, handle on server side
                MyUtils.startTimelog();
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                writeStream(out, postDataString);
                MyUtils.endTimelog("send image & image record data to server");

                int respondCode = connection.getResponseCode();
                Log.d("respond code", respondCode + "");

                if (respondCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = connection.getInputStream();
                    isSuccessReturned = readStream(in);
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (isCancelled()) {
                    Log.d("------", "task is cancelled");
                } else {
                    Log.d("------", "server error happened");
                }
                // return here, otherwise the task will stuck
                return false;

            } finally {
                if (connection != null){
                    connection.disconnect();
                }
            }

            return isSuccessReturned;
        }

        private void writeStream(OutputStream out, String postDataString) throws IOException {
            byte[] postData = postDataString.getBytes();
            int chunkSize = 1024; // 2k
            int offset = 0, percentage = 0;
            while (offset + chunkSize <= postData.length) {
                out.write(postData, offset, chunkSize);

                if (offset*100 / postData.length > percentage) {
                    percentage = offset * 100 / postData.length;
                    publishProgress(percentage);
                }

                offset += chunkSize;
            }
            out.write(postData, offset, postData.length - offset);

            //This two steps are key to get data back
            out.flush();
            out.close();

            publishProgress(100);
        }

        private boolean readStream(InputStream in) throws Exception {
            boolean isSuccessReturned = true;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    isSuccessReturned &= parseResponse(line);
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

            return isSuccessReturned;
        }

        private boolean parseResponse(String response) throws Exception {
            System.out.println(response);
            int bracIndex = response.indexOf(']');
            if (bracIndex > 0 && bracIndex < response.length() - 1) {
                String tag = response.substring(1, bracIndex);
                String data = response.substring(bracIndex + 1);
                Log.d("tag", tag);
                Log.d("data", data);

                if (tag.equals("fail")) {
                    return false;

                } else if (tag.equals("jsonResult")) {
                    result = gson.fromJson(data, Result.class);
                }
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... params) {
            int percentage = params[0];
            position = mAdapter.updateImageRecordProgress(position, imageRecord, percentage);
        }

        @Override
        protected void onPostExecute(Boolean isEstimated) {
            Log.d("------", "on post execute");

            if (isEstimated && result != null) {
                imageRecord.setEstimate((int) (Double.parseDouble(result.estimate)));
                imageRecord.setIsSynced(true);

                mAdapter.updateImageRecord(imageRecord, ImageRecordListAdapter.Status.FINISHED);

                // save in database
                dbManager.update(imageRecord, null, result.densityImage);
                Log.d("density image size", result.densityImage.length() / 1024 + "KB");

//                Log.d("estimate", result.estimate);
//                Log.d("densityImage", result.densityImage + "\n");
//                Log.d("objectImage", result.objectImage + "\n");

//
//                final android.app.Dialog resultDialog = new android.app.Dialog(getActivity());
//                resultDialog.setContentView(getActivity().getLayoutInflater().inflate(R.layout.dialog_result, null));
//                resultDialog.setTitle("Title...");
//
//                ImageView densityImageView = (ImageView)resultDialog.findViewById(R.id.density_image);
//                ImageView objectImageView = (ImageView)resultDialog.findViewById(R.id.object_image);
//                TextView estimateTextView = (TextView)resultDialog.findViewById(R.id.estimate);
//                Button okayButton = (Button)resultDialog.findViewById(R.id.okay);
//
//                densityImageView.setImageBitmap(MyUtils.decodeBitmapFromString(result.densityImage));
//                objectImageView.setImageBitmap(MyUtils.decodeBitmapFromString(result.objectImage));
//                estimateTextView.setText(result.estimate);
//                okayButton.setOnClickListener(new View.OnClickListener(){
//                    @Override
//                    public void onClick(View v) {
//                        resultDialog.dismiss();
//                        }
//                    });
//
//                resultDialog.show();

            } else {
                showTempInfo("Internet is not available or server failed, please try again");
                //set record status back to idling
                mAdapter.setImageRecordStatus(imageRecord, ImageRecordListAdapter.Status.IDLING);

            }

            // begin next task to estimate record no matter whether the previous one got estimated or not
            estimateRecordTask = null;
            estimateNextRecord();
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            Log.d("------", "on cancelled");
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
            //set record status back to idling
            mAdapter.setImageRecordStatus(imageRecord, ImageRecordListAdapter.Status.IDLING);

            estimateRecordTask = null;
            estimateNextRecord();
        }

        public class Result {
            public String estimate;
            public String densityImage;
            public String objectImage;
        }
    }
}