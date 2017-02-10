package com.example.peter.berryestimator;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.BoringLayout;

import java.util.Date;

public class ImageRecord implements Parcelable{

    private String compressedThumbnailString;
    private String imagePath;
    private String recordId; // GPS + Date time
    private String title;
    private String imageLocation;
    private String targetType;
    // TODO: add GPS

    private long createdDate;
    private long imageTakenDate;

    private int actualCount;
    private int estimate;
    private int estimateError;

    private boolean isSynced;

//    private Status status;
//    private int progressPercentage;

    // initialize values
    public ImageRecord(){
        setCompressedThumbnailString("");
        setImagePath("");
        setRecordId("");
        setTitle("");
        setImageLocation("");
        setTargetType("");
        setCreatedDate(new Date().getTime());
        setImageTakenDate(-1);

        setActualCount(-1);
        setEstimate(-1);
        setEstimateError(0);

        setIsSynced(false);

//        setStatus(Status.CREATED);
//        setProgressPercentage(0);
    }

    public ImageRecord(ImageRecord imageRecord) {
        setCompressedThumbnailString(imageRecord.getCompressedThumbnailString());
        setImagePath(imageRecord.getImagePath());
        setRecordId(imageRecord.getRecordId());
        setTitle(imageRecord.getTitle());
        setImageLocation(imageRecord.getImageLocation());
        setTargetType(imageRecord.getTargetType());
        setCreatedDate(imageRecord.getCreatedDate());
        setImageTakenDate(imageRecord.getImageTakenDate());

        setActualCount(imageRecord.getActualCount());
        setEstimate(imageRecord.getEstimate());
        setEstimateError(imageRecord.getEstimateError());

        setIsSynced(imageRecord.isSynced());

//        setStatus(imageRecord.getStatus());
//        setProgressPercentage(imageRecord.getProgressPercentage());
    }

    /**
     * implement the override parcelable functions
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // the order of writing operations should be fixed
        dest.writeString(compressedThumbnailString);

        dest.writeString(imagePath);
        dest.writeString(recordId);
        dest.writeString(title);
        dest.writeString(imageLocation);
        dest.writeString(targetType);

        dest.writeLong(createdDate);
        dest.writeLong(imageTakenDate);

        dest.writeInt(actualCount);
        dest.writeInt(estimate);
        dest.writeInt(estimateError);

        dest.writeInt(isSynced? 1 : 0);

//        dest.writeSerializable(status);
//        dest.writeInt(progressPercentage);
    }

    public static final Parcelable.Creator<ImageRecord> CREATOR =
            new Parcelable.Creator<ImageRecord>() {
                public ImageRecord createFromParcel(Parcel in) {
                    ImageRecord imageRecord = new ImageRecord();

                    imageRecord.compressedThumbnailString = in.readString();

                    imageRecord.imagePath = in.readString();
                    imageRecord.recordId = in.readString();
                    imageRecord.title = in.readString();
                    imageRecord.imageLocation = in.readString();
                    imageRecord.targetType = in.readString();

                    imageRecord.createdDate = in.readLong();
                    imageRecord.imageTakenDate = in.readLong();

                    imageRecord.actualCount = in.readInt();
                    imageRecord.estimate = in.readInt();
                    imageRecord.estimateError = in.readInt();

                    imageRecord.isSynced = (in.readInt() != 0);

//                    imageRecord.status = (Status)(in.readSerializable());
//                    imageRecord.progressPercentage = in.readInt();

                    return imageRecord;
                }

                @Override
                public ImageRecord[] newArray(int size) {
                    return new ImageRecord[size];
                }
            };

    /**
     * all getters and setters
     */

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
    }

    public long getImageTakenDate() {
        return imageTakenDate;
    }

    public void setImageTakenDate(long imageTakenDate) {
        this.imageTakenDate = imageTakenDate;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public int getActualCount() {
        return actualCount;
    }

    public void setActualCount(int actualCount) {
        this.actualCount = actualCount;
    }

    public int getEstimate() {
        return estimate;
    }

    public void setEstimate(int estimate) {
        this.estimate = estimate;
    }

    public int getEstimateError() {
        return estimateError;
    }

    public void setEstimateError(int estimateError) {
        this.estimateError = estimateError;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

//    public Status getStatus() {
//        return status;
//    }
//
//    public void setStatus(Status status) {
//        this.status = status;
//    }
//
//    public int getProgressPercentage() {
//        return progressPercentage;
//    }
//
//    public void setProgressPercentage(int progressPercentage) {
//        this.progressPercentage = progressPercentage;
//    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getCompressedThumbnailString() {
        return compressedThumbnailString;
    }

    public void setCompressedThumbnailString(String imageCompressedThumbnail) {
        this.compressedThumbnailString = imageCompressedThumbnail;
    }
}
