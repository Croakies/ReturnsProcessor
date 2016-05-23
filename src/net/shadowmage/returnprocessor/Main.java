package net.shadowmage.returnprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Main 
{

public static ProductDatabase db;
public static ProductGenericsInfo genDb;
public static ProductCounts countsDb;
private static int rowNum = 0;
public static Gui gui;
public static Properties config;

protected static File remoteExcelFile;
protected static File localExcelFile;
protected static File configFile;
protected static File localCSVFile;

public static void main(String[] args) throws InvalidFormatException, IOException 
  {
    
  config = new Properties();
  configFile = new File("resources/config.cfg");
  localCSVFile = new File("resources/upccodes.csv");
  localExcelFile = new File("resources/upccodes.xlsx");
  remoteExcelFile = new File("\\\\192.168.17.107\\Cust Service\\UPC Codes\\upccodes.xlsx");
  if(!configFile.exists())
    {
    configFile.createNewFile();
    }
  FileInputStream fis = new FileInputStream(configFile);
  config.load(fis);
  fis.close();
  gui = new Gui();
  try
    {
    loadWorkbook();
    }
  catch(Exception e)
    {
    gui.log("Caught exception while reading rowNum: "+rowNum);
    for(StackTraceElement ste : e.getStackTrace())
      {
      gui.log(ste.toString());
      }
    e.printStackTrace();
    return;
    } 
  
  gui.log("saving config file...");
  configFile.createNewFile();
  FileOutputStream fos = new FileOutputStream(configFile);
  config.store(fos, "Returns Processor Configuration File");
  fos.close();
  
  countsDb = new ProductCounts();
       
  gui.addElements();
  gui.mainUpdateLoop();
  
  }

private static void loadWorkbookFromExcel(File file) throws InvalidFormatException, IOException
  {
  /**
   * load stuff from workbook into DB
   */
  OPCPackage opcPackage = OPCPackage.open(file.getAbsolutePath());
  XSSFWorkbook book = new XSSFWorkbook(opcPackage);
//  Workbook book = WorkbookFactory.create(file);
  Sheet sheet = book.getSheet("UPC CODE LIST");
  Row row;
  Cell upcBit;
  Cell description;
  Cell code;
  Cell[] upcBits = new Cell[12];
  
  String codeStr;
  String descriptionStr;
  String upcStr;
  String genUpcStr;
  
  int[] upc = new int[12];
  rowNum = 8;
  while(rowNum<60000)
    {
    row = sheet.getRow(rowNum);
    if(row==null)
      {
      rowNum++;
      continue;
      }
    description = row.getCell(1);
    code = row.getCell(15);
//    System.out.println(code+","+description);
    if(code==null || description==null)
      {
      rowNum++;
      continue;
      }
    code.setCellType(Cell.CELL_TYPE_STRING);
    description.setCellType(Cell.CELL_TYPE_STRING);
    codeStr = code.getStringCellValue();
    descriptionStr = description.getStringCellValue(); 
    if(codeStr.trim().isEmpty() || codeStr.equals("*") || codeStr.equals("N/A") || code.getStringCellValue().isEmpty() || description.getStringCellValue().isEmpty() || description.getStringCellValue().equals("0") || code.getStringCellValue().equals("0"))
      {
      rowNum++;
      continue;
      }   
    
    for(int i = 2; i < 14; i++)
      {
      upcBit = row.getCell(i);
      upcBits[i-2] = upcBit;
      if(upcBit==null)
        {
        upc[i-2] = 0;       
        }
      else if(upcBit.getCellType()==Cell.CELL_TYPE_NUMERIC || upcBit.getCellType()==Cell.CELL_TYPE_FORMULA)
        {
        upc[i-2] = (int) upcBit.getNumericCellValue();
        }
      else
        {
        upc[i-2] = Integer.valueOf(upcBit.getStringCellValue());
        }
      }
    upcStr = getCellValue(upc);
    if(genDb.hasGenericUPC(codeStr))
      {
      genUpcStr = genDb.getGenericUpc(codeStr);
      }
    else
      {
      genUpcStr = upcStr;
      }      
    db.addNewProduct(codeStr, upcStr, genUpcStr, descriptionStr);
    rowNum++;
    }
  }

private static void loadWorkbook() throws IOException, InvalidFormatException
  {
  genDb = new ProductGenericsInfo(new File("resources/generics.csv"));
  db = new ProductDatabase();
  
  boolean downloadSheet = false;
  boolean outputCSV = false;
  
  gui.log("Checking for updated master UPC data.");
  
  if(!checkDates() || !localExcelFile.exists())
    {
    downloadSheet = true;
    outputCSV = true;
    }
  else
    {
    gui.log("UPC data is up-to-date.");
    }
  if(!outputCSV && !localCSVFile.exists())
    {
    outputCSV = true;
    }
  
  if(downloadSheet)
    {
    copyWorkbookFromServer();    
    }
  
  if(outputCSV)
    {    
    gui.log("Creating product database");
    gui.log("Copying workbook..");
    gui.log("Populating product database from excel file");
    loadWorkbookFromExcel(localExcelFile);
    gui.log("Outputting cached csv database");
    db.writeCsvFile(localCSVFile);
    }
  else
    {
    gui.log("Populating database from csv file");
    db.readFromCsv(localCSVFile);   
    }
  
  gui.log("Finished loading database.");
  gui.log("Please either 'Load' an existing project");
  gui.log("or begin a new project by entering info");
  }

private static boolean checkDates()
  {   
  String date = config.getProperty("lastModified");
  if(date!=null && remoteExcelFile.exists())
    {
    long lmd = remoteExcelFile.lastModified();
    if(String.valueOf(lmd).equals(date))
      {
      return true;
      }
    else
      {
      gui.log("Local excel file is out of date.");
      gui.log("Updating with a new copy.  Please be patient.");
      }
    }
  return false;
  }

private static String getCellValue(int[] vals)
  {
  String out = "";
  for(int i = 0; i < vals.length; i++)
    {
    out = out + String.valueOf(vals[i]);
    }
  return out;
  }

private static void copyWorkbookFromServer() throws IOException
  {
  File file = new File("\\\\192.168.17.107\\Cust Service\\UPC Codes\\upccodes.xlsx");
  if(file.exists())
    {
    gui.log("Found remote file on server, starting copy...");
    }
  FileInputStream fis = new FileInputStream(file);

  byte[] buffer = new byte[1024];
  FileOutputStream fos = new FileOutputStream(new File("resources/upccodes.xlsx"));
  
  int read;
  long size = file.length();
  gui.log(("Reading file from server. File size: "+size));
  long readBytes = 0;
  while( (read = fis.read(buffer))>0)
    {
    readBytes += read;
    gui.readAndDispatch();
    fos.write(buffer, 0, read);
    gui.readAndDispatch();
    gui.log("Please be patient.... copied : "+readBytes +" of "+size+" bytes");
    }
  fos.close();
  fis.close();
  gui.log("Finished copying excel file from server");
  config.setProperty("lastModified", String.valueOf(remoteExcelFile.lastModified()));
  }

}
