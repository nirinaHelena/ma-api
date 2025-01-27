package app.m.advise.integration;

import static app.m.advise.testutils.TestUtils.DOCTOR1_TOKEN;
import static app.m.advise.testutils.TestUtils.anAvailablePort;
import static app.m.advise.testutils.TestUtils.setFileStorageService;
import static app.m.advise.testutils.TestUtils.setFirebaseService;
import static app.m.advise.testutils.TestUtils.user1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import app.m.advise.AbstractContextInitializer;
import app.m.advise.endpoint.rest.client.ApiClient;
import app.m.advise.service.api.firebase.FirebaseService;
import app.m.advise.service.file.FileStorageService;
import app.m.advise.testutils.TestUtils;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = FileControllerIT.ContextInitializer.class)
@AutoConfigureMockMvc
class FileControllerIT {
  @MockBean private FirebaseService firebaseServiceMock;
  @MockBean private FileStorageService fileStorageService;

  @BeforeEach
  void setUp() throws IOException {
    setFirebaseService(firebaseServiceMock);
    setFileStorageService(fileStorageService);
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  @Test
  @Disabled
  void upload_file_ok() throws IOException, InterruptedException {
    String fileId = "photo.png";
    byte[] file = fileId.getBytes();

    String filename = upload(fileId, file);

    assertEquals(fileId, filename);
  }

  @Test
  void upload_user_photo_ok() throws IOException, InterruptedException {
    byte[] file = "photo.png".getBytes();

    String filename = uploadUserPhoto(user1().getId(), file);

    assertNotNull(filename);
  }

  @Test
  void download_file_ok() throws IOException, InterruptedException {
    String fileId = "photo.png";

    byte[] actual = download(fileId);

    assertNotNull(actual);
  }

  private String upload(String fileId, byte[] file) throws IOException, InterruptedException {
    HttpClient unauthenticatedClient = HttpClient.newBuilder().build();
    String basePath = "http://localhost:" + FileControllerIT.ContextInitializer.SERVER_PORT;

    HttpResponse<String> response =
        unauthenticatedClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(basePath + "/raw/" + fileId))
                .header("Authorization", "Bearer " + DOCTOR1_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofByteArray(file))
                .build(),
            HttpResponse.BodyHandlers.ofString());

    return response.body();
  }

  private String uploadUserPhoto(String userId, byte[] file)
      throws IOException, InterruptedException {
    HttpClient unauthenticatedClient = HttpClient.newBuilder().build();
    String basePath = "http://localhost:" + FileControllerIT.ContextInitializer.SERVER_PORT;

    HttpResponse<String> response =
        unauthenticatedClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(basePath + "/users/" + userId + "/raw"))
                .header("Authorization", "Bearer " + DOCTOR1_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofByteArray(file))
                .build(),
            HttpResponse.BodyHandlers.ofString());

    return response.body();
  }

  private byte[] download(String fileId) throws IOException, InterruptedException {
    HttpClient unauthenticatedClient = HttpClient.newBuilder().build();
    String basePath = "http://localhost:" + FileControllerIT.ContextInitializer.SERVER_PORT;

    HttpResponse<byte[]> response =
        unauthenticatedClient.send(
            HttpRequest.newBuilder().uri(URI.create(basePath + "/raw/" + fileId)).GET().build(),
            HttpResponse.BodyHandlers.ofByteArray());

    return response.body();
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailablePort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
