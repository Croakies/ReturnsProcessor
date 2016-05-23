/*
 * Decompiled with CFR 0_114.
 */
package net.shadowmage.returnprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static final List<String> getCSVLines(File file) throws IOException {
        String line;
        ArrayList<String> lines = new ArrayList<String>();
        FileInputStream fis = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) continue;
            lines.add(line);
        }
        reader.close();
        fis.close();
        return lines;
    }

    public static final String[] parseCsvLine(String line) {
        return line.split(",", -1);
    }
}

