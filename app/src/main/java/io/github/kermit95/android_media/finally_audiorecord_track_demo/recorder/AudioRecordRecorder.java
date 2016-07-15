package io.github.kermit95.android_media.finally_audiorecord_track_demo.recorder;

import android.media.AudioRecord;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import io.github.kermit95.android_media.finally_audiorecord_track_demo.AudioConfig;
import io.github.kermit95.android_media.finally_audiorecord_track_demo.OhMyRecorder;

/**
 * Created by kermit on 16/7/13.
 */

public class AudioRecordRecorder implements OhMyRecorder {

    // audiorecord
    private AudioRecord audioRecord;
    private int inBufferSize;

    private String targetPath;

    // flag
    private boolean isRecording = false;
    private boolean isRecordPause = false;

    private RecorderCallback mCallback;


    @Override
    public void prepare(String targetPath, RecorderCallback callback) {

        this.mCallback = callback;

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

        this.targetPath = targetPath;
    }

    @Override
    public void record() {
        new RecordTask().execute(targetPath);
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

    private class RecordTask extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {

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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mCallback.onFinish();
        }
    }


}
