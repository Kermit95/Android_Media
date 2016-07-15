package io.github.kermit95.android_media.finally_audiorecord_track_demo.encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import io.github.kermit95.android_media.finally_audiorecord_track_demo.AudioConfig;
import io.github.kermit95.android_media.finally_audiorecord_track_demo.OhMyEncoder;

/**
 * Created by kermit on 16/7/14.
 */

public class MediaCodecEncoder implements OhMyEncoder {

    private static final String TAG = "MediaCodecEncoder";

    private static final String COMPRESSED_AUDIO_FILE_MIME_TYPE = "audio/mp4a-latm";

    private static final int CODEC_TIMEOUT = 5000;

    private MediaFormat mediaFormat;
    private MediaCodec mediaCodec;
    private MediaMuxer mediaMuxer;
    private ByteBuffer[] codecInputBuffers;
    private ByteBuffer[] codecOutputBuffers;
    private MediaCodec.BufferInfo bufferInfo;

    private int audioTrackId;
    private int totalBytesRead;
    private double presentationTimeUs;

    private String inputPath;

    private EncoderCallback mCallback;


    public MediaCodecEncoder(){
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void prepare(String inputPath, String outputPath, EncoderCallback callback) {

        this.inputPath = inputPath;
        this.mCallback = callback;

        try {
            mediaFormat = MediaFormat.createAudioFormat(COMPRESSED_AUDIO_FILE_MIME_TYPE, AudioConfig.SAMPLE_RATE, AudioConfig.CHANNEL_COUNT);
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, AudioConfig.BITRATE);

            // init MediaCodec
            mediaCodec = MediaCodec.createEncoderByType(COMPRESSED_AUDIO_FILE_MIME_TYPE);
            // configure
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();

            codecInputBuffers = mediaCodec.getInputBuffers();
            codecOutputBuffers = mediaCodec.getOutputBuffers();

            bufferInfo = new MediaCodec.BufferInfo();

            mediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            totalBytesRead = 0;
            presentationTimeUs = 0;
        } catch (IOException e) {
            Log.e(TAG, "Exception while initializing PCMEncoder", e);
        }
    }

    @Override
    public void encode() {
        new EncoderTask().execute();
    }

    private class EncoderTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void...params) {
            Log.d(TAG, "Starting encoding of InputStream");
            byte[] tempBuffer = new byte[2 * AudioConfig.SAMPLE_RATE];
            boolean hasMoreData = true;
            boolean stop = false;

            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(inputPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            while (!stop) {
                int inputBufferIndex = 0;
                int currentBatchRead = 0;
                while (inputBufferIndex != -1 && hasMoreData && currentBatchRead <= 50 * AudioConfig.SAMPLE_RATE) {
                    inputBufferIndex = mediaCodec.dequeueInputBuffer(CODEC_TIMEOUT);

                    if (inputBufferIndex >= 0) {
                        ByteBuffer buffer = codecInputBuffers[inputBufferIndex];
                        buffer.clear();

                        int bytesRead = 0;
                        try {
                            bytesRead = inputStream.read(tempBuffer, 0, buffer.limit());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (bytesRead == -1) {
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, (long) presentationTimeUs, 0);
                            hasMoreData = false;
                            stop = true;
                        } else {
                            totalBytesRead += bytesRead;
                            currentBatchRead += bytesRead;
                            buffer.put(tempBuffer, 0, bytesRead);
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, bytesRead, (long) presentationTimeUs, 0);
                            presentationTimeUs = 1000000L * (totalBytesRead / 2) / AudioConfig.SAMPLE_RATE;
                        }
                    }
                }

                int outputBufferIndex = 0;
                while (outputBufferIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, CODEC_TIMEOUT);
                    if (outputBufferIndex >= 0) {
                        ByteBuffer encodedData = codecOutputBuffers[outputBufferIndex];
                        encodedData.position(bufferInfo.offset);
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);

                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0 && bufferInfo.size != 0) {
                            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        } else {
                            mediaMuxer.writeSampleData(audioTrackId, codecOutputBuffers[outputBufferIndex], bufferInfo);
                            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        }
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        mediaFormat = mediaCodec.getOutputFormat();
                        audioTrackId = mediaMuxer.addTrack(mediaFormat);
                        mediaMuxer.start();
                    }
                }
            }

            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Finished encoding of InputStream");

            // 释放缓冲区
            mediaCodec.stop();
            mediaCodec.release();
            mediaMuxer.stop();
            mediaMuxer.release();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mCallback.onFinish();
        }
    }

    @Override
    public void relase() {
    }
}
