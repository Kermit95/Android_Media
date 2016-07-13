package io.github.kermit95.android_media.refractor_and;

import java.io.File;

/**
 * Created by kermit on 16/7/13.
 */

public interface OhMyEncoder {

    void prepare(File inputFile, File outputFile);

    void encode();

    void relase();
}
