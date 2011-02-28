package com.ociweb.io;

import java.io.IOException;
import java.io.Reader;

/**
 * Utility methods for working with files and streams.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
public class IOUtil {

    /**
     * Instances of this class cannot be created.
     */
    private IOUtil() {}

    /**
     * Closes a Reader.
     * It does nothing if null is passed.
     * If an exception is thrown during closing,
     * it is rethrown as a RuntimeException.
     * @param reader the Reader
     */
    public static void close(Reader reader) {
        if (reader == null) return;
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}