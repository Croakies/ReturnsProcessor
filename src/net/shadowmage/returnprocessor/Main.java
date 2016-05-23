/*
 * Decompiled with CFR 0_114.
 */
package net.shadowmage.returnprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import net.shadowmage.returnprocessor.Gui;
import net.shadowmage.returnprocessor.ProductCounts;
import net.shadowmage.returnprocessor.ProductDatabase;
import net.shadowmage.returnprocessor.ProductGenericsInfo;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Main {
    public static ProductDatabase db;
    public static ProductGenericsInfo genDb;
    public static ProductCounts countsDb;
    private static int rowNum;
    public static Gui gui;
    public static Properties config;
    protected static File remoteExcelFile;
    protected static File localExcelFile;
    protected static File configFile;
    protected static File localCSVFile;

    static {
        rowNum = 0;
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    public static void main(String[] args) throws InvalidFormatException, IOException {
        Main.config = new Properties();
        Main.configFile = new File("resources/config.cfg");
        Main.localCSVFile = new File("resources/upccodes.csv");
        Main.localExcelFile = new File("resources/upccodes.xlsx");
        Main.remoteExcelFile = new File("\\\\192.168.17.107\\Cust Service\\UPC Codes\\upccodes.xlsx");
        if (!Main.configFile.exists()) {
            Main.configFile.createNewFile();
        }
        fis = new FileInputStream(Main.configFile);
        Main.config.load(fis);
        fis.close();
        Main.gui = new Gui();
        try {
            Main.loadWorkbook();
        }
        catch (Exception e) {
            Main.gui.log("Caught exception while reading rowNum: " + Main.rowNum);
            var6_4 = e.getStackTrace();
            var5_5 = var6_4.length;
            var4_6 = 0;
            ** GOTO lbl33
        }
        Main.gui.log("saving config file...");
        Main.configFile.createNewFile();
        fos = new FileOutputStream(Main.configFile);
        Main.config.store(fos, "Returns Processor Configuration File");
        fos.close();
        Main.countsDb = new ProductCounts();
        Main.gui.addElements();
        Main.gui.mainUpdateLoop();
        return;
lbl-1000: // 1 sources:
        {
            ste = var6_4[var4_6];
            Main.gui.log(ste.toString());
            ++var4_6;
lbl33: // 2 sources:
            ** while (var4_6 < var5_5)
        }
lbl34: // 1 sources:
        e.printStackTrace();
    }

    private static void loadWorkbookFromExcel(File file) throws InvalidFormatException, IOException {
        OPCPackage opcPackage = OPCPackage.open(file.getAbsolutePath());
        XSSFWorkbook book = new XSSFWorkbook(opcPackage);
        XSSFSheet sheet = book.getSheet("UPC CODE LIST");
        Cell[] upcBits = new Cell[12];
        int[] upc = new int[12];
        rowNum = 8;
        while (rowNum < 60000) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                ++rowNum;
                continue;
            }
            Cell description = row.getCell(1);
            Cell code = row.getCell(15);
            if (code == null || description == null) {
                ++rowNum;
                continue;
            }
            code.setCellType(1);
            description.setCellType(1);
            String codeStr = code.getStringCellValue();
            String descriptionStr = description.getStringCellValue();
            if (codeStr.trim().isEmpty() || codeStr.equals("*") || codeStr.equals("N/A") || code.getStringCellValue().isEmpty() || description.getStringCellValue().isEmpty() || description.getStringCellValue().equals("0") || code.getStringCellValue().equals("0")) {
                ++rowNum;
                continue;
            }
            int i = 2;
            while (i < 14) {
                Cell upcBit;
                upcBits[i - 2] = upcBit = row.getCell(i);
                upc[i - 2] = upcBit == null ? 0 : (upcBit.getCellType() == 0 || upcBit.getCellType() == 2 ? (int)upcBit.getNumericCellValue() : Integer.valueOf(upcBit.getStringCellValue()));
                ++i;
            }
            String upcStr = Main.getCellValue(upc);
            String genUpcStr = genDb.hasGenericUPC(codeStr) ? genDb.getGenericUpc(codeStr) : upcStr;
            db.addNewProduct(codeStr, upcStr, genUpcStr, descriptionStr);
            ++rowNum;
        }
    }

    private static void loadWorkbook() throws IOException, InvalidFormatException {
        genDb = new ProductGenericsInfo(new File("resources/generics.csv"));
        db = new ProductDatabase();
        boolean downloadSheet = false;
        boolean outputCSV = false;
        gui.log("Checking for updated master UPC data.");
        if (!Main.checkDates() || !localExcelFile.exists()) {
            downloadSheet = true;
            outputCSV = true;
        } else {
            gui.log("UPC data is up-to-date.");
        }
        if (!outputCSV && !localCSVFile.exists()) {
            outputCSV = true;
        }
        if (downloadSheet) {
            Main.copyWorkbookFromServer();
        }
        if (outputCSV) {
            gui.log("Creating product database");
            gui.log("Copying workbook..");
            gui.log("Populating product database from excel file");
            Main.loadWorkbookFromExcel(localExcelFile);
            gui.log("Outputting cached csv database");
            db.writeCsvFile(localCSVFile);
        } else {
            gui.log("Populating database from csv file");
            db.readFromCsv(localCSVFile);
        }
        gui.log("Finished loading database.");
        gui.log("Please either 'Load' an existing project");
        gui.log("or begin a new project by entering info");
    }

    private static boolean checkDates() {
        String date = config.getProperty("lastModified");
        if (date != null && remoteExcelFile.exists()) {
            long lmd = remoteExcelFile.lastModified();
            if (String.valueOf(lmd).equals(date)) {
                return true;
            }
            gui.log("Local excel file is out of date.");
            gui.log("Updating with a new copy.  Please be patient.");
        }
        return false;
    }

    private static String getCellValue(int[] vals) {
        String out = "";
        int i = 0;
        while (i < vals.length) {
            out = String.valueOf(out) + String.valueOf(vals[i]);
            ++i;
        }
        return out;
    }

    private static void copyWorkbookFromServer() throws IOException {
        int read;
        File file = new File("\\\\192.168.17.107\\Cust Service\\UPC Codes\\upccodes.xlsx");
        if (file.exists()) {
            gui.log("Found remote file on server, starting copy...");
        }
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        FileOutputStream fos = new FileOutputStream(new File("resources/upccodes.xlsx"));
        long size = file.length();
        gui.log("Reading file from server. File size: " + size);
        long readBytes = 0;
        while ((read = fis.read(buffer)) > 0) {
            gui.readAndDispatch();
            fos.write(buffer, 0, read);
            gui.readAndDispatch();
            gui.log("Please be patient.... copied : " + (readBytes += (long)read) + " of " + size + " bytes");
        }
        fos.close();
        fis.close();
        gui.log("Finished copying excel file from server");
        config.setProperty("lastModified", String.valueOf(remoteExcelFile.lastModified()));
    }
}

