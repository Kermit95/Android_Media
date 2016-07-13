package io.github.kermit95.android_media.refractor_and;

import java.io.File;

/**
 * Created by kermit on 16/7/13.
 */

public interface OhMyRecorder {

    /**
     * initialize
     */
    void prepare(File tagetFile);

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
