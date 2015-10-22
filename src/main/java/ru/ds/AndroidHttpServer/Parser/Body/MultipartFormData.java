package ru.ds.AndroidHttpServer.Parser.Body;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import ru.ds.AndroidHttpServer.HttpRequest;
import ru.ds.AndroidHttpServer.Parser.HeaderParser.ContentType;

/**
 * class provided data of the Form, recieved in request
 */
public class MultipartFormData extends FormData {

    private static final String TAG = "MultipartFormData";
    private enum ProcessingState {START_LINE, STOP_LINE, CONTINUE }

    /**
     * Parse the multipart form body
     * @param reader BufferedReader of the Socket
     * @param requestedSize value of Content-Length header
     */
    public MultipartFormData(InputStream inputStream, ContentType contentTypeHeader) {
        super(null, 0);
        String boundary = contentTypeHeader.getBoundary();
        if (boundary == null || boundary.isEmpty()) {
            return;
        }
        while (searchNextMultipart(inputStream, boundary)) {
            ArrayList<String> newMultipartArray = readMultipart(inputStream, boundary);
            if (!newMultipartArray.isEmpty()) {
                MultipartValue multipartValue = new MultipartValue(newMultipartArray);
                if (multipartValue.isValid()) {
                    putValue(multipartValue.getPartKey(), multipartValue);
                } else {
                    Log.e(TAG, "Invalid multipart");
                }
            }
        }
    }

    /**
     * get MultipartValue from hash
     * @param key key of the part
     * @return value
     */
    public MultipartValue getValue(String key) {
        Object value = getObject(key.toLowerCase());
        if (value == null) {
            return null;
        }
        if (value instanceof MultipartValue) {
            return (MultipartValue) value;
        }
        return null;
    }

    /**
     * read lines from the buffer while not find the boundary string
     */
    private boolean searchNextMultipart(InputStream inputStream, String boundary) {
        if (boundary == null || boundary.isEmpty()) {
            return false;
        }

        final String startBoundery = "--"+boundary;
        final String endBoundary   =startBoundery + "--";

        return processLines(inputStream, new LineProcessor() {
            @Override
            public ProcessingState ProcessLine(String line) {
                String l = null;
                if (line != null) {
                    l = line.toLowerCase();
                }

                if (line == null) {
                    return ProcessingState.STOP_LINE;
                } else {
                    if (endBoundary.equals(l)) {
                        return ProcessingState.STOP_LINE;
                    } else if (startBoundery.equals(l)) {
                        return ProcessingState.START_LINE;
                    }
                }
                return ProcessingState.CONTINUE;
            }
        });
    }

    private ArrayList<String> readMultipart(InputStream inputStream, final String boundary) {
        final ArrayList partStrings = new ArrayList<String>();
        final boolean[] emptyLineSkipped    = {false};

        final String startBoundery = "--" + boundary;
        final String endBoundary   =startBoundery + "--";

        processLines(inputStream, new LineProcessor() {
            @Override
            public ProcessingState ProcessLine(String line) {

                String l = null;
                if (line != null) {
                    l = line.toLowerCase();
                }
                if (l == null) {
                    return ProcessingState.STOP_LINE;
                }

                if (l.isEmpty()) {
                    if (emptyLineSkipped[0]) {
                        return ProcessingState.STOP_LINE;
                    } else {
                        emptyLineSkipped[0] = true;
                    }
                }

                if (l.equals(startBoundery)) {
                    MultipartValue multipartValue = new MultipartValue(partStrings);
                    if (multipartValue.isValid()) {
                        putValue(multipartValue.getPartKey(), multipartValue);
                    } else {
                        Log.e(TAG, "Invalid multipart");
                    }
                    emptyLineSkipped[0] = false;
                    partStrings.clear();
                    return ProcessingState.CONTINUE;
                } else if (l.equals(endBoundary)) {
                    return ProcessingState.STOP_LINE;
                }
                partStrings.add(line);
                return ProcessingState.CONTINUE;
            }
        });
        return partStrings;
    }

    private boolean processLines(InputStream inputStream, LineProcessor processor) {
        if (processor == null) {
            return false;
        }
        boolean result = false;
        while (true) {
            String line = null;
            line = HttpRequest.HttpRequestBuilder.readStringFromBuffer(inputStream);
            ProcessingState state = processor.ProcessLine(line == null ? line : line.trim());
            if (state == ProcessingState.STOP_LINE) {
                result = false;
            } else if (state == ProcessingState.START_LINE) {
                result = true;
            }
            if (state != ProcessingState.CONTINUE) {
                break;
            }
        }
        return result;
    }

    private interface LineProcessor {
        ProcessingState ProcessLine(String line);
    }
}
