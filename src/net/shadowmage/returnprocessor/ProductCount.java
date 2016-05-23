package net.shadowmage.returnprocessor;

public class ProductCount
{

private int validCount;
private int invalidCount;
private final Product product;

public ProductCount(Product p)
  {
  this.product = p;
  }

public int getValidCount()
  {
  return validCount;
  }

public void setValidCount(int validCount)
  {
  this.validCount = validCount;
  }

public int getInvalidCount()
  {
  return invalidCount;
  }

public void setInvalidCount(int invalidCount)
  {
  this.invalidCount = invalidCount;
  }

public Product getProduct()
  {
  return product;
  }


}
