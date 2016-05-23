package net.shadowmage.returnprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProductGenericsInfo
{

private HashMap<String, String> genericUpcByCode = new HashMap<String, String>();

public ProductGenericsInfo(File csvFile)
  {
  if(!csvFile.exists())
    {
    throw new IllegalArgumentException("Cannot read from non-existent file: "+csvFile.getAbsolutePath());
    }
  parseFile(csvFile);
  }

private final void parseFile(File file)
  {
  List<String> lines = null;
  try
    {
    lines = getCSVLines(file);        
    } 
  catch (IOException e)
    {  
    e.printStackTrace();
    return;
    }
  String[] bits;
  String code;
//  String specific;
  String generic;
  for(String line : lines)
    {
    bits = line.split(",", -1);
    code = bits[0];
    generic = bits[1];
//  specific = bits[2];
    genericUpcByCode.put(code.trim().toUpperCase(), "088989"+generic);
    }
  }

public boolean hasGenericUPC(String code)
  {
  return genericUpcByCode.containsKey(code.toUpperCase());
  }

public String getGenericUpc(String code)
  {
  return genericUpcByCode.get(code.toUpperCase());
  }

private List<String> getCSVLines(File file) throws IOException
  {
  List<String> lines = new ArrayList<String>();
  FileInputStream fis = new FileInputStream(file);
  BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
  String line;
  while((line = reader.readLine())!=null)
    {
    if(line.startsWith("#")){continue;}
    lines.add(line);
    }
  reader.close();
  fis.close();
  return lines;
  }
}
