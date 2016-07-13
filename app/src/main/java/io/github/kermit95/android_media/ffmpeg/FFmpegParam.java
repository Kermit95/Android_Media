package io.github.kermit95.android_media.ffmpeg;

/**
 * Created by kermit on 16/7/11.
 */

public class FFmpegParam {

    // 1. -f s16le -ar 44.1k -ac 2 -i input_file.pcm output_file.wav
    // 2. -f s16le -ar 44100 -ac 2 -i test.pcm -acodec aac -strict experimental test.aac
    public static final String FF_PREFIX = "-f s16le -ar 11025 -ac 1 -y -i ";
    public static final String FF_SUFFIX = " -acodec aac -strict experimental ";


    // 3.-ar 8000 -ac 1 -f alaw -i test.pcm  -acodec libmp3lame -ac 1 -ab 128k 1.mp3
    public static final String FF_M_PREFIX = "-ar 8000 -ac 1 -f alaw -y -i ";
    public static final String FF_M_SUFFIX = " -acodec libmp3lame -ac 1 -ab 128k ";

}
