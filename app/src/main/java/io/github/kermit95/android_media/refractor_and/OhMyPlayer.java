package io.github.kermit95.android_media.refractor_and;

import java.io.File;

/**
 * Created by kermit on 16/7/13.
 */

public interface OhMyPlayer {

    /**
     * initialize
     */
    void prepare(File tagetFile);

    /**
     * play audio
     */
    void play();

    /**
     * pause
     */
    void pause();

    /**
     * resume
     */
    void resume();

    /**
     * stop
     */
    void stop();

    /**
     * relase
     */

    void release();

}
