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
import java.util.HashMap;
import java.util.List;

public class ProductGenericsInfo {
    private HashMap<String, String> genericUpcByCode = new HashMap();

    public ProductGenericsInfo(File csvFile) {
        if (!csvFile.exists()) {
            throw new IllegalArgumentException("Cannot read from non-existent file: " + csvFile.getAbsolutePath());
        }
        this.parseFile(csvFile);
    }

    private final void parseFile(File file) {
        List<String> lines = null;
        try {
            lines = this.getCSVLines(file);
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for (String line : lines) {
            String[] bits = line.split(",", -1);
            String code = bits[0];
            String generic = bits[1];
            this.genericUpcByCode.put(code.trim().toUpperCase(), "088989" + generic);
        }
    }

    public boolean hasGenericUPC(String code) {
        return this.genericUpcByCode.containsKey(code.toUpperCase());
    }

    public String getGenericUpc(String code) {
        return this.genericUpcByCode.get(code.toUpperCase());
    }

    private List<String> getCSVLines(File file) throws IOException {
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
}

