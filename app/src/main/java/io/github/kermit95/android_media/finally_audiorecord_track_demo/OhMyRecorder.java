package io.github.kermit95.android_media.finally_audiorecord_track_demo;

import io.github.kermit95.android_media.finally_audiorecord_track_demo.recorder.RecorderCallback;

/**
 * Created by kermit on 16/7/13.
 */

public interface OhMyRecorder {

    void prepare(String targetPath, RecorderCallback callback);

    void record();

    void pause();

    void stop();

    void release();

}
