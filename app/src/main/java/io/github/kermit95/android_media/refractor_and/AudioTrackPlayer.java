package io.github.kermit95.android_media.refractor_and;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by kermit on 16/7/13.
 */

public class AudioTrackPlayer implements OhMyPlayer{

    // audio track
    private AudioTrack audioTrack;
    private int outBufferSize;

    // targetfile
    private File targetFile;

    @Override
    public void prepare(File tagetFile) {
        outBufferSize = AudioRecord.getMinBufferSize(
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_OUT,
                AudioConfig.AUDIO_ENCODING);

        audioTrack = new AudioTrack(
                AudioConfig.AUDIO_SOURCE,
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_OUT,
                AudioConfig.AUDIO_ENCODING,
                outBufferSize,
                AudioTrack.MODE_STREAM);

        this.targetFile = tagetFile;
    }

    @Override
    public void play() {
        new PlayTask().execute(targetFile);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop() {
        audioTrack.stop();
    }

    @Override
    public void release() {
        if (audioTrack != null){
            if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED){
                audioTrack.stop();
            }
            audioTrack.release();
        }
    }

    private class PlayTask extends AsyncTask<File, Integer, Void> {

        @Override
        protected Void doInBackground(File... params) {

            DataInputStream dis;

            try {

                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(params[0])));

                byte[] audiodata = new byte[outBufferSize/4];

                audioTrack.play();

                while (AudioTrack.PLAYSTATE_PLAYING == audioTrack.getPlayState()) {
                    int i = 0;
                    while (i < audiodata.length) {
                        audiodata[i] = dis.readByte();
                        ++i;
                    }
                    audioTrack.write(audiodata, 0, audiodata.length);
                }

                dis.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
