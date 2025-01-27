package app.m.advise.integration;

import static app.m.advise.testutils.TestUtils.DOCTOR1_TOKEN;
import static app.m.advise.testutils.TestUtils.anAvailablePort;
import static app.m.advise.testutils.TestUtils.doctor1;
import static app.m.advise.testutils.TestUtils.setFileStorageService;
import static app.m.advise.testutils.TestUtils.setFirebaseService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import app.m.advise.AbstractContextInitializer;
import app.m.advise.endpoint.rest.api.AiApi;
import app.m.advise.endpoint.rest.client.ApiClient;
import app.m.advise.endpoint.rest.client.ApiException;
import app.m.advise.endpoint.rest.model.UserPrompt;
import app.m.advise.service.api.firebase.FirebaseService;
import app.m.advise.service.api.gemini.GeminiService;
import app.m.advise.service.file.FileStorageService;
import app.m.advise.testutils.TestUtils;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = AIControllerIT.ContextInitializer.class)
@AutoConfigureMockMvc
class AIControllerIT {
  private static final String EXPECTED_AI_RESPONSE = "dummy response";
  @MockBean private FirebaseService firebaseServiceMock;
  @MockBean private FileStorageService fileStorageService;
  @MockBean private GeminiService geminiService;

  @BeforeEach
  void setUp() throws IOException {
    setFirebaseService(firebaseServiceMock);
    setFileStorageService(fileStorageService);

    when(geminiService.generateContent(any(String.class))).thenReturn(EXPECTED_AI_RESPONSE);
  }

  @Test
  void try_to_prompt_ok() throws ApiException {
    ApiClient client = anApiClient(DOCTOR1_TOKEN);
    AiApi api = new AiApi(client);

    var actual = api.sendPrompt(prompt());

    assertEquals(EXPECTED_AI_RESPONSE, actual.getContent());
  }

  UserPrompt prompt() {
    return new UserPrompt().content("dummy content").userId(doctor1().getId()).attachement(null);
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailablePort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
