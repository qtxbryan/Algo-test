package utils;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class BotAPI {

  private final String BASE_URL = "http://192.168.10.106:3000"; // IP of RPI
  private final OkHttpClient client = new OkHttpClient.Builder()
          .connectTimeout(10, TimeUnit.SECONDS)
          .writeTimeout(10, TimeUnit.SECONDS)
          .readTimeout(30, TimeUnit.SECONDS)
          .build();

  public String getGyroscopeValue() {
    System.out.println("Sending GET request...");

    Request getRequest = buildGetRequest("/get_gyroscope_value");
    String responseStr = executeRequest(getRequest);
    System.out.println(responseStr);
    return responseStr;
  }

  public void postCommand(String command) {
    System.out.println("Sending POST request...");

    RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("command", command)
            .build();

    Request postRequest = buildPostRequest("/send_cmd", requestBody);
    String responseStr = executeRequest(postRequest);
    System.out.println(responseStr);
  }

  public void postMovement(String mode, String speed) {
    System.out.println("Sending POST request...");

    RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("mode", mode)
            .addFormDataPart("speed", speed)
            .build();

    Request postRequest = buildPostRequest("/move_robot", requestBody);
    String responseStr = executeRequest(postRequest);
    System.out.println(responseStr);
  }

  private Request buildGetRequest(String route) {
    return new Request.Builder()
            .url(BASE_URL + route)
            .build();
  }

  private Request buildPostRequest(String route, RequestBody requestBody) {
    return new Request.Builder()
            .url(BASE_URL + route)
            .post(requestBody)
            .build();
  }

  private String executeRequest(Request request) {
    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}


//package utils;
//
//import okhttp3.*;
//
//import java.io.IOException;
//import java.util.concurrent.TimeUnit;
//
//
//public class BotAPI {
//
//  private final String BASE_URL = "http://192.168.15.1:3000"; // IP of RPI
//  private final OkHttpClient client = new OkHttpClient.Builder()
//      .connectTimeout(10, TimeUnit.SECONDS)
//      .writeTimeout(10, TimeUnit.SECONDS)
//      .readTimeout(30, TimeUnit.SECONDS)
//      .build();
//
//  public String getGyroscopeValue() {
//    System.out.println("Sending GET request...");
//
//    Request getRequest = buildGetRequest("/get_gyroscope_value");
//    String responseStr = executeRequest(getRequest);
//    System.out.println(responseStr);
//    return responseStr;
//  }
//
//  public void postCommand(String command) {
//    System.out.println("Sending POST request...");
//
//    RequestBody requestBody = new MultipartBody.Builder()
//        .setType(MultipartBody.FORM)
//        .addFormDataPart("command", command)
//        .build();
//
//    Request postRequest = buildPostRequest("/send_cmd", requestBody);
//    String responseStr = executeRequest(postRequest);
//    System.out.println(responseStr);
//  }
//
//  public void postMovement(String mode, String speed) {
//    System.out.println("Sending POST request...");
//
//    RequestBody requestBody = new MultipartBody.Builder()
//        .setType(MultipartBody.FORM)
//        .addFormDataPart("mode", mode)
//        .addFormDataPart("speed", speed)
//        .build();
//
//    Request postRequest = buildPostRequest("/move_robot", requestBody);
//    String responseStr = executeRequest(postRequest);
//    System.out.println(responseStr);
//  }
//
//  private Request buildGetRequest(String route) {
//    return new Request.Builder()
//        .url(BASE_URL + route)
//        .build();
//  }
//
//  private Request buildPostRequest(String route, RequestBody requestBody) {
//    return new Request.Builder()
//        .url(BASE_URL + route)
//        .post(requestBody)
//        .build();
//  }
//
//  private String executeRequest(Request request) {
//    try (Response response = client.newCall(request).execute()) {
//      return response.body().string();
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
//  }
//}
