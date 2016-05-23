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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import net.shadowmage.returnprocessor.Main;
import net.shadowmage.returnprocessor.Product;
import net.shadowmage.returnprocessor.ProductCount;
import net.shadowmage.returnprocessor.ProductDatabase;
import net.shadowmage.returnprocessor.Utils;

public class ProductCounts {
    private HashMap<String, ProductCount> counts = new HashMap();
    private List<ProductCount> countList = new ArrayList<ProductCount>();

    public void setProductValidCount(Product product, int validCount) {
        ProductCount pc = this.getOrCreateProductCount(product);
        pc.setValidCount(validCount);
    }

    public void setProductInvalidCount(Product product, int invalidCount) {
        ProductCount pc = this.getOrCreateProductCount(product);
        pc.setInvalidCount(invalidCount);
    }

    public void addToProductValidCount(Product product, int addition) {
        ProductCount pc = this.getOrCreateProductCount(product);
        pc.setValidCount(pc.getValidCount() + addition);
    }

    public void addToProductInvalidCount(Product product, int addition) {
        ProductCount pc = this.getOrCreateProductCount(product);
        pc.setInvalidCount(pc.getInvalidCount() + addition);
    }

    private ProductCount getOrCreateProductCount(Product product) {
        ProductCount pc = this.counts.get(product.code);
        if (pc == null) {
            pc = new ProductCount(product);
            this.counts.put(product.code, pc);
            this.countList.add(pc);
        }
        return pc;
    }

    public List<ProductCount> getCounts() {
        return this.countList;
    }

    public ProductCount getProductCount(Product p) {
        return this.counts.get(p.code);
    }

    public void writeToCSV(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
        for (ProductCount p : this.counts.values()) {
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

    public void parseFromFile(File file) throws IOException {
        List<String> lines = Utils.getCSVLines(file);
        for (String line : lines) {
            String[] bits = Utils.parseCsvLine(line);
            String code = bits[0];
            String validCount = bits[1];
            String invalidCount = bits[2];
            Product p = Main.db.getProduct(code);
            if (p == null) continue;
            this.setProductValidCount(p, Integer.parseInt(validCount));
            this.setProductInvalidCount(p, Integer.parseInt(invalidCount));
        }
    }
}

