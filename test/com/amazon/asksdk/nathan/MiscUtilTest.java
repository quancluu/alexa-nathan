package com.amazon.asksdk.nathan;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MiscUtilTest {

    @Test
    public void testReadDataFromFileName() throws Exception {

    }

    @Test
    public void testReadLinesFromFileName() throws Exception {
        final List<String> lines = MiscUtil.ReadLinesFromFileName("/company-symbol-name.csv");
        System.out.println("lines=" + lines);
    }


}