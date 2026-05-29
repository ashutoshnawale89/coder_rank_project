package com.code.rank.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class StreamGobbler implements Runnable {

    private static final int MAX_BYTES = 256 * 1024;

    private final InputStream stream;
    private final StringBuilder buffer = new StringBuilder();
    private volatile boolean truncated = false;

    StreamGobbler(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            char[] chunk = new char[4096];
            int read;
            while ((read = reader.read(chunk)) != -1) {
                if (buffer.length() >= MAX_BYTES) {
                    truncated = true;
                    continue;
                }
                int remaining = MAX_BYTES - buffer.length();
                buffer.append(chunk, 0, Math.min(read, remaining));
            }
        } catch (IOException ignored) {
        }
    }

    String getOutput() {
        return truncated ? buffer + "\n... [output truncated]" : buffer.toString();
    }
}
