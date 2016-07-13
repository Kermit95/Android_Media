package io.github.kermit95.android_media.audiodemo;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.todoroo.aacenc.AACEncoder;
import com.todoroo.aacenc.AACToM4A;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Created by kermit on 16/7/12.
 *
 */
public class AudioWorker {

    private static final String TAG = "AudioWorker";

    public static final String CONVERTED_OUT_PUT_PATH_M4A =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestRecord/toM4A.m4a";

    public static final String CONVERTED_OUT_PUT_PATH_AAC =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestRecord/toAAC.aac";

    public static final String CONVERTED_OUT_PUT_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestRecord";

    // audio configuration
    private final int samplerate = 16000;
    private final int audioSource = MediaRecorder.AudioSource.MIC;
    private final int channelIn = AudioFormat.CHANNEL_IN_STEREO;
    private final int channelOut = AudioFormat.CHANNEL_OUT_STEREO;
    private final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    // flag
    private boolean isRecording = false;
    private boolean isRecordPause = false;

    // Audio
    private AudioTrack audioTrack;
    private AudioRecord audioRecord;

    private int inBufferSize;
    private int outBufferSize;

    private Context mContext;


    // constructor
    // samplerate = 44100, 16000, 11025

    public AudioWorker(Context context){
        this.mContext = context;
    }

    private void initAudioRecorder(){
        // bufferSize = samplerate x bit-width x 采样时间 x channel_count
        inBufferSize = AudioRecord.getMinBufferSize(samplerate, channelIn, audioEncoding);
        audioRecord = new AudioRecord(
                audioSource,
                samplerate,
                channelIn,
                audioEncoding,
                inBufferSize);
    }

    private void initAudioTrack(){
        outBufferSize = AudioRecord.getMinBufferSize(samplerate, channelOut, audioEncoding);
        audioTrack = new AudioTrack(
                audioSource,
                samplerate,
                channelOut,
                audioEncoding,
                outBufferSize,
                AudioTrack.MODE_STREAM);
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

            if (audioRecord == null){
                initAudioRecorder();
            }

            try {
                byte[] buffer = new byte[outBufferSize/4];

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

    private class PlayTask extends AsyncTask<File, Integer, Void> {

        @Override
        protected Void doInBackground(File... params) {

            if (audioTrack == null){
                initAudioTrack();
            }

            DataInputStream dis;

            try {

                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(params[0])));

                byte[] audiodata = new byte[inBufferSize/4];

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


    private class ConvertTask extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {

//            MediaFormat format = getAudioFormat(params[0]);

//            int bitRate = format.getInteger(MediaFormat.KEY_BIT_RATE);
//            int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
//            int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

            /*
             INPUT: PCM Bitrate = 44100(Hz) x 16(bit) x 1(Monoral) = 705600 bit/s
             OUTPUT: AAC-HE Bitrate = 64 x 1024(bit) = 65536 bit/s/
            */
            int bitrate =  samplerate * 16 * 1;

            try {
//                final AACEncoder aacEncoder = new AACEncoder(bitRate, sampleRate, channelCount);

                // My AACEncoder
//                final AACEncoder aacEncoder = new AACEncoder(16000, 11025, 1);
//
//                aacEncoder.setOutputPath(CONVERTED_OUT_PUT_PATH + "/converted.m4a");
//                aacEncoder.prepare();
//
//                InputStream inputStream = new FileInputStream(params[0]);
//
//                aacEncoder.encode(inputStream, 11025);
//
//                aacEncoder.stop();
//
//                inputStream.close();

                /** 以下使用android-aac-encoder **/
                // get byte array
                //读取录制的pcm音频文件
                DataInputStream mDataInputStream = new DataInputStream(new FileInputStream(params[0]));

                byte[] buffer = new byte[(int) new File(params[0]).length()];

                mDataInputStream.read(buffer);

                AACEncoder aacEncoder = new AACEncoder();

                // PCM -> AAC
                aacEncoder.init(32000, 2, samplerate, 16, CONVERTED_OUT_PUT_PATH_AAC);
                aacEncoder.encode(buffer);
                aacEncoder.uninit();

                // AAC -> M4A
                new AACToM4A().convert(mContext, CONVERTED_OUT_PUT_PATH_AAC, CONVERTED_OUT_PUT_PATH_M4A);

                mDataInputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(mContext, "Finish!", Toast.LENGTH_SHORT).show();
        }
    }

    // Audio 方法
    public void play(File targetFile){
        new PlayTask().execute(targetFile);
    }

    public void stopPlay(){
        audioTrack.stop();
    }

    public void releasePlayer(){
        if (audioTrack != null){
            if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
                    || audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED){
                audioTrack.stop();
            }
            audioTrack.release();
        }
    }

    public void releaseRecorder(){
        if (audioRecord != null){
            if (isRecording){
                audioRecord.stop();
            }
            audioRecord.release();
        }
    }

    public void record(File targetFile){
        new RecordTask().execute(targetFile);
    }

    public void stopRecord(){
        isRecording = false;
    }

    public void pauseRecord(){
        isRecordPause = true;
    }

    public void resumeRecord(){
        isRecordPause = false;
    }

    public void convertToM4A(String filePath){
        new ConvertTask().execute(filePath);
    }






    /** 以下的方法会触发异常 **/

    // This method will throw java.lang.IllegalStateException
    // the possible reason is WVMExtractor: Failed to open libwvm.so: dlopen failed: library "libwvm.so" not found
    private MediaFormat getAudioFormat(String filePath){

        MediaExtractor mediaExtractor;
        MediaFormat format = null;

        try {
            //此类可分离视频文件的音轨和视频轨道
            mediaExtractor = new MediaExtractor();
            //媒体文件的位置
            mediaExtractor.setDataSource(filePath);

            //遍历媒体轨道, 此处传入的是音频文件，所以也就只有一条轨道
            Log.e(TAG, "TrackCount: " + mediaExtractor.getTrackCount());
            for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                format = mediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);

                // 如果MIME类型是audio, 即音频就提取轨道
                if (mime.startsWith("audio")) {
                    // 获取音频轨道
                    // format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 200 * 1024);
                    // 选择此音频轨道
                    mediaExtractor.selectTrack(i);
                    format = mediaExtractor.getTrackFormat(i);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return format;
    }
}
