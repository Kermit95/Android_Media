package io.github.kermit95.android_media.audiorecord_track_demo;

import java.io.IOException;

import io.github.kermit95.android_media.audiorecord_track_demo.encoder.EncoderCallback;

/**
 * Created by kermit on 16/7/13.
 */

public interface OhMyEncoder {

    void prepare(String inputPath, String outputPath, EncoderCallback callback);

    void encode();

    void relase();
}
