package net.shadowmage.returnprocessor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class Gui
{

private Display display;
private Shell shell;
private Shell inputSelectionShell;
private Shell searchShell;

int saveBoxY = 0;
int inputLabelY = 30;
int inputBoxY = inputLabelY + 20;
int editLabelY = inputBoxY+20;
int editBoxY = editLabelY+20;

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

private Button autoEnter;//checkbox

private Shell consoleShell;
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

String[] titles = {"Index", "Code", "UPC", "Generic UPC", "Description", "Good", "Reject"};
TableItemSorter tableSorter = new TableItemSorter();
int sortColumn = 0;
boolean sortDirection = false;//true=ascending, false=descending

public Gui() throws IOException
  {
  display = new Display();
  consoleShell = new Shell(display, SWT.BORDER | SWT.TITLE);
  consoleShell.setSize(325, 200);
  consoleShell.setText("Console");
  
  
  consoleText = new Text(consoleShell, SWT.V_SCROLL | SWT.MULTI | SWT.H_SCROLL);
  consoleText.setSize(325, 200);
  consoleText.setText("");
  consoleText.setLocation(0, 0);
  consoleText.setEditable(false);
  consoleShell.pack();
  consoleShell.open();
  

  File file = new File("log.txt");
  if(!file.exists())
    {
    file.createNewFile();
    }
  ps = new PrintStream(file);
  
  shell = new Shell(display, SWT.MAX | SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE);
  shell.setSize(640, 480);
  shell.setText("Returns Processor"); 
  shell.addListener(SWT.Resize, new Listener()
    {    
    @Override
    public void handleEvent(Event event)
      {
      updateTableBounds();
      }
    }); 
  shell.open();
  consoleShell.setLocation(shell.getLocation().x+shell.getSize().x+10, shell.getLocation().y);
  
  shell.addListener(SWT.Close, new Listener()
    {
    @Override
    public void handleEvent(Event event)
      {
      if(unsavedProgress)
        {
        MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO);
        mb.setText("Unsaved Progress!!");
        mb.setMessage("If you quit now, you will lose all unsaved progress.\nDo you really want to quit?");        
        if(mb.open()==SWT.YES)
          {
          event.doit = true;
          }
        else
          {
          event.doit = false;
          }
        }
      }
    });
  }

public void mainUpdateLoop() throws IOException
  {  
  while(!shell.isDisposed())
    {
    if(!display.readAndDispatch())
      {
      display.sleep();
      }   
    }
  display.dispose(); 
  ps.close();
  }

public void readAndDispatch()
  {
  display.readAndDispatch();
  }

public void log(String message)
  {
  message = consoleText.getText().equals("") ? message : "\n" + message;
  consoleText.append(message);
  System.out.println(message);
  try
    {
    byte[] bytes = message.getBytes();
    ps.write(bytes);
    } 
  catch (IOException e)
    {
    e.printStackTrace();
    }
  }

public void addElements()
  {
  addTable();
  
  Label inputLabel = new Label(shell, SWT.BORDER);
  inputLabel.setLocation(0, inputLabelY);
  inputLabel.setText("Input by Code");
  inputLabel.pack();
  
  codeInput = new Text(shell, SWT.BORDER);
  codeInput.setLocation(0, inputBoxY);
  codeInput.pack();
  codeInput.setSize(175, codeInput.getSize().y);
  
  Label inputLabel2 = new Label(shell, SWT.BORDER);
  inputLabel2.setLocation(200, inputLabelY);
  inputLabel2.setText("OR Input by UPC");
  inputLabel2.pack();
  
  upcInput = new Text(shell, SWT.BORDER);
  upcInput.setLocation(200, inputBoxY);
  upcInput.pack();
  upcInput.setSize(100, upcInput.getSize().y); 
  upcInput.addListener(SWT.Traverse, new Listener()
    {    
    @Override
    public void handleEvent(Event event)
      {
      if(event.keyCode==SWT.TAB && upcInput.isFocusControl())
        {
        validInput.setSelection(0, validInput.getText().length());
        }    
      else if(autoEnter.getSelection() && event.keyCode==SWT.CR)
        {
        if(validInput.getText().equals("0"))
          {
          validInput.setText("1");
          }
        addProductByUPC(upcInput.getText(), validInput.getText(), invalidInput.getText());
        updateTable();
        }
      }
    });  
  upcInput.addVerifyListener(new VerifyListener()
    {
    @Override
    public void verifyText(VerifyEvent e)
      {
      if(e.character==SWT.BS || e.character==SWT.DEL || e.character==SWT.CR || e.text.equals(""))
        {
        e.doit = true;
        }
      else
        {        
        try
          {
          Integer.parseInt(e.text);
          }
        catch(NumberFormatException e1)
          {
          e.doit = false;
          }
        }      
      }
    });
  
  
  Label inputLabel3 = new Label(shell, SWT.BORDER);
  inputLabel3.setLocation(325, inputLabelY);
  inputLabel3.setText("Good");
  inputLabel3.pack();
  
  validInput = new Text(shell, SWT.BORDER);
  validInput.setLocation(325, inputBoxY);
  validInput.pack();  
  validInput.setSize(50, validInput.getSize().y);
  validInput.addListener(SWT.Traverse, new Listener()
    {    
    @Override
    public void handleEvent(Event event)
      {
      if(event.keyCode==SWT.TAB && validInput.isFocusControl())
        {
        invalidInput.setSelection(0, invalidInput.getText().length());
        }        
      }
    });
  validInput.setText("0");
  
  validInput.addVerifyListener(new VerifyListener()
    {
    @Override
    public void verifyText(VerifyEvent e)
      {
      if(e.character==SWT.BS || e.character==SWT.DEL || e.character==SWT.CR)
        {
        e.doit = true;
        }
      else
        {
        try
          {
          Integer.parseInt(e.text);
          }
        catch(NumberFormatException e1)
          {
          e.doit = false;
          }
        }      
      }
    });
  
  Label inputLabel4 = new Label(shell, SWT.BORDER);
  inputLabel4.setLocation(400, inputLabelY);
  inputLabel4.setText("Reject");
  inputLabel4.pack();
  
  invalidInput = new Text(shell, SWT.BORDER);
  invalidInput.setLocation(400, inputBoxY);
  invalidInput.pack();  
  invalidInput.setSize(50, validInput.getSize().y);
  invalidInput.setText("0");
  
  invalidInput.addVerifyListener(new VerifyListener()
    {
    @Override
    public void verifyText(VerifyEvent e)
      {
      if(e.character==SWT.BS || e.character==SWT.DEL || e.character==SWT.CR)
        {
        e.doit = true;
        }
      else
        {
        try
          {
          Integer.parseInt(e.text);
          }
        catch(NumberFormatException e1)
          {
          e.doit = false;
          }
        }      
      }
    });
  
  Button button = new Button(shell, SWT.NONE);
  button.setText("Add Count");
  button.pack();
  button.setLocation(475, inputBoxY);
  button.addMouseListener(new MouseListener()
    {
    @Override
    public void mouseDoubleClick(MouseEvent e)
      {
      }

    @Override
    public void mouseDown(MouseEvent e)
      {

      }

    @Override
    public void mouseUp(MouseEvent e)
      {
      String code = codeInput.getText();
      String upc = upcInput.getText();
      String validStr = validInput.getText();
      String invalidStr = invalidInput.getText();
      if(!code.isEmpty())
        {
        addProductByCode(code, validStr, invalidStr);
        clearInput();
        codeInput.forceFocus();
        }
      else if(!upc.isEmpty())
        {
        addProductByUPC(upc, validStr, invalidStr);
        }     
      updateTable();
      }
    });
  
  button.addListener(SWT.Traverse, new Listener()
    {   
    @Override
    public void handleEvent(Event event)
      {
      if(event.keyCode == SWT.CR)
        {
        String code = codeInput.getText();
        String upc = upcInput.getText();
        String validStr = validInput.getText();
        String invalidStr = invalidInput.getText();
        if(!code.isEmpty())
          {
          addProductByCode(code, validStr, invalidStr);          
          }
        else if(!upc.isEmpty())
          {
          addProductByUPC(upc, validStr, invalidStr);
          }            
        updateTable();
        codeInput.forceFocus();
        }
      }
    });
  
  load = new Button(shell, SWT.NONE);
  load.setText("Load");
  load.setLocation(0, saveBoxY);
  load.pack();
  load.addListener(SWT.MouseUp, new Listener()
    {
    @Override
    public void handleEvent(Event event)
      {
      handleLoadAction();
      }
    });
  
  save = new Button(shell, SWT.NONE);
  save.setText("Save");
  save.setLocation(0 + load.getSize().x + 10, saveBoxY);
  save.pack();
  save.addListener(SWT.MouseUp, new Listener()
    {
    @Override
    public void handleEvent(Event event)
      {
      handleSaveAction();
      }
    });
  
  reset = new Button(shell, SWT.NONE);
  reset.setText("Reset");
  reset.setLocation(save.getLocation().x + save.getSize().x + 10, saveBoxY);
  reset.pack();
  reset.addListener(SWT.MouseUp, new Listener()
    {
    @Override
    public void handleEvent(Event event)
      {
      
      MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
      mb.setMessage("Do you really wish to start over?\nThis action is not reversible\nYou will lose all entered data that is not saved.");
      int id = mb.open();
      if(id==SWT.YES)
        {  
        Main.countsDb = new ProductCounts();
        log("Reset working spreadsheet.");
        updateTable();        
        }          
      }
    });
  
  autoEnter = new Button(shell, SWT.CHECK);
  autoEnter.setText("Auto Enter on UPC Input");
  autoEnter.setLocation(reset.getLocation().x + reset.getSize().x+10, saveBoxY);
  autoEnter.pack();
  
  
  Label label = new Label(shell, SWT.NONE);
  label.setText("Edit Selection:");  
  label.setLocation(0, editLabelY);
  label.pack();
  /**
   * add table-manipulation boxes   * 
   */
  selectedItemCodeLabel = new Text(shell, SWT.BORDER);
  selectedItemCodeLabel.setEditable(false);
  selectedItemCodeLabel.setText("No Selection");
  selectedItemCodeLabel.setLocation(0, editBoxY);
  selectedItemCodeLabel.pack();
  selectedItemCodeLabel.setSize(180, selectedItemCodeLabel.getSize().y);
  
  selectedItemSpecificLabel = new Text(shell, SWT.BORDER);
  selectedItemSpecificLabel.setEditable(false);
  selectedItemSpecificLabel.setText("..");
  selectedItemSpecificLabel.setLocation(200, editBoxY);
  selectedItemSpecificLabel.pack();
  selectedItemSpecificLabel.setSize(105, selectedItemSpecificLabel.getSize().y);
    
  selectedItemValidLabel = new Text(shell, SWT.BORDER);
  selectedItemValidLabel.setText("0");
  selectedItemValidLabel.setLocation(325, editBoxY);
  selectedItemValidLabel.pack();
  selectedItemValidLabel.setSize(50, validInput.getSize().y);
  selectedItemValidLabel.addVerifyListener(new VerifyListener()
    {
    @Override
    public void verifyText(VerifyEvent e)
      {
      if(e.character==SWT.BS || e.character==SWT.DEL || e.character==SWT.CR)
        {
        e.doit = true;
        }
      else
        {
        try
          {
          Integer.parseInt(e.text);
          }
        catch(NumberFormatException e1)
          {
          e.doit = false;
          }
        }      
      }
    });
  
  selectedItemInvalidLabel = new Text(shell, SWT.BORDER);
  selectedItemInvalidLabel.setText("0");
  selectedItemInvalidLabel.setLocation(400, editBoxY);
  selectedItemInvalidLabel.pack();
  selectedItemInvalidLabel.setSize(50, validInput.getSize().y);
  selectedItemInvalidLabel.addVerifyListener(new VerifyListener()
    {
    @Override
    public void verifyText(VerifyEvent e)
      {
      if(e.character==SWT.BS || e.character==SWT.DEL || e.character==SWT.CR)
        {
        e.doit = true;
        }
      else
        {
        try
          {
          Integer.parseInt(e.text);
          }
        catch(NumberFormatException e1)
          {
          e.doit = false;
          }
        }      
      }
    });
  
  updateRecord = new Button(shell, SWT.NONE);
  updateRecord.setText("Update Record");
  updateRecord.setLocation(475, editBoxY);
  updateRecord.pack();
  updateRecord.addListener(SWT.MouseUp, new Listener()
    {
    @Override
    public void handleEvent(Event event)
      {
      if(selectedCode!=null)
        {
        Product p = Main.db.getProduct(selectedCode);
        ProductCount pc = Main.countsDb.getProductCount(p);
        try{Integer.parseInt(selectedItemValidLabel.getText());}
        catch(NumberFormatException e){selectedItemValidLabel.setText("0");}
        
        try{Integer.parseInt(selectedItemInvalidLabel.getText());}
        catch(NumberFormatException e){selectedItemInvalidLabel.setText("0");}
        
        pc.setValidCount(Integer.parseInt(selectedItemValidLabel.getText()));
        pc.setInvalidCount(Integer.parseInt(selectedItemInvalidLabel.getText()));
        updateTable();
        unsavedProgress = true;
        }      
      }
    });
  
  updateRecord.addListener(SWT.Traverse, new Listener()
    {   
    @Override
    public void handleEvent(Event event)
      {
      if(event.keyCode == SWT.CR)
        {
        Product p = Main.db.getProduct(selectedCode);
        if(selectedCode!=null)
          {
          ProductCount pc = Main.countsDb.getProductCount(p);
          pc.setValidCount(Integer.parseInt(selectedItemValidLabel.getText()));
          pc.setInvalidCount(Integer.parseInt(selectedItemInvalidLabel.getText()));
          updateTable();
          }  
        }
      }
    });
  
  find = new Button(shell, SWT.NONE);
  find.setText("Search");
  find.setLocation(autoEnter.getLocation().x + autoEnter.getSize().x + 10, autoEnter.getLocation().y);
  find.pack();
  find.addListener(SWT.MouseUp, new Listener()
    {
    @Override
    public void handleEvent(Event event)
      {      
      /**
       * TODO ... todo what?
       */      
      searchShell.setLocation(find.getLocation());
      searchShell.open();
      searchShell.setVisible(true);
      searchShell.setEnabled(true);
      shell.setEnabled(false);
      searchShell.setFocus();      
      }
    });
  
  searchShell = new Shell(display, SWT.BORDER);
  searchShell.setLocation(find.getLocation());
  searchShell.setSize(200, 70);
  searchShell.setVisible(false);
  searchShell.setEnabled(false);
  
  label = new Label(searchShell, SWT.NONE);
  label.setText("Search for:");
  label.setLocation(10, 0);
  label.pack();
  
  Text searchInput = new Text(searchShell, SWT.BORDER);
  searchInput.setLocation(10, 30);  
  searchInput.pack();
  searchInput.setSize(125, searchInput.getSize().y);
  searchText = searchInput;
  
  Button searchSelect = new Button(searchShell, SWT.NONE);
  searchSelect.setText("Search");
  searchSelect.setLocation(searchInput.getLocation().x + 10 + searchInput.getSize().x, searchInput.getLocation().y);
  searchSelect.pack();
  searchSelect.addListener(SWT.MouseUp, new Listener()
    {
    @Override
    public void handleEvent(Event event)
      {
      
      String text = searchText.getText();
      handleProductSelection(text);
      searchShell.setVisible(false);
      searchShell.setEnabled(false);
      shell.setEnabled(true);
      shell.setFocus();
      searchText.setText("");
      }    
    });
  
  
  }

private void handleProductSelection(String code)
  {  
  Product p = Main.db.getProduct(code);
  ProductCount pc = null;
  boolean valid = p !=null;
  if(valid)
    {
    pc = Main.countsDb.getProductCount(p);
    }
  valid = p!=null && pc!=null;  
  if(valid)
    {
    this.selectedCode = code;
    selectedItemCodeLabel.setText(p.code);
    selectedItemSpecificLabel.setText(p.upc);
    selectedItemValidLabel.setText(String.valueOf(pc.getValidCount()));
    selectedItemInvalidLabel.setText(String.valueOf(pc.getInvalidCount()));    
    }
  else
    {
    MessageBox mb = new MessageBox(shell, SWT.OK);
    mb.setText("Invalid Code");
    mb.setMessage("Invalid code selected: "+code+"\nIf you believe this is an error\nplease contact an administrator");
    mb.open();
    }
  }

private void addProductByCode(String code, String valid, String invalid)
  {
  try{Integer.parseInt(valid);}
  catch(NumberFormatException e){valid = "0";}
  try{Integer.parseInt(invalid);}
  catch(NumberFormatException e){invalid = "0";}
  Product p = Main.db.getProduct(code);
  if(p!=null)
    {
    Main.countsDb.addToProductValidCount(p, Integer.parseInt(valid));  
    Main.countsDb.addToProductInvalidCount(p, Integer.parseInt(invalid));
    }
  else
    {
    MessageBox mb = new MessageBox(shell, SWT.OK);
    mb.setMessage("Invalid code: "+code+"\n"+
        "If you believe this is an error\n"+
        "please contact an Administrator");
    mb.open();
    log("invalid code: "+code);
    }  
  clearInput();
  codeInput.forceFocus();
  unsavedProgress = true;
  }

private void addProductByUPC(String upc, String valid, String invalid)
  {  
  Collection<Product> products = Main.db.getProducts(upc);  
  if(products==null || products.size()==0)
    {
    MessageBox mb = new MessageBox(shell, SWT.OK);
    mb.setMessage("Invalid upc: "+upc+"\n"+
        "If you believe this is an error\n"+
        "please contact an Administrator");
    mb.open();
    log("invalid upc: "+upc);
    return;
    }
  if(products.size()==1)
    {
    for(Product p : products)
      {
      addProductByCode(p.code, validInput.getText(), invalidInput.getText());
      clearInput();
      upcInput.forceFocus();
      break;
      }   
    return;
    }
  
  shell.setEnabled(false);
  inputSelectionShell = new Shell(display, SWT.BORDER);
  inputSelectionShell.setLocation(shell.getLocation());
  inputSelectionShell.setSize(400, 200);
  
  Label prefixLabel = new Label(inputSelectionShell, SWT.NONE);
  prefixLabel.setLocation(0,0);
  prefixLabel.setText("Multiple Product exist for the input UPC.  Please select one.");
  prefixLabel.pack();
  
  inputSelectionTable = new Table(inputSelectionShell, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION );
  inputSelectionTable.setLocation(0, prefixLabel.getLocation().y + prefixLabel.getSize().y + 10);
  inputSelectionTable.setLinesVisible(true);
  inputSelectionTable.setHeaderVisible(true);
  inputSelectionTable.getVerticalBar().setVisible(true);
  
  String[] titles = {"Code", "UPC", "Generic UPC", "Description"};
  for (int i=0; i<titles.length; i++) 
    {
    TableColumn column = new TableColumn (inputSelectionTable, SWT.NONE);
    column.setText (titles [i]);
    }
  
  for(Product p : products)
    {
    TableItem item = new TableItem(inputSelectionTable, SWT.NONE);    
    item.setText (0, p.code);
    item.setText (1, p.upc);
    item.setText (2, p.genericupc);
    item.setText (3, p.description);
    }
  for(TableColumn cl : inputSelectionTable.getColumns())
    {
    cl.pack();
    }
  
  inputSelectionTable.pack();
  inputSelectionTable.setSize(475, 250);
  
  inputSelectionTable.addListener(SWT.MouseUp, new Listener()
    {
    @Override
    public void handleEvent(Event event)
      {
      TableItem[] items = ((Table)event.widget).getSelection();
      if(items.length>0 && items[0]!=null)
        {
        String code = items[0].getText(0);
        log("selected code: "+code + " for UPC "+upcInput.getText());
        addProductByCode(code, validInput.getText(), invalidInput.getText());
        
        updateTable();
        inputSelectionShell.dispose();
        shell.setEnabled(true);
        shell.forceActive();
        shell.forceFocus();
        clearInput();
        upcInput.setText("");
        upcInput.forceFocus();
        }
      }
    });
  
  inputSelectionShell.pack();
  inputSelectionShell.open();
  }

private void clearInput()
  {
  codeInput.setText("");
  upcInput.setText("");
  validInput.setText("0");
  invalidInput.setText("0");
  }

private void addTable()
  {
 
  Table table = new Table(shell, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
  table.setLinesVisible(true);
  table.setHeaderVisible(true);
  table.getVerticalBar().setVisible(true);
  
  GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
  data.heightHint = 200;
  table.setLayoutData(data);

  for (int i=0; i<titles.length; i++) 
    {
    final int index = i;
    TableColumn column = new TableColumn (table, SWT.NONE);
    column.setText (titles [i]);
    column.addListener(SWT.Selection, new Listener()
      {
      @Override
      public void handleEvent(Event event)
        {
        if(sortColumn==index){sortDirection=!sortDirection;}
        else{sortDirection=false;}
        sortColumn = index;
        sortTable(index);
        }
      });
    }
  inputDisplayTable = table;
  updateTable();
  
  table.addListener(SWT.Selection, new Listener()
    {
    @Override
    public void handleEvent(Event event)
      {
      TableItem item = inputDisplayTable.getSelection()[0];
      handleProductSelection(item.getText(1));
      }
    });
  }

private void sortTable(int index)
  {
  tableSorter.setSortingColumn(index);
  tableSorter.sortDirection = sortDirection;
  
  inputDisplayTable.setSortColumn(inputDisplayTable.getColumn(index));
  inputDisplayTable.setSortDirection(sortDirection? SWT.UP : SWT.DOWN);  
  
  TableItem[] items = this.inputDisplayTable.getItems(); 
  
  List<TableItem> itemsList = Arrays.asList(items); 
  Collections.sort(itemsList, tableSorter);  
  items = itemsList.toArray(items);
  
  for(TableItem old : items)
    {
    TableItem item = new TableItem(inputDisplayTable, SWT.NONE);    
    for(int i = 0; i <7 ;i++)
      {
      item.setText(i, old.getText(i));
      }
    old.dispose();
    }
  }

public void updateTable()
  {
  inputDisplayTable.removeAll();
  int elementNum = 1;  
  for(ProductCount pc : Main.countsDb.getCounts())
    {
    TableItem item = new TableItem (inputDisplayTable, SWT.NONE);    
    item.setText (0, String.valueOf(elementNum));
    item.setText (1, pc.getProduct().code);
    item.setText (2, pc.getProduct().upc);
    item.setText (3, pc.getProduct().genericupc);
    item.setText (4, pc.getProduct().description);
    item.setText (5, String.valueOf(pc.getValidCount()));
    item.setText (6, String.valueOf(pc.getInvalidCount()));  
    elementNum++;
    }
  for(TableColumn cl : inputDisplayTable.getColumns())
    {
    cl.pack();
    }
  inputDisplayTable.pack();
  updateTableBounds();
  sortTable(sortColumn);
  }

private void updateTableBounds()
  {
  Rectangle clientArea = shell.getClientArea ();
  inputDisplayTable.setBounds (clientArea.x, clientArea.y+120, clientArea.width, clientArea.height-120);
  }

private void handleSaveAction()
  {
  FileDialog dialog = new FileDialog(shell, SWT.BORDER);
  dialog.setFilterPath(".");
  dialog.setFilterExtensions(new String[]{"*.csv"});
  dialog.setFileName("*.csv");
  dialog.open();
  if(!"".equals(dialog.getFileName()))
    {
    handleSaveFileSelection(new File(dialog.getFilterPath(), dialog.getFileName()));    
    }  
  }

private void handleLoadAction()
  {
  FileDialog dialog = new FileDialog(shell, SWT.BORDER);
  dialog.setFilterPath(".");
  dialog.setFilterExtensions(new String[]{"*.csv"});
  dialog.open();
  handleLoadFileSelection(new File(dialog.getFilterPath(), dialog.getFileName()));
  
  }

private void handleSaveFileSelection(File file)
  {
  try
    {
    boolean write = true;
    if(file.exists())
      {      
      MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
//      MessageBox mb = new MessageBox(shell, SWT.BORDER | SWT.YES | SWT.NO);
      mb.setMessage("Chosen File already exists.  Do you wish to overwrite?");
      int msg = mb.open();
      if(msg!=SWT.YES)
        {
        write = false;
        }
      }
    if(write)
      {
      Main.countsDb.writeToCSV(file); 
      log("saved to file: "+file.getAbsolutePath()); 
      unsavedProgress = false;
      lastSaveTime = System.currentTimeMillis();
      }
    } 
  catch (IOException e)
    {
    for(StackTraceElement ste : e.getStackTrace())
      {
      log(ste.toString());
      }
    e.printStackTrace();
    }
  }

private void handleLoadFileSelection(File file)
  {
  if(file==null)
    {
    return;
    }
  if(!file.exists() || file.getName().equals("."))
    {
    log("Selected file does not exist: "+file.getAbsolutePath());
    log("Please select a valid file to load");
    return;
    }
  
  Main.countsDb = new ProductCounts();
  try
    {
    log("loading from file: "+file.getAbsolutePath()); 
    Main.countsDb.parseFromFile(file);
    unsavedProgress = false;
    } 
  catch (IOException e)
    {
    for(StackTraceElement ste : e.getStackTrace())
      {
      log(ste.toString());
      }
    e.printStackTrace();
    }
  updateTable();
  }

}
