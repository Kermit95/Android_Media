package io.github.kermit95.android_media.refractor_and;

import android.media.AudioRecord;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * Created by kermit on 16/7/13.
 */

public class AudioRecordRecorder implements OhMyRecorder {

    // audiorecord
    private AudioRecord audioRecord;
    private int inBufferSize;

    private File targetFile;

    // flag
    private boolean isRecording = false;
    private boolean isRecordPause = false;


    @Override
    public void prepare(File tagetFile) {

        // bufferSize = samplerate x bit-width x 采样时间 x channel_count
        inBufferSize = AudioRecord.getMinBufferSize(
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_IN,
                AudioConfig.AUDIO_ENCODING);

        audioRecord = new AudioRecord(
                AudioConfig.AUDIO_SOURCE,
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_IN,
                AudioConfig.AUDIO_ENCODING,
                inBufferSize);

        this.targetFile = tagetFile;
    }

    @Override
    public void record() {
        new RecordTask().execute(targetFile);
    }

    @Override
    public void pause() {
        isRecordPause = true;
    }

    @Override
    public void resume() {
        isRecordPause = false;
    }

    @Override
    public void stop() {
        isRecording = false;
    }


    @Override
    public void release() {
        if (audioRecord != null){
            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED){
                audioRecord.stop();
            }
            audioRecord.release();
        }
    }

    private class RecordTask extends AsyncTask<File, Integer, Void> {

        @Override
        protected Void doInBackground(File... params) {

            RandomAccessFile randomAccessFile = null;

            try {
                randomAccessFile = new RandomAccessFile(params[0], "rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (randomAccessFile == null){
                return null;
            }

            try {
                byte[] buffer = new byte[inBufferSize/4];

                audioRecord.startRecording();

                isRecording = true;

                while(isRecording) {
                    if (!isRecordPause) {
                        audioRecord.read(buffer, 0, buffer.length);

                        //向原文件中追加内容
                        randomAccessFile.seek(randomAccessFile.length());
                        randomAccessFile.write(buffer, 0, buffer.length);
                    }
                }

                audioRecord.stop();
                randomAccessFile.close();

            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }
    }


}
