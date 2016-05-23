package net.shadowmage.returnprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductDatabase
{



private HashMap<String, Set<Product>> productByUPC = new HashMap<String, Set<Product>>();

private HashMap<String, Product> productsByCode = new HashMap<String, Product>();

public void addNewProduct(String code, String upc, String genericUpc, String description)
  {
  Product p = new Product(code.toUpperCase(), upc, genericUpc, description);
  addNewProduct(p);
  }

private void addNewProduct(Product p)
  {
  productsByCode.put(p.code, p);  
  if(!productByUPC.containsKey(p.upc))
    {
    productByUPC.put(p.upc, new HashSet<Product>());
    }
  if(!productByUPC.containsKey(p.genericupc))
    {
    productByUPC.put(p.genericupc, new HashSet<Product>());
    }
  productByUPC.get(p.upc).add(p);
  productByUPC.get(p.genericupc).add(p);

//  System.out.println("Adding new product: "+p.code + " :: "+p.upc +" :: "+p.genericupc +" :: "+p.description);
  }

public int getElementCount()
  {
  return this.productsByCode.size();
  }

public Product getProduct(String code)
  {
  return productsByCode.get(code.toUpperCase());
  }

public Collection<Product> getProducts(String upc)
  {
  return productByUPC.get(upc);
  }

public Collection<Product> getProducts()
  {
  return productsByCode.values();
  }

public void writeCsvFile(File file) throws IOException
  {
  FileOutputStream fos = new FileOutputStream(file);
  BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
  for(Product p : this.productsByCode.values())
    {
    writer.write(p.code);
    writer.write(",");
    writer.write(p.upc);
    writer.write(",");
    writer.write(p.genericupc);
    writer.write(",");
    writer.write(p.description);
    writer.newLine();
    }
  writer.close();
  fos.close();
  }

public void readFromCsv(File file) throws IOException
  {
  List<String> lines = Utils.getCSVLines(file);
  String[] bits;
  Product p;
  for(String line : lines)
    {
    bits = Utils.parseCsvLine(line);
    p = new Product(bits[0], bits[1], bits[2], bits[3]);
    this.addNewProduct(p);
    }
  }

}
