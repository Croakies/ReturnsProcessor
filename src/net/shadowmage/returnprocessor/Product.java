package net.shadowmage.returnprocessor;

public class Product
{

String description;
String code;
String upc;
String genericupc;

public Product(String code, String upc, String genericupc, String desc)
  {
  this.code = code.toUpperCase();
  this.upc = upc;
  this.description = desc;
  this.genericupc = genericupc;
  }

}
