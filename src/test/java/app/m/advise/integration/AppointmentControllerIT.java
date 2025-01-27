package app.m.advise.integration;

import static app.m.advise.testutils.TestUtils.DOCTOR1_TOKEN;
import static app.m.advise.testutils.TestUtils.DOCTOR_1_ID;
import static app.m.advise.testutils.TestUtils.PATIENT1_TOKEN;
import static app.m.advise.testutils.TestUtils.anAvailablePort;
import static app.m.advise.testutils.TestUtils.appointment1;
import static app.m.advise.testutils.TestUtils.appointment2;
import static app.m.advise.testutils.TestUtils.patient1;
import static app.m.advise.testutils.TestUtils.setFileStorageService;
import static app.m.advise.testutils.TestUtils.setFirebaseService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import app.m.advise.AbstractContextInitializer;
import app.m.advise.endpoint.rest.api.ActivityApi;
import app.m.advise.endpoint.rest.client.ApiClient;
import app.m.advise.endpoint.rest.client.ApiException;
import app.m.advise.service.api.firebase.FirebaseService;
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
@ContextConfiguration(initializers = AppointmentControllerIT.ContextInitializer.class)
@AutoConfigureMockMvc
class AppointmentControllerIT {
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
  void read_appointment_by_id() throws ApiException {
    ApiClient client = anApiClient(DOCTOR1_TOKEN);
    ActivityApi api = new ActivityApi(client);

    var actual = api.readAppointment("appointment_id");

    assertEquals(appointment1(), actual);
  }

  @Test
  void read_appointment_by_doctor_id() throws ApiException {
    ApiClient client = anApiClient(DOCTOR1_TOKEN);
    ActivityApi api = new ActivityApi(client);

    var actual = api.getDoctorAppointments(DOCTOR_1_ID);

    assertTrue(actual.contains(appointment1()));
  }

  @Test
  void read_appointment_by_patient_id() throws ApiException {
    ApiClient client = anApiClient(PATIENT1_TOKEN);
    ActivityApi api = new ActivityApi(client);

    var actual = api.getPatientsAppointments(patient1().getId());

    assertTrue(actual.contains(appointment1()));
  }

  @Test
  void crupdate_appointment_by_id() throws ApiException {
    ApiClient client = anApiClient(DOCTOR1_TOKEN);
    ActivityApi api = new ActivityApi(client);

    var actual = api.crupdateAppointment("appointment2_id", appointment2());

    assertNotNull(actual.getRoomId());
    assertEquals(appointment2(), actual.roomId(null));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailablePort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
