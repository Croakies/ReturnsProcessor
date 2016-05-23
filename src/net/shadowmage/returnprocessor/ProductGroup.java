package net.shadowmage.returnprocessor;

import java.util.HashMap;

public class ProductGroup
{

String upc;
HashMap<String, Product> productsByCode;

public ProductGroup(String upc)
  {
  this.upc = upc;
  this.productsByCode = new HashMap<String, Product>();
  }

public void addProduct(Product p)
  {
  this.productsByCode.put(p.code, p);
  }
}
