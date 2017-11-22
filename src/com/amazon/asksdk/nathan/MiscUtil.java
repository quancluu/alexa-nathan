package com.amazon.asksdk.nathan;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by qcluu on 11/19/17.
 */
public class MiscUtil {

    static public final String ReadDataFromFileName(final String fileName) {
       return new MiscUtil().readDataFromFile(fileName);
    }

    static public final List<String> ReadLinesFromFileName(final String fileName) {
        return new MiscUtil().readLinesFromFile(fileName);
    }

    private String readDataFromFile(final String fileName) {
        try {
            final InputStream inputStream = getClass().getResourceAsStream(fileName);
            final String data = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    private List<String> readLinesFromFile(final String fileName) {
        try {
            final InputStream inputStream = getClass().getResourceAsStream(fileName);
            return IOUtils.readLines(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
