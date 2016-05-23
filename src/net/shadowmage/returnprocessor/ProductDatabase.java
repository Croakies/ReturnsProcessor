/*
 * Decompiled with CFR 0_114.
 */
package net.shadowmage.returnprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.shadowmage.returnprocessor.Product;
import net.shadowmage.returnprocessor.Utils;

public class ProductDatabase {
    private HashMap<String, Set<Product>> productByUPC = new HashMap();
    private HashMap<String, Product> productsByCode = new HashMap();

    public void addNewProduct(String code, String upc, String genericUpc, String description) {
        Product p = new Product(code.toUpperCase(), upc, genericUpc, description);
        this.addNewProduct(p);
    }

    private void addNewProduct(Product p) {
        this.productsByCode.put(p.code, p);
        if (!this.productByUPC.containsKey(p.upc)) {
            this.productByUPC.put(p.upc, new HashSet());
        }
        if (!this.productByUPC.containsKey(p.genericupc)) {
            this.productByUPC.put(p.genericupc, new HashSet());
        }
        this.productByUPC.get(p.upc).add(p);
        this.productByUPC.get(p.genericupc).add(p);
    }

    public int getElementCount() {
        return this.productsByCode.size();
    }

    public Product getProduct(String code) {
        return this.productsByCode.get(code.toUpperCase());
    }

    public Collection<Product> getProducts(String upc) {
        return this.productByUPC.get(upc);
    }

    public Collection<Product> getProducts() {
        return this.productsByCode.values();
    }

    public void writeCsvFile(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
        for (Product p : this.productsByCode.values()) {
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

    public void readFromCsv(File file) throws IOException {
        List<String> lines = Utils.getCSVLines(file);
        for (String line : lines) {
            String[] bits = Utils.parseCsvLine(line);
            Product p = new Product(bits[0], bits[1], bits[2], bits[3]);
            this.addNewProduct(p);
        }
    }
}

