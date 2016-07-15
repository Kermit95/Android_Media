package io.github.kermit95.android_media.finally_audiorecord_track_demo.encoder;

import io.github.kermit95.android_media.finally_audiorecord_track_demo.AudioConfig;

/**
 * Created by kermit on 16/7/11.
 */

public class FFmpegParam {

    // -f s16le -ar 44100 -ac 2 -i test.pcm -acodec aac -strict experimental test.aac
    public static final String FF_PREFIX = "-f s16le -ar " + AudioConfig.SAMPLE_RATE + " -ac 2 -y -i ";
    public static final String FF_SUFFIX = " -acodec aac -strict experimental ";

}
