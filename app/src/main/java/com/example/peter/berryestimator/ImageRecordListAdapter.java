package com.example.peter.berryestimator;

import android.graphics.Bitmap;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class ImageRecordListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EMPTY_RECORD = 0;
    private static final int NORMAL_RECORD = 1;

    public static final int SORT_BY_DATE = 0;
    public static final int SORT_BY_ESTIMATE = 1;

    public static final int GROUP_BY_ALL = 10;
    public static final int GROUP_BY_ESTIMATED = 11;
    public static final int GROUP_BY_NOT_ESTIMATED = 12;

    private ItemClickListener onItemClickListener;

    // keep this list static to avoid problems when fragment is destroyed and recreated
    // due to other reasons like ram memory is exhausted
    private static ArrayList<Record> recordList;

    public ImageRecordListAdapter(@NonNull ItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;

        // the record list only gets initialized at the first time
        if (recordList == null) {
            Log.d("-------", "static recordList is created");
            recordList = new ArrayList<>();
        }
    }

    public static boolean recordListExists() {
        return recordList != null;
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    @Override
    public int getItemViewType(int position) {
//        if (recordList.get(position).show) {
//            return NORMAL_RECORD;
//        } else {
//            return EMPTY_RECORD;
//        }
        return NORMAL_RECORD;
    }

    @Override
    public ImageRecordViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View rowView;
        if (viewType == NORMAL_RECORD) {
            rowView = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.image_list_record, viewGroup, false);
        } else {
            rowView = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.empty_list_record, viewGroup, false);
        }

        return new ImageRecordViewHolder(rowView);
    }

    //TODO: set an upper limit of the array size, like 50 or 100 (must be < 128 because of async queue size)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int itemPostion) {
        switch (viewHolder.getItemViewType()) {
            case NORMAL_RECORD:
                configureImageRecordViewHolder((ImageRecordViewHolder)viewHolder, itemPostion);
                break;

            case EMPTY_RECORD:
                break;
        }
    }

    public void configureImageRecordViewHolder(final ImageRecordViewHolder imageRecordViewHolder, final int itemPostion) {
//        final Record record = recordList.get(getOriginalPosition(itemPostion));
        final Record record = recordList.get(itemPostion);
        final ImageRecord imageRecord = record.imageRecord;

        Bitmap bitmap = MyUtils.decodeBitmapFromString(imageRecord.getCompressedThumbnailString());
        imageRecordViewHolder.recordImage.setImageBitmap(bitmap);

//        imageRecordViewHolder.recordTitle.setText(imageRecord.getRecordId() + "--" + imageRecord.getTitle());
        imageRecordViewHolder.recordTitle.setText(imageRecord.getTitle());
        imageRecordViewHolder.recordCreatedDate.setText("Created: " + MyUtils.getProperTimeString(imageRecord.getCreatedDate()));

        switch (record.status) {
            case IDLING:
                imageRecordViewHolder.recordStatus.setText("NOT ESTIMATED");
                imageRecordViewHolder.recordEstimateButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                imageRecordViewHolder.uploadProgressBar.setVisibility(View.GONE);
                record.progressPercentage = 0;
                break;

            case FINISHED:
                imageRecordViewHolder.recordStatus.setText("ESTIMATED: " + imageRecord.getEstimate());
                imageRecordViewHolder.recordEstimateButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                imageRecordViewHolder.uploadProgressBar.setVisibility(View.GONE);
                record.progressPercentage = 0;
                break;

            case PENDING:
                imageRecordViewHolder.recordStatus.setText(record.status.toString());
                imageRecordViewHolder.recordEstimateButton.setImageResource(R.drawable.ic_clear_black_24dp);
                imageRecordViewHolder.uploadProgressBar.setVisibility(View.VISIBLE);
                imageRecordViewHolder.uploadProgressBar.setIndeterminate(true);
                break;

            case UPLOADING:
                imageRecordViewHolder.recordStatus.setText(record.message);
                imageRecordViewHolder.recordEstimateButton.setImageResource(R.drawable.ic_clear_black_24dp);
                imageRecordViewHolder.uploadProgressBar.setVisibility(View.VISIBLE);
                imageRecordViewHolder.uploadProgressBar.setIndeterminate(false);
                imageRecordViewHolder.uploadProgressBar.setProgress(record.progressPercentage);
                break;

            default:
                // pending or uploading
                imageRecordViewHolder.recordStatus.setText(record.status.toString());
        }

        imageRecordViewHolder.recordEstimateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (record.status == Status.UPLOADING) {
                    record.status = Status.IDLING;
                    onItemClickListener.cancelRecordEstimation();

                } else if (record.status == Status.PENDING) {
                    record.status = Status.IDLING;

                } else {
                    // when idling or finished, start estimation task, set to pending
                    record.status = Status.PENDING;
                    onItemClickListener.attemptEstimateRecord(imageRecord);
                }

                notifyItemChanged(imageRecordViewHolder.getPosition());
            }
        });

        imageRecordViewHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(imageRecordViewHolder.getPosition(), imageRecord);
            }
        });

        imageRecordViewHolder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemClickListener.showRecordRemoveDialog(imageRecordViewHolder.getPosition(), imageRecord);
                return true;
            }
        });
    }

    public int getOriginalPosition(int itemPosition) {
        int count = 0;
        for (int position = 0; position < recordList.size(); position++) {
            if (recordList.get(position).show) {
                if (count++ == itemPosition)
                    return position;
            }
        }
        return -1;
    }

    public static class ImageRecordViewHolder extends RecyclerView.ViewHolder {
        private View rowView;
        private ImageView recordImage;
        private TextView recordTitle;
        private TextView recordCreatedDate;
        private TextView recordStatus;
        private ImageButton recordEstimateButton;
        private ProgressBar uploadProgressBar;

        public ImageRecordViewHolder(View v){
            super(v);
            rowView = v;
            recordImage = (ImageView) rowView.findViewById(R.id.record_image_thumbnail);
            recordTitle = (TextView) rowView.findViewById(R.id.record_title);
            recordCreatedDate = (TextView) rowView.findViewById(R.id.record_created_date);
            recordStatus = (TextView) rowView.findViewById(R.id.record_status);
            recordEstimateButton = (ImageButton) rowView.findViewById(R.id.record_estimate_button);
            uploadProgressBar = (ProgressBar) rowView.findViewById(R.id.upload_progress_bar);
        }

        public void setOnClickListener(View.OnClickListener listener) {
            rowView.setOnClickListener(listener);
        }

        public void setOnLongClickListener(View.OnLongClickListener listener) {
            rowView.setOnLongClickListener(listener);
        }
    }

    public static class Record {
        private ImageRecord imageRecord;
        private boolean show;
        private String message;
        private int progressPercentage;
        private Status status;

        public Record(ImageRecord imageRecord) {
            this.imageRecord = imageRecord;
            show = true;
            message = "";
            progressPercentage = 0;
            status = imageRecord.getEstimate() >= 0 ? Status.FINISHED : Status.IDLING;
        }
    }

    /**
     * Indicates the current status of the record.
     */
    public enum Status {
        /**
         * Indicates that the record is ready to be uploaded
         */
        IDLING,
        /**
         * Indicates that the record is waiting to be uploaded.
         */
        PENDING,
        /**
         * Indicates that the record is being uploading.
         */
        UPLOADING,
        /**
         * Indicates that upload has finished and estimate is returned.
         */
        FINISHED,
    }

    public interface ItemClickListener {
        void onItemClick(int position, ImageRecord imageRecord);
        void attemptEstimateRecord(ImageRecord imageRecord);
        void cancelRecordEstimation();
        void showRecordRemoveDialog(int position, ImageRecord imageRecord);
    }

    public void addImageRecord(int position, ImageRecord imageRecord) {
        Log.d("------", "madapter insert record " + imageRecord.getRecordId());
        recordList.add(position, new Record(imageRecord));
        notifyItemInserted(position);
    }

    // add to the end of list
    public void addImageRecord(ImageRecord imageRecord) {
        recordList.add(new Record(imageRecord));
        notifyItemInserted(recordList.size() - 1);
    }

    public void removeImageRecord(ImageRecord imageRecord) {
        Log.d("------", "madapter removes record " + imageRecord.getRecordId());
        int position = getImageRecordPosition(imageRecord);
        if (inRange(position)) {
            recordList.remove(position);
        }
    }

    public void removeImageRecord(int position, ImageRecord imageRecord) {
        Log.d("------", "madapter removes record " + recordList.get(position).imageRecord.getRecordId());
        if (inRange(position) && recordList.get(position).imageRecord.getRecordId().equals(imageRecord.getRecordId())) {
            recordList.remove(position);
            notifyItemRemoved(position);

        } else {
            removeImageRecord(imageRecord);
        }
    }

    public void updateImageRecord(ImageRecord imageRecord, Status status){
        Log.d("------", "madapter update record " + imageRecord.getRecordId());
        int position = getImageRecordPosition(imageRecord);
        if (inRange(position)) {
            recordList.get(position).imageRecord = imageRecord;
            recordList.get(position).status = status;
            notifyItemChanged(position);
        }
    }

    public void updateImageRecord(int position, ImageRecord imageRecord, Status status) {
        Log.d("------", "madapter update record " + imageRecord.getRecordId());
        if (inRange(position) && recordList.get(position).imageRecord.getRecordId().equals(imageRecord.getRecordId())) {
            recordList.get(position).imageRecord = imageRecord;
            recordList.get(position).status = status;
            notifyItemChanged(position);

        } else {
            updateImageRecord(imageRecord, status);
        }
    }

    public int updateImageRecordProgress(int position, ImageRecord imageRecord, int percentage) {
        // if no record found before or the record on the position has moved away
        if (!inRange(position) || !recordList.get(position).imageRecord.getRecordId().equals(imageRecord.getRecordId())) {
            position = getImageRecordPosition(imageRecord);
        }

        if (inRange(position)) {
            recordList.get(position).progressPercentage = percentage;
            recordList.get(position).message =
                    percentage < 100 ? percentage + "% uploaded" : "image is being estimated";

            notifyItemChanged(position);
        }

        return position;
    }

    public void setImageRecordStatus(ImageRecord imageRecord, Status status) {
        int position = getImageRecordPosition(imageRecord);
        if (inRange(position)) {
            recordList.get(position).status = status;
            notifyItemChanged(position);
        }
    }

    public boolean inRange(int position) {
        return position >= 0 && position < recordList.size();
    }

    public Status getImageRecordStatus(int position) {
        return recordList.get(position).status;
    }

    public ImageRecord getImageRecord(int position){
        return recordList.get(position).imageRecord;
    }

    public ImageRecord getNextRecordToEstimate() {
        for (int position = 0; position < recordList.size(); position++) {
            if (recordList.get(position).status == Status.PENDING) {
                return recordList.get(position).imageRecord;
            }
        }
        // no next record to estimate
        return null;
    }

    public int getImageRecordPosition(ImageRecord imageRecord){
        for (int position = 0; position < recordList.size(); position++) {
            if (recordList.get(position).imageRecord.getRecordId().equals(imageRecord.getRecordId())) {
                return position;
            }
        }

        // no record found
        Log.e("NOT FOUND", "record " + imageRecord.getRecordId() + "not found!");
        return -1;
    }

    public void setImageRecordList(ArrayList<ImageRecord> imageRecordList) {
        recordList.clear();
        for (ImageRecord imageRecord : imageRecordList) {
            recordList.add(new Record(imageRecord));
        }

        notifyDataSetChanged();
    }

    public void removeAll(){
        recordList.clear();
        notifyDataSetChanged();
    }

    public void setAllPending() {
        for (int position = 0; position < recordList.size(); position++) {
            // only make idling to be pending
            if (recordList.get(position).status == Status.IDLING) {
                recordList.get(position).status = Status.PENDING;
                notifyItemChanged(position);
            }
        }
    }

    public void setAllIdling() {
        Status tempStatus;

        for (int position = 0; position < recordList.size(); position++) {
            tempStatus = recordList.get(position).status;

            // cancel pending as well as uploading
            if (tempStatus == Status.PENDING || tempStatus == Status.UPLOADING) {
                recordList.get(position).status = Status.IDLING;
                notifyItemChanged(position);
            }
        }
    }

    // selection sort
    public void sortRecordsBy(int sortingAction) {
        // no need to sort when list has less than or equal to 1 element
        if (recordList.size() <= 1) {
            return;
        }

        for (int startPosition = 0; startPosition < recordList.size(); startPosition++) {
            int maxValPosition = startPosition;
            for (int currPosition = startPosition + 1; currPosition < recordList.size(); currPosition++) {
                switch (sortingAction) {
                    case SORT_BY_DATE:
                        if (recordList.get(maxValPosition).imageRecord.getCreatedDate()
                                < recordList.get(currPosition).imageRecord.getCreatedDate()) {
                            maxValPosition = currPosition;
                        }
                        break;

                    case SORT_BY_ESTIMATE:
                        if (recordList.get(maxValPosition).imageRecord.getEstimate()
                                < recordList.get(currPosition).imageRecord.getEstimate()) {
                            maxValPosition = currPosition;
                        }
                        break;
                }
            }
            // remove then add to avoid misorder
            Record tempRecord = recordList.get(maxValPosition);
            recordList.remove(maxValPosition);
            recordList.add(startPosition, tempRecord);
            notifyItemMoved(maxValPosition, startPosition);
        }
    }

    public void groupRecordsBy(int groupingAction) {
        switch (groupingAction) {
            case GROUP_BY_ALL:
                for (Record record : recordList) {
                   record.show = true;
                }
                break;

            case GROUP_BY_ESTIMATED:
                for (Record record : recordList) {
                    record.show = (record.status == Status.FINISHED);
                }
                break;

            case GROUP_BY_NOT_ESTIMATED:
                for (Record record : recordList) {
                    record.show = (record.status != Status.FINISHED);
                }
                break;
        }

        for (int startPosition = 0; startPosition < recordList.size(); startPosition++) {
            if (recordList.get(startPosition).show) {
                continue;
            }

            for (int currPosition = startPosition + 1; currPosition < recordList.size(); currPosition++) {
                if (recordList.get(currPosition).show) {
                    // remove then add to avoid misorder
                    Record tempRecord = recordList.get(currPosition);
                    recordList.remove(currPosition);
                    recordList.add(startPosition, tempRecord);
                    notifyItemMoved(currPosition, startPosition);
                    break;
                }
            }
        }
    }
}
