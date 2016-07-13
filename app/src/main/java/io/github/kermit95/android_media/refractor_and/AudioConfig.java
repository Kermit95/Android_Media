package io.github.kermit95.android_media.refractor_and;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;

/**
 * Created by kermit on 16/7/13.
 */

public class AudioConfig {

    static final String CONVERTED_OUT_PUT_PATH_M4A =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestRecord/toM4A.m4a";

    static final String CONVERTED_OUT_PUT_PATH_AAC =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestRecord/toAAC.aac";

    static final String CONVERTED_OUT_PUT_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestRecord";

    // audio configuration
    static final int SAMPLE_RATE = 16000;
    static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    static final int CHANNEL_IN = AudioFormat.CHANNEL_IN_STEREO;
    static final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_STEREO;
    static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

}
