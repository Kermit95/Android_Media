package io.github.kermit95.android_media.finally_audiorecord_track_demo.bean;

/**
 * Created by kermit on 16/7/16.
 */

public class RecordSound {

    private String filePath;

    // 单位: 秒
    private int timeLength;


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getTimeLength() {
        return timeLength;
    }

    public void setTimeLength(int timeLength) {
        this.timeLength = timeLength;
    }
}
