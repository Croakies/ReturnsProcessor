/*
 * Decompiled with CFR 0_114.
 */
package net.shadowmage.returnprocessor;

import java.util.Comparator;
import org.eclipse.swt.widgets.TableItem;

public class TableItemSorter
implements Comparator<TableItem> {
    int sortingColumn = 0;
    public boolean sortDirection = false;

    public void setSortingColumn(int columnIndex) {
        this.sortingColumn = columnIndex;
    }

    @Override
    public int compare(TableItem o1, TableItem o2) {
        String d2;
        String d1 = o1.getText(this.sortingColumn);
        if (this.isIntValue(d1, d2 = o2.getText(this.sortingColumn))) {
            return this.compareInt(d1, d2);
        }
        return this.sortDirection ? d1.compareTo(d2) : d2.compareTo(d1);
    }

    private boolean isIntValue(String d1, String d2) {
        try {
            Integer.parseInt(d1);
        }
        catch (NumberFormatException e) {
            return false;
        }
        try {
            Integer.parseInt(d2);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private int compareInt(String d1, String d2) {
        int a = Integer.parseInt(d1);
        int b = Integer.parseInt(d2);
        return this.sortDirection ? b - a : a - b;
    }
}

