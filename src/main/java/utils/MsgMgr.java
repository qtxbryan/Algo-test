package utils;

import java.io.*;
import java.net.Socket;


public class MsgMgr {

  private static Socket socket;
  private static MsgMgr instance;
  private BufferedWriter writer;
  private BufferedReader reader;


  private MsgMgr() {
  }


  public static MsgMgr getCommMgr() {
    if (instance == null) {
      instance = new MsgMgr();
    }
    return instance;
  }


  public boolean connectToRPi() {
    try {
      socket = new Socket(MsgConst.HOST_ADDRESS, MsgConst.PORT);
      writer = new BufferedWriter(
          new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream())));
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      System.out.println("Connection established with RPi");
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }


  public boolean endConnection() {
    System.out.println("Ending connection");
    try {
      socket.close();
      writer.close();
      reader.close();

      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }


  public boolean sendMsg(String msg) {
    try {
      System.out.println("Sending message: " + msg);
      writer.write(msg);
      writer.flush();
      System.out.println("Message sent");
      return true;
    } catch (IOException e) {
      e.printStackTrace();

      boolean result;
      while (true) {
        result = instance.connectToRPi();
        if (result) {
          break;
        }
      }
      return instance.sendMsg(msg);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }


  public String recieveMsg() {
    String msg = null;

    try {
      System.out.println("reading");
      msg = reader.readLine();
//      msg = "ALG|5,5,N|7,9,E";
      System.out.println("msg:" + msg);
      return msg;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Failed reading");
    return null;
  }
}
