/**
 * gaoqs 修改，继承至RuntimeException，如果在到达main之前没有被处理，后续程序将不再执行
 * 修改，异常处理
 * */
package com.thoughtworks.selenium;

//public class SeleniumException extends RuntimeException
public class SeleniumException extends Exception
{
  public SeleniumException(String message)
  {
    super(message);
  }

  public SeleniumException(Exception e) {
    super(e);
  }

  public SeleniumException(String message, Exception e) {
    super(message, e);
  }
}