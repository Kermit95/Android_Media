package io.github.kermit95.android_media.audiorecord_track_demo;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Environment;

/**
 * Created by kermit on 16/7/13.
 */

public class AudioConfig {

    public static final String CONVERTED_OUT_PUT_PATH_M4A =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestRecord/toM4A.m4a";

    public static final String CONVERTED_OUT_PUT_PATH_AAC =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestRecord/toAAC.aac";

    public static final String CONVERTED_OUT_PUT_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestRecord";


    /**
     * INPUT: PCM Bitrate = 44100(Hz) x 16(bit) x 1(Monoral) = 705600 bit/s
     * OUTPUT: AAC-HE Bitrate = 64 x 1024(bit) = 65536 bit/s/
     *
     * 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050,16000,11025
     * 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
     * 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
     *
     */
    // audio configuration
    public static final int SAMPLE_RATE = 16000;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int CHANNEL_IN = AudioFormat.CHANNEL_IN_STEREO;
    public static final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_STEREO;
    public static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int CHANNEL_COUNT = 2;
    public static final int BITRATE = 32000;


//    public static final int SAMPLE_RATE = 44100;
//    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
//    public static final int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
//    public static final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
//    public static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
//    public static final int CHANNEL_COUNT = 1;
//    public static final int BITRATE = 705600;
}
