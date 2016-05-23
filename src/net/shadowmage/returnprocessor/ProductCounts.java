package net.shadowmage.returnprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ProductCounts
{

private HashMap<String, ProductCount> counts = new HashMap<String, ProductCount>();
private List<ProductCount> countList = new ArrayList<ProductCount>();

public void setProductValidCount(Product product, int validCount)
  {
  ProductCount pc = getOrCreateProductCount(product);
  pc.setValidCount(validCount);
  }

public void setProductInvalidCount(Product product, int invalidCount)
  {
  ProductCount pc = getOrCreateProductCount(product);
  pc.setInvalidCount(invalidCount);
  }

public void addToProductValidCount(Product product, int addition)
  {
  ProductCount pc = getOrCreateProductCount(product);
  pc.setValidCount(pc.getValidCount() + addition);
  }

public void addToProductInvalidCount(Product product, int addition)
  {
  ProductCount pc = getOrCreateProductCount(product);
  pc.setInvalidCount(pc.getInvalidCount() + addition);
  }

private ProductCount getOrCreateProductCount(Product product)
  {
  ProductCount pc = counts.get(product.code);
  if(pc==null)
    {
    pc = new ProductCount(product);
    counts.put(product.code, pc);
    countList.add(pc);
    }  
  return pc;
  }

public List<ProductCount> getCounts()
  {
  return countList;
  }

public ProductCount getProductCount(Product p)
  {
  return counts.get(p.code);
  }

public void writeToCSV(File file) throws IOException
  {
  FileOutputStream fos = new FileOutputStream(file);
  BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
  for(ProductCount p : this.counts.values())
    {
    writer.write(p.getProduct().code);
    writer.write(",");
    writer.write(String.valueOf(p.getValidCount()));
    writer.write(",");
    writer.write(String.valueOf(p.getInvalidCount()));    
    writer.newLine();
    }
  writer.close();
  fos.close();
  }

public void parseFromFile(File file) throws IOException
  {
  List<String> lines = Utils.getCSVLines(file);
  
  String code;
  String validCount;
  String invalidCount;
  String bits[];
  for(String line : lines)
    {
    bits = Utils.parseCsvLine(line);
    code = bits[0];
    validCount = bits[1];
    invalidCount = bits[2];
    
    Product p = Main.db.getProduct(code);
    if(p!=null)
      {
      this.setProductValidCount(p, Integer.parseInt(validCount));
      this.setProductInvalidCount(p, Integer.parseInt(invalidCount));
      }
    }
  }

}
