/*
 * Decompiled with CFR 0_114.
 */
package net.shadowmage.returnprocessor;

import java.util.HashMap;
import net.shadowmage.returnprocessor.Product;

public class ProductGroup {
    String upc;
    HashMap<String, Product> productsByCode;

    public ProductGroup(String upc) {
        this.upc = upc;
        this.productsByCode = new HashMap();
    }

    public void addProduct(Product p) {
        this.productsByCode.put(p.code, p);
    }
}

