package io.github.kermit95.android_media.finally_audiorecord_track_demo;

import io.github.kermit95.android_media.finally_audiorecord_track_demo.recorder.RecorderCallback;

/**
 * Created by kermit on 16/7/13.
 */

public interface OhMyRecorder {

    /**
     * initialize
     */
    void prepare(String targetPath, RecorderCallback callback);

    /**
     * record
     */
    void record();

    /**
     * pause record
     */
    void pause();

    /**
     * resume record
     */
    void resume();

    /**
     * stop and save
     */
    void stop();

    /**
     * release
     */

    void release();

}
