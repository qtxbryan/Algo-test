package utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

// All image APIs will be defined here
public class ImageAPI {

  //    private final String BASE_URL = "http://127.0.0.1:5000"; // localhost
  private final String BASE_URL = "http://127.0.0.1:3000"; // server
  private final OkHttpClient client = new OkHttpClient.Builder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .writeTimeout(10, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .build();

  public void clearDir() {
    System.out.println("Sending GET request...");

    Request get_request = buildGetRequest("/clear_dir");
    String responseStr = executeRequest(get_request);
    System.out.println(responseStr);
  }

  public List<String> detect() {
    System.out.println("Sending GET request...");

    Request get_request = buildGetRequest("/detect_image");
    String responseStr = executeRequest(get_request);
    System.out.println(responseStr);

    return List.of(responseStr.split(" ", -1));
  }

  public void combineImages() {
    System.out.println("Sending GET request...");

    Request get_request = buildGetRequest("/combine");
    String responseStr = executeRequest(get_request);
    System.out.println(responseStr);
  }

  private Request buildGetRequest(String route) {
    return new Request.Builder()
        .url(BASE_URL + route)
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