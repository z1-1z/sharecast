package com.jcraft.jzlib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GsZilb {
 
 private static final int MAXLENGTH = 1024*1024;
 private static final int BUFFERSIZE = 1024;

 public static byte[] Compress(byte[] object) throws IOException {

  byte[] data = null;
  try {
   ByteArrayOutputStream out = new ByteArrayOutputStream();
   ZOutputStream zOut = new ZOutputStream(out,
     JZlib.Z_BEST_COMPRESSION);
   DataOutputStream objOut = new DataOutputStream(zOut);
   objOut.write(object);
   objOut.flush();
   zOut.close();
   data = out.toByteArray();
   out.close();

  } catch (IOException e) {
   e.printStackTrace();
   throw e;
  }
  return data;
 }

 public static byte[] UnCompress(byte[] object) throws IOException {

  byte[] data = new byte[MAXLENGTH];
  try {
   ByteArrayInputStream in = new ByteArrayInputStream(object);
   ZInputStream zIn = new ZInputStream(in);
   DataInputStream objIn = new DataInputStream(zIn);

   int len = 0;
   int count = 0;
   int nextReadCount = BUFFERSIZE;
   while ((count = objIn.read(data, len, nextReadCount)) != -1) {
    len = len + count;
    nextReadCount = nextReadCount + len;
    while (nextReadCount > MAXLENGTH - len)
    {
    	nextReadCount = nextReadCount / 2;
    }
   }

   byte[] trueData = new byte[len];
   System.arraycopy(data, 0, trueData, 0, len);

   objIn.close();
   zIn.close();
   in.close();

   return trueData;

  } catch (IOException e) {
   e.printStackTrace();
   throw e;
  }
 }
}