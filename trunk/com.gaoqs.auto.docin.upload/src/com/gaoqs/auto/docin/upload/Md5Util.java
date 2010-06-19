package com.gaoqs.auto.docin.upload;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util
{
  public static String md5(String text, String key)
  {
    MessageDigest msgDigest = null;
    try
    {
      msgDigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
      throw new IllegalStateException(
        "System doesn't support MD5 algorithm.");
    }
    try
    {
      msgDigest.update(text.getBytes("GBK"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    byte[] bytes = (byte[])null;
    try {
      bytes = msgDigest.digest(key.getBytes("GBK"));
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    if (bytes == null) {
      throw new IllegalStateException("System doesn't support MD5 algorithm.");
    }

    String md5Str = new String();

    for (int i = 0; i < bytes.length; ++i) {
      byte tb = bytes[i];

      char tmpChar = (char)(tb >>> 4 & 0xF);
      char high;
      if (tmpChar >= '\n')
        high = (char)('a' + tmpChar - 10);
      else {
        high = (char)('0' + tmpChar);
      }

      md5Str = md5Str + high;
      tmpChar = (char)(tb & 0xF);
      char low;
      if (tmpChar >= '\n')
        low = (char)('a' + tmpChar - 10);
      else {
        low = (char)('0' + tmpChar);
      }

      md5Str = md5Str + low;
    }

    return md5Str;
  }

  public static String getString(String str, String key) {
    return md5(str, key.toUpperCase()).trim();
  }

}
