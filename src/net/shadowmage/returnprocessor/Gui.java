/*
 * Decompiled with CFR 0_114.
 */
package net.shadowmage.returnprocessor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.shadowmage.returnprocessor.Main;
import net.shadowmage.returnprocessor.Product;
import net.shadowmage.returnprocessor.ProductCount;
import net.shadowmage.returnprocessor.ProductCounts;
import net.shadowmage.returnprocessor.ProductDatabase;
import net.shadowmage.returnprocessor.TableItemSorter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class Gui {
    private Display display = new Display();
    private Shell shell;
    private Shell inputSelectionShell;
    private Shell searchShell;
    int saveBoxY = 0;
    int inputLabelY = 30;
    int inputBoxY = this.inputLabelY + 20;
    int editLabelY = this.inputBoxY + 20;
    int editBoxY = this.editLabelY + 20;
    private Table inputSelectionTable;
    private Table inputDisplayTable;
    private Text codeInput;
    private Text upcInput;
    private Text validInput;
    private Text invalidInput;
    private Button load;
    private Button save;
    private Button reset;
    private Button find;
    private Text searchText;
    private Button autoEnter;
    private Shell consoleShell = new Shell(this.display, 2080);
    private Text consoleText;
    private PrintStream ps;
    private String selectedCode;
    private Text selectedItemCodeLabel;
    private Text selectedItemSpecificLabel;
    private Text selectedItemValidLabel;
    private Text selectedItemInvalidLabel;
    private Button updateRecord;
    private boolean unsavedProgress = false;
    long lastSaveTime;
    String[] titles = new String[]{"Index", "Code", "UPC", "Generic UPC", "Description", "Good", "Reject"};
    TableItemSorter tableSorter = new TableItemSorter();
    int sortColumn = 0;
    boolean sortDirection = false;

    public Gui() throws IOException {
        this.consoleShell.setSize(325, 200);
        this.consoleShell.setText("Console");
        this.consoleText = new Text(this.consoleShell, 770);
        this.consoleText.setSize(325, 200);
        this.consoleText.setText("");
        this.consoleText.setLocation(0, 0);
        this.consoleText.setEditable(false);
        this.consoleShell.pack();
        this.consoleShell.open();
        File file = new File("log.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        this.ps = new PrintStream(file);
        this.shell = new Shell(this.display, 1264);
        this.shell.setSize(640, 480);
        this.shell.setText("Returns Processor");
        this.shell.addListener(11, new Listener(){

            @Override
            public void handleEvent(Event event) {
                Gui.this.updateTableBounds();
            }
        });
        this.shell.open();
        this.consoleShell.setLocation(this.shell.getLocation().x + this.shell.getSize().x + 10, this.shell.getLocation().y);
        this.shell.addListener(21, new Listener(){

            @Override
            public void handleEvent(Event event) {
                if (Gui.this.unsavedProgress) {
                    MessageBox mb = new MessageBox(Gui.this.shell, 192);
                    mb.setText("Unsaved Progress!!");
                    mb.setMessage("If you quit now, you will lose all unsaved progress.\nDo you really want to quit?");
                    event.doit = mb.open() == 64;
                }
            }
        });
    }

    public void mainUpdateLoop() throws IOException {
        while (!this.shell.isDisposed()) {
            if (this.display.readAndDispatch()) continue;
            this.display.sleep();
        }
        this.display.dispose();
        this.ps.close();
    }

    public void readAndDispatch() {
        this.display.readAndDispatch();
    }

    public void log(String message) {
        message = this.consoleText.getText().equals("") ? message : "\n" + message;
        this.consoleText.append(message);
        System.out.println(message);
        try {
            byte[] bytes = message.getBytes();
            this.ps.write(bytes);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addElements() {
        this.addTable();
        Label inputLabel = new Label(this.shell, 2048);
        inputLabel.setLocation(0, this.inputLabelY);
        inputLabel.setText("Input by Code");
        inputLabel.pack();
        this.codeInput = new Text(this.shell, 2048);
        this.codeInput.setLocation(0, this.inputBoxY);
        this.codeInput.pack();
        this.codeInput.setSize(175, this.codeInput.getSize().y);
        Label inputLabel2 = new Label(this.shell, 2048);
        inputLabel2.setLocation(200, this.inputLabelY);
        inputLabel2.setText("OR Input by UPC");
        inputLabel2.pack();
        this.upcInput = new Text(this.shell, 2048);
        this.upcInput.setLocation(200, this.inputBoxY);
        this.upcInput.pack();
        this.upcInput.setSize(100, this.upcInput.getSize().y);
        this.upcInput.addListener(31, new Listener(){

            @Override
            public void handleEvent(Event event) {
                if (event.keyCode == 9 && Gui.this.upcInput.isFocusControl()) {
                    Gui.this.validInput.setSelection(0, Gui.this.validInput.getText().length());
                } else if (Gui.this.autoEnter.getSelection() && event.keyCode == 13) {
                    if (Gui.this.validInput.getText().equals("0")) {
                        Gui.this.validInput.setText("1");
                    }
                    Gui.this.addProductByUPC(Gui.this.upcInput.getText(), Gui.this.validInput.getText(), Gui.this.invalidInput.getText());
                    Gui.this.updateTable();
                }
            }
        });
        this.upcInput.addVerifyListener(new VerifyListener(){

            @Override
            public void verifyText(VerifyEvent e) {
                if (e.character == '\b' || e.character == '' || e.character == '\r' || e.text.equals("")) {
                    e.doit = true;
                } else {
                    try {
                        Integer.parseInt(e.text);
                    }
                    catch (NumberFormatException e1) {
                        e.doit = false;
                    }
                }
            }
        });
        Label inputLabel3 = new Label(this.shell, 2048);
        inputLabel3.setLocation(325, this.inputLabelY);
        inputLabel3.setText("Good");
        inputLabel3.pack();
        this.validInput = new Text(this.shell, 2048);
        this.validInput.setLocation(325, this.inputBoxY);
        this.validInput.pack();
        this.validInput.setSize(50, this.validInput.getSize().y);
        this.validInput.addListener(31, new Listener(){

            @Override
            public void handleEvent(Event event) {
                if (event.keyCode == 9 && Gui.this.validInput.isFocusControl()) {
                    Gui.this.invalidInput.setSelection(0, Gui.this.invalidInput.getText().length());
                }
            }
        });
        this.validInput.setText("0");
        this.validInput.addVerifyListener(new VerifyListener(){

            @Override
            public void verifyText(VerifyEvent e) {
                if (e.character == '\b' || e.character == '' || e.character == '\r') {
                    e.doit = true;
                } else {
                    try {
                        Integer.parseInt(e.text);
                    }
                    catch (NumberFormatException e1) {
                        e.doit = false;
                    }
                }
            }
        });
        Label inputLabel4 = new Label(this.shell, 2048);
        inputLabel4.setLocation(400, this.inputLabelY);
        inputLabel4.setText("Reject");
        inputLabel4.pack();
        this.invalidInput = new Text(this.shell, 2048);
        this.invalidInput.setLocation(400, this.inputBoxY);
        this.invalidInput.pack();
        this.invalidInput.setSize(50, this.validInput.getSize().y);
        this.invalidInput.setText("0");
        this.invalidInput.addVerifyListener(new VerifyListener(){

            @Override
            public void verifyText(VerifyEvent e) {
                if (e.character == '\b' || e.character == '' || e.character == '\r') {
                    e.doit = true;
                } else {
                    try {
                        Integer.parseInt(e.text);
                    }
                    catch (NumberFormatException e1) {
                        e.doit = false;
                    }
                }
            }
        });
        Button button = new Button(this.shell, 0);
        button.setText("Add Count");
        button.pack();
        button.setLocation(475, this.inputBoxY);
        button.addMouseListener(new MouseListener(){

            @Override
            public void mouseDoubleClick(MouseEvent e) {
            }

            @Override
            public void mouseDown(MouseEvent e) {
            }

            @Override
            public void mouseUp(MouseEvent e) {
                String code = Gui.this.codeInput.getText();
                String upc = Gui.this.upcInput.getText();
                String validStr = Gui.this.validInput.getText();
                String invalidStr = Gui.this.invalidInput.getText();
                if (!code.isEmpty()) {
                    Gui.this.addProductByCode(code, validStr, invalidStr);
                    Gui.this.clearInput();
                    Gui.this.codeInput.forceFocus();
                } else if (!upc.isEmpty()) {
                    Gui.this.addProductByUPC(upc, validStr, invalidStr);
                }
                Gui.this.updateTable();
            }
        });
        button.addListener(31, new Listener(){

            @Override
            public void handleEvent(Event event) {
                if (event.keyCode == 13) {
                    String code = Gui.this.codeInput.getText();
                    String upc = Gui.this.upcInput.getText();
                    String validStr = Gui.this.validInput.getText();
                    String invalidStr = Gui.this.invalidInput.getText();
                    if (!code.isEmpty()) {
                        Gui.this.addProductByCode(code, validStr, invalidStr);
                    } else if (!upc.isEmpty()) {
                        Gui.this.addProductByUPC(upc, validStr, invalidStr);
                    }
                    Gui.this.updateTable();
                    Gui.this.codeInput.forceFocus();
                }
            }
        });
        this.load = new Button(this.shell, 0);
        this.load.setText("Load");
        this.load.setLocation(0, this.saveBoxY);
        this.load.pack();
        this.load.addListener(4, new Listener(){

            @Override
            public void handleEvent(Event event) {
                Gui.this.handleLoadAction();
            }
        });
        this.save = new Button(this.shell, 0);
        this.save.setText("Save");
        this.save.setLocation(0 + this.load.getSize().x + 10, this.saveBoxY);
        this.save.pack();
        this.save.addListener(4, new Listener(){

            @Override
            public void handleEvent(Event event) {
                Gui.this.handleSaveAction();
            }
        });
        this.reset = new Button(this.shell, 0);
        this.reset.setText("Reset");
        this.reset.setLocation(this.save.getLocation().x + this.save.getSize().x + 10, this.saveBoxY);
        this.reset.pack();
        this.reset.addListener(4, new Listener(){

            @Override
            public void handleEvent(Event event) {
                MessageBox mb = new MessageBox(Gui.this.shell, 200);
                mb.setMessage("Do you really wish to start over?\nThis action is not reversible\nYou will lose all entered data that is not saved.");
                int id = mb.open();
                if (id == 64) {
                    Main.countsDb = new ProductCounts();
                    Gui.this.log("Reset working spreadsheet.");
                    Gui.this.updateTable();
                }
            }
        });
        this.autoEnter = new Button(this.shell, 32);
        this.autoEnter.setText("Auto Enter on UPC Input");
        this.autoEnter.setLocation(this.reset.getLocation().x + this.reset.getSize().x + 10, this.saveBoxY);
        this.autoEnter.pack();
        Label label = new Label(this.shell, 0);
        label.setText("Edit Selection:");
        label.setLocation(0, this.editLabelY);
        label.pack();
        this.selectedItemCodeLabel = new Text(this.shell, 2048);
        this.selectedItemCodeLabel.setEditable(false);
        this.selectedItemCodeLabel.setText("No Selection");
        this.selectedItemCodeLabel.setLocation(0, this.editBoxY);
        this.selectedItemCodeLabel.pack();
        this.selectedItemCodeLabel.setSize(180, this.selectedItemCodeLabel.getSize().y);
        this.selectedItemSpecificLabel = new Text(this.shell, 2048);
        this.selectedItemSpecificLabel.setEditable(false);
        this.selectedItemSpecificLabel.setText("..");
        this.selectedItemSpecificLabel.setLocation(200, this.editBoxY);
        this.selectedItemSpecificLabel.pack();
        this.selectedItemSpecificLabel.setSize(105, this.selectedItemSpecificLabel.getSize().y);
        this.selectedItemValidLabel = new Text(this.shell, 2048);
        this.selectedItemValidLabel.setText("0");
        this.selectedItemValidLabel.setLocation(325, this.editBoxY);
        this.selectedItemValidLabel.pack();
        this.selectedItemValidLabel.setSize(50, this.validInput.getSize().y);
        this.selectedItemValidLabel.addVerifyListener(new VerifyListener(){

            @Override
            public void verifyText(VerifyEvent e) {
                if (e.character == '\b' || e.character == '' || e.character == '\r') {
                    e.doit = true;
                } else {
                    try {
                        Integer.parseInt(e.text);
                    }
                    catch (NumberFormatException e1) {
                        e.doit = false;
                    }
                }
            }
        });
        this.selectedItemInvalidLabel = new Text(this.shell, 2048);
        this.selectedItemInvalidLabel.setText("0");
        this.selectedItemInvalidLabel.setLocation(400, this.editBoxY);
        this.selectedItemInvalidLabel.pack();
        this.selectedItemInvalidLabel.setSize(50, this.validInput.getSize().y);
        this.selectedItemInvalidLabel.addVerifyListener(new VerifyListener(){

            @Override
            public void verifyText(VerifyEvent e) {
                if (e.character == '\b' || e.character == '' || e.character == '\r') {
                    e.doit = true;
                } else {
                    try {
                        Integer.parseInt(e.text);
                    }
                    catch (NumberFormatException e1) {
                        e.doit = false;
                    }
                }
            }
        });
        this.updateRecord = new Button(this.shell, 0);
        this.updateRecord.setText("Update Record");
        this.updateRecord.setLocation(475, this.editBoxY);
        this.updateRecord.pack();
        this.updateRecord.addListener(4, new Listener(){

            @Override
            public void handleEvent(Event event) {
                if (Gui.this.selectedCode != null) {
                    Product p = Main.db.getProduct(Gui.this.selectedCode);
                    ProductCount pc = Main.countsDb.getProductCount(p);
                    try {
                        Integer.parseInt(Gui.this.selectedItemValidLabel.getText());
                    }
                    catch (NumberFormatException e) {
                        Gui.this.selectedItemValidLabel.setText("0");
                    }
                    try {
                        Integer.parseInt(Gui.this.selectedItemInvalidLabel.getText());
                    }
                    catch (NumberFormatException e) {
                        Gui.this.selectedItemInvalidLabel.setText("0");
                    }
                    pc.setValidCount(Integer.parseInt(Gui.this.selectedItemValidLabel.getText()));
                    pc.setInvalidCount(Integer.parseInt(Gui.this.selectedItemInvalidLabel.getText()));
                    Gui.this.updateTable();
                    Gui.access$16(Gui.this, true);
                }
            }
        });
        this.updateRecord.addListener(31, new Listener(){

            @Override
            public void handleEvent(Event event) {
                if (event.keyCode == 13) {
                    Product p = Main.db.getProduct(Gui.this.selectedCode);
                    if (Gui.this.selectedCode != null) {
                        ProductCount pc = Main.countsDb.getProductCount(p);
                        pc.setValidCount(Integer.parseInt(Gui.this.selectedItemValidLabel.getText()));
                        pc.setInvalidCount(Integer.parseInt(Gui.this.selectedItemInvalidLabel.getText()));
                        Gui.this.updateTable();
                    }
                }
            }
        });
        this.find = new Button(this.shell, 0);
        this.find.setText("Search");
        this.find.setLocation(this.autoEnter.getLocation().x + this.autoEnter.getSize().x + 10, this.autoEnter.getLocation().y);
        this.find.pack();
        this.find.addListener(4, new Listener(){

            @Override
            public void handleEvent(Event event) {
                Gui.this.searchShell.setLocation(Gui.this.find.getLocation());
                Gui.this.searchShell.open();
                Gui.this.searchShell.setVisible(true);
                Gui.this.searchShell.setEnabled(true);
                Gui.this.shell.setEnabled(false);
                Gui.this.searchShell.setFocus();
            }
        });
        this.searchShell = new Shell(this.display, 2048);
        this.searchShell.setLocation(this.find.getLocation());
        this.searchShell.setSize(200, 70);
        this.searchShell.setVisible(false);
        this.searchShell.setEnabled(false);
        label = new Label(this.searchShell, 0);
        label.setText("Search for:");
        label.setLocation(10, 0);
        label.pack();
        Text searchInput = new Text(this.searchShell, 2048);
        searchInput.setLocation(10, 30);
        searchInput.pack();
        searchInput.setSize(125, searchInput.getSize().y);
        this.searchText = searchInput;
        Button searchSelect = new Button(this.searchShell, 0);
        searchSelect.setText("Search");
        searchSelect.setLocation(searchInput.getLocation().x + 10 + searchInput.getSize().x, searchInput.getLocation().y);
        searchSelect.pack();
        searchSelect.addListener(4, new Listener(){

            @Override
            public void handleEvent(Event event) {
                String text = Gui.this.searchText.getText();
                Gui.this.handleProductSelection(text);
                Gui.this.searchShell.setVisible(false);
                Gui.this.searchShell.setEnabled(false);
                Gui.this.shell.setEnabled(true);
                Gui.this.shell.setFocus();
                Gui.this.searchText.setText("");
            }
        });
    }

    private void handleProductSelection(String code) {
        boolean valid;
        Product p = Main.db.getProduct(code);
        ProductCount pc = null;
        boolean bl = valid = p != null;
        if (valid) {
            pc = Main.countsDb.getProductCount(p);
        }
        boolean bl2 = valid = p != null && pc != null;
        if (valid) {
            this.selectedCode = code;
            this.selectedItemCodeLabel.setText(p.code);
            this.selectedItemSpecificLabel.setText(p.upc);
            this.selectedItemValidLabel.setText(String.valueOf(pc.getValidCount()));
            this.selectedItemInvalidLabel.setText(String.valueOf(pc.getInvalidCount()));
        } else {
            MessageBox mb = new MessageBox(this.shell, 32);
            mb.setText("Invalid Code");
            mb.setMessage("Invalid code selected: " + code + "\nIf you believe this is an error\nplease contact an administrator");
            mb.open();
        }
    }

    private void addProductByCode(String code, String valid, String invalid) {
        try {
            Integer.parseInt(valid);
        }
        catch (NumberFormatException e) {
            valid = "0";
        }
        try {
            Integer.parseInt(invalid);
        }
        catch (NumberFormatException e) {
            invalid = "0";
        }
        Product p = Main.db.getProduct(code);
        if (p != null) {
            Main.countsDb.addToProductValidCount(p, Integer.parseInt(valid));
            Main.countsDb.addToProductInvalidCount(p, Integer.parseInt(invalid));
        } else {
            MessageBox mb = new MessageBox(this.shell, 32);
            mb.setMessage("Invalid code: " + code + "\n" + "If you believe this is an error\n" + "please contact an Administrator");
            mb.open();
            this.log("invalid code: " + code);
        }
        this.clearInput();
        this.codeInput.forceFocus();
        this.unsavedProgress = true;
    }

    private void addProductByUPC(String upc, String valid, String invalid) {
        Collection<Product> products = Main.db.getProducts(upc);
        if (products == null || products.size() == 0) {
            MessageBox mb = new MessageBox(this.shell, 32);
            mb.setMessage("Invalid upc: " + upc + "\n" + "If you believe this is an error\n" + "please contact an Administrator");
            mb.open();
            this.log("invalid upc: " + upc);
            return;
        }
        if (products.size() == 1) {
            Iterator<Product> iterator = products.iterator();
            if (iterator.hasNext()) {
                Product p = iterator.next();
                this.addProductByCode(p.code, this.validInput.getText(), this.invalidInput.getText());
                this.clearInput();
                this.upcInput.forceFocus();
            }
            return;
        }
        this.shell.setEnabled(false);
        this.inputSelectionShell = new Shell(this.display, 2048);
        this.inputSelectionShell.setLocation(this.shell.getLocation());
        this.inputSelectionShell.setSize(400, 200);
        Label prefixLabel = new Label(this.inputSelectionShell, 0);
        prefixLabel.setLocation(0, 0);
        prefixLabel.setText("Multiple Product exist for the input UPC.  Please select one.");
        prefixLabel.pack();
        this.inputSelectionTable = new Table(this.inputSelectionShell, 68100);
        this.inputSelectionTable.setLocation(0, prefixLabel.getLocation().y + prefixLabel.getSize().y + 10);
        this.inputSelectionTable.setLinesVisible(true);
        this.inputSelectionTable.setHeaderVisible(true);
        this.inputSelectionTable.getVerticalBar().setVisible(true);
        String[] titles = new String[]{"Code", "UPC", "Generic UPC", "Description"};
        int i = 0;
        while (i < titles.length) {
            TableColumn column = new TableColumn(this.inputSelectionTable, 0);
            column.setText(titles[i]);
            ++i;
        }
        for (Product p : products) {
            TableItem item = new TableItem(this.inputSelectionTable, 0);
            item.setText(0, p.code);
            item.setText(1, p.upc);
            item.setText(2, p.genericupc);
            item.setText(3, p.description);
        }
        TableColumn[] arrtableColumn = this.inputSelectionTable.getColumns();
        int item = arrtableColumn.length;
        int column = 0;
        while (column < item) {
            TableColumn cl = arrtableColumn[column];
            cl.pack();
            ++column;
        }
        this.inputSelectionTable.pack();
        this.inputSelectionTable.setSize(475, 250);
        this.inputSelectionTable.addListener(4, new Listener(){

            @Override
            public void handleEvent(Event event) {
                TableItem[] items = ((Table)event.widget).getSelection();
                if (items.length > 0 && items[0] != null) {
                    String code = items[0].getText(0);
                    Gui.this.log("selected code: " + code + " for UPC " + Gui.this.upcInput.getText());
                    Gui.this.addProductByCode(code, Gui.this.validInput.getText(), Gui.this.invalidInput.getText());
                    Gui.this.updateTable();
                    Gui.this.inputSelectionShell.dispose();
                    Gui.this.shell.setEnabled(true);
                    Gui.this.shell.forceActive();
                    Gui.this.shell.forceFocus();
                    Gui.this.clearInput();
                    Gui.this.upcInput.setText("");
                    Gui.this.upcInput.forceFocus();
                }
            }
        });
        this.inputSelectionShell.pack();
        this.inputSelectionShell.open();
    }

    private void clearInput() {
        this.codeInput.setText("");
        this.upcInput.setText("");
        this.validInput.setText("0");
        this.invalidInput.setText("0");
    }

    private void addTable() {
        Table table = new Table(this.shell, 67588);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.getVerticalBar().setVisible(true);
        GridData data = new GridData(4, 4, true, true);
        data.heightHint = 200;
        table.setLayoutData(data);
        int i = 0;
        while (i < this.titles.length) {
            final int index = i;
            TableColumn column = new TableColumn(table, 0);
            column.setText(this.titles[i]);
            column.addListener(13, new Listener(){

                @Override
                public void handleEvent(Event event) {
                    Gui.this.sortDirection = Gui.this.sortColumn == index ? !Gui.this.sortDirection : false;
                    Gui.this.sortColumn = index;
                    Gui.this.sortTable(index);
                }
            });
            ++i;
        }
        this.inputDisplayTable = table;
        this.updateTable();
        table.addListener(13, new Listener(){

            @Override
            public void handleEvent(Event event) {
                TableItem item = Gui.this.inputDisplayTable.getSelection()[0];
                Gui.this.handleProductSelection(item.getText(1));
            }
        });
    }

    private void sortTable(int index) {
        this.tableSorter.setSortingColumn(index);
        this.tableSorter.sortDirection = this.sortDirection;
        this.inputDisplayTable.setSortColumn(this.inputDisplayTable.getColumn(index));
        this.inputDisplayTable.setSortDirection(this.sortDirection ? 128 : 1024);
        TableItem[] items = this.inputDisplayTable.getItems();
        List<TableItem> itemsList = Arrays.asList(items);
        Collections.sort(itemsList, this.tableSorter);
        TableItem[] arrtableItem = items = itemsList.toArray(items);
        int n = arrtableItem.length;
        int n2 = 0;
        while (n2 < n) {
            TableItem old = arrtableItem[n2];
            TableItem item = new TableItem(this.inputDisplayTable, 0);
            int i = 0;
            while (i < 7) {
                item.setText(i, old.getText(i));
                ++i;
            }
            old.dispose();
            ++n2;
        }
    }

    public void updateTable() {
        this.inputDisplayTable.removeAll();
        int elementNum = 1;
        for (ProductCount pc : Main.countsDb.getCounts()) {
            TableItem item = new TableItem(this.inputDisplayTable, 0);
            item.setText(0, String.valueOf(elementNum));
            item.setText(1, pc.getProduct().code);
            item.setText(2, pc.getProduct().upc);
            item.setText(3, pc.getProduct().genericupc);
            item.setText(4, pc.getProduct().description);
            item.setText(5, String.valueOf(pc.getValidCount()));
            item.setText(6, String.valueOf(pc.getInvalidCount()));
            ++elementNum;
        }
        TableColumn[] arrtableColumn = this.inputDisplayTable.getColumns();
        int item = arrtableColumn.length;
        int n = 0;
        while (n < item) {
            TableColumn cl = arrtableColumn[n];
            cl.pack();
            ++n;
        }
        this.inputDisplayTable.pack();
        this.updateTableBounds();
        this.sortTable(this.sortColumn);
    }

    private void updateTableBounds() {
        Rectangle clientArea = this.shell.getClientArea();
        this.inputDisplayTable.setBounds(clientArea.x, clientArea.y + 120, clientArea.width, clientArea.height - 120);
    }

    private void handleSaveAction() {
        FileDialog dialog = new FileDialog(this.shell, 2048);
        dialog.setFilterPath(".");
        dialog.setFilterExtensions(new String[]{"*.csv"});
        dialog.setFileName("*.csv");
        dialog.open();
        if (!"".equals(dialog.getFileName())) {
            this.handleSaveFileSelection(new File(dialog.getFilterPath(), dialog.getFileName()));
        }
    }

    private void handleLoadAction() {
        FileDialog dialog = new FileDialog(this.shell, 2048);
        dialog.setFilterPath(".");
        dialog.setFilterExtensions(new String[]{"*.csv"});
        dialog.open();
        this.handleLoadFileSelection(new File(dialog.getFilterPath(), dialog.getFileName()));
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    private void handleSaveFileSelection(File file) {
        try {
            write = true;
            if (file.exists()) {
                mb = new MessageBox(this.shell, 200);
                mb.setMessage("Chosen File already exists.  Do you wish to overwrite?");
                msg = mb.open();
                if (msg != 64) {
                    return;
                }
                if (write == false) return;
            }
            Main.countsDb.writeToCSV(file);
            this.log("saved to file: " + file.getAbsolutePath());
            this.unsavedProgress = false;
            this.lastSaveTime = System.currentTimeMillis();
            return;
        }
        catch (IOException e) {
            var6_8 = e.getStackTrace();
            var5_9 = var6_8.length;
            msg = 0;
            ** while (msg < var5_9)
        }
lbl-1000: // 1 sources:
        {
            ste = var6_8[msg];
            this.log(ste.toString());
            ++msg;
            continue;
        }
lbl24: // 1 sources:
        e.printStackTrace();
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    private void handleLoadFileSelection(File file) {
        block5 : {
            if (file == null) {
                return;
            }
            if (!file.exists() || file.getName().equals(".")) {
                this.log("Selected file does not exist: " + file.getAbsolutePath());
                this.log("Please select a valid file to load");
                return;
            }
            Main.countsDb = new ProductCounts();
            try {
                this.log("loading from file: " + file.getAbsolutePath());
                Main.countsDb.parseFromFile(file);
                this.unsavedProgress = false;
                break block5;
            }
            catch (IOException e) {
                var6_3 = e.getStackTrace();
                var5_4 = var6_3.length;
                var4_5 = 0;
                ** while (var4_5 < var5_4)
            }
lbl-1000: // 1 sources:
            {
                ste = var6_3[var4_5];
                this.log(ste.toString());
                ++var4_5;
                continue;
            }
lbl22: // 1 sources:
            e.printStackTrace();
        }
        this.updateTable();
    }

    static /* synthetic */ void access$16(Gui gui, boolean bl) {
        gui.unsavedProgress = bl;
    }

}

