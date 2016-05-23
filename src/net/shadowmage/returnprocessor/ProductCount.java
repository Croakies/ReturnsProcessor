/*
 * Decompiled with CFR 0_114.
 */
package net.shadowmage.returnprocessor;

import net.shadowmage.returnprocessor.Product;

public class ProductCount {
    private int validCount;
    private int invalidCount;
    private final Product product;

    public ProductCount(Product p) {
        this.product = p;
    }

    public int getValidCount() {
        return this.validCount;
    }

    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }

    public int getInvalidCount() {
        return this.invalidCount;
    }

    public void setInvalidCount(int invalidCount) {
        this.invalidCount = invalidCount;
    }

    public Product getProduct() {
        return this.product;
    }
}

