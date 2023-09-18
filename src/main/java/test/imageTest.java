package test;

import utils.ImageAPI;

import java.util.List;

public class imageTest {

  public static void main(String[] args) {
    ImageAPI imageAPI = new ImageAPI();
    List<String> obj = imageAPI.detect();
    System.out.println(obj.toString());
  }
}
