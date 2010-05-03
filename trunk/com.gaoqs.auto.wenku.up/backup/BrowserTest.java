package com.gaoqs.auto.wenku.click;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class BrowserTest {
  static final ArrayList urls = new ArrayList(); // @jve:decl-index=0:
  static boolean canRemove = false;
  public static void main(String[] args) {
      Display display = new Display();
      Shell shell = new Shell(display);
      shell.setText("Main Window");
      shell.setLayout(new FillLayout());
      Browser browser = new Browser(shell, SWT.NONE);
      initialize(display, browser);
      shell.open();
      browser.setUrl("http://www.baidu.com");
      while (!shell.isDisposed()) {
        if (!display.readAndDispatch())
          display.sleep();
      }
      display.dispose();
  }

  static void initialize(final Display display, Browser browser) {
      browser.addListener(SWT.MouseDown, new Listener() {
        public void handleEvent(Event event) {
          System.out.println("event.time:" + event.time);
        }
      });
      browser.addOpenWindowListener(new OpenWindowListener() {
        public void open(WindowEvent event) {
          // Embed the new window
          final Shell shell = new Shell(display);
          shell.setText("New Window");
          shell.setLayout(new FillLayout());
          final Browser browser = new Browser(shell, SWT.NONE);
          initialize(display, browser);
          event.browser = browser;
          event.display.asyncExec(new Runnable() {
              public void run() {
                String url = browser.getUrl();
                if (urls.contains(url)){
                  //flag to chek if the window is closed automatic 
                  canRemove = false;
                  shell.close();
                }
                else{
                  canRemove = true;
                  urls.add(url);
                }
              }
          });
        }
      });
      browser.addVisibilityWindowListener(new VisibilityWindowListener() {
        public void hide(WindowEvent event) {
          Browser browser = (Browser) event.widget;
          Shell shell = browser.getShell();
          shell.setVisible(false);
        }

        public void show(WindowEvent event) {
          Browser browser = (Browser) event.widget;
          Shell shell = browser.getShell();
          if (event.location != null)
              shell.setLocation(event.location);
          if (event.size != null) {
              Point size = event.size;
              shell.setSize(shell.computeSize(size.x, size.y));
          }
          if (event.addressBar || event.menuBar || event.statusBar
                || event.toolBar) {
              // Create widgets for the address bar, menu bar, status bar and/or tool bar
              // leave enough space in the Shell to accomodate a Browser of the size
              // given by event.size
          }
          shell.open();
        }
      });
      browser.addDisposeListener(new DisposeListener(){
        public void widgetDisposed(DisposeEvent event){
          Browser browser = (Browser) event.widget;
          if (canRemove)
              urls.remove(browser.getUrl());
          Shell shell = browser.getShell();
          shell.close();
        }
      });
  }

}