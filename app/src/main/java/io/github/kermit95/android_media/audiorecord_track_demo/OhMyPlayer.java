package io.github.kermit95.android_media.audiorecord_track_demo;

/**
 * Created by kermit on 16/7/13.
 */

public interface OhMyPlayer {

    /**
     * initialize
     */
    void prepare(String tagetPath);

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
