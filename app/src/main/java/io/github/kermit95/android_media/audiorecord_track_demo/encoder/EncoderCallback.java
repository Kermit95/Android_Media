package io.github.kermit95.android_media.audiorecord_track_demo.encoder;

/**
 * Created by kermit on 16/7/13.
 */

public interface EncoderCallback {

    void onStart();

    void onProgress(String msg);

    void onFinish();

}
