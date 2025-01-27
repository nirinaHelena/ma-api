package app.m.advise.integration;

import static app.m.advise.testutils.TestUtils.DOCTOR1_TOKEN;
import static app.m.advise.testutils.TestUtils.DOCTOR_1_ID;
import static app.m.advise.testutils.TestUtils.HOSPITAL1_ID;
import static app.m.advise.testutils.TestUtils.anAvailablePort;
import static app.m.advise.testutils.TestUtils.doctor1;
import static app.m.advise.testutils.TestUtils.doctor2;
import static app.m.advise.testutils.TestUtils.setFileStorageService;
import static app.m.advise.testutils.TestUtils.setFirebaseService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import app.m.advise.AbstractContextInitializer;
import app.m.advise.endpoint.rest.api.DepartmentApi;
import app.m.advise.endpoint.rest.api.UserApi;
import app.m.advise.endpoint.rest.client.ApiClient;
import app.m.advise.endpoint.rest.client.ApiException;
import app.m.advise.endpoint.rest.model.Doctor;
import app.m.advise.service.api.firebase.FirebaseService;
import app.m.advise.service.file.FileStorageService;
import app.m.advise.testutils.TestUtils;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = DoctorControllerIT.ContextInitializer.class)
@AutoConfigureMockMvc
class DoctorControllerIT {
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
  void read_doctor_by_id_ok() throws ApiException {
    ApiClient client = anApiClient(DOCTOR1_TOKEN);
    UserApi api = new UserApi(client);

    var actual = api.getDoctorById(DOCTOR_1_ID);

    assertEquals(doctor1(), actual);
  }

  @Test
  void crupdate_doctor_ok() throws ApiException {
    ApiClient client = anApiClient(DOCTOR1_TOKEN);
    UserApi api = new UserApi(client);

    var actual = api.crupdateDoctor(DOCTOR_1_ID, doctor1().nic("3214457888"));

    assertEquals(doctor1().nic("3214457888"), actual);
  }

  @Test
  void read_doctors_by_department_ok() throws ApiException {
    ApiClient client = anApiClient(DOCTOR1_TOKEN);
    DepartmentApi api = new DepartmentApi(client);

    List<Doctor> actual = api.getDoctorsByHospitalsId(HOSPITAL1_ID);

    assertEquals(2, actual.size());
    assertTrue(actual.contains(doctor2()));
  }

  @Test
  void read_doctors() throws ApiException {
    ApiClient client = anApiClient(DOCTOR1_TOKEN);
    UserApi api = new UserApi(client);

    List<Doctor> actual = api.getDoctors(null, null);
    List<Doctor> actualFiltered = api.getDoctors(doctor1().getFirstName(), null);

    assertEquals(2, actual.size());
    assertTrue(actual.contains(doctor1()));
    assertEquals(doctor1(), actualFiltered.get(0));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailablePort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
