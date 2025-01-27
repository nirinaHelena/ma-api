package app.m.advise.testutils;

import static app.m.advise.endpoint.rest.model.User.RoleEnum.ADVISOR;
import static app.m.advise.endpoint.rest.model.User.RoleEnum.DOCTOR;
import static app.m.advise.endpoint.rest.model.User.RoleEnum.PATIENT;
import static java.time.Instant.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.m.advise.endpoint.rest.client.ApiClient;
import app.m.advise.endpoint.rest.model.Appointment;
import app.m.advise.endpoint.rest.model.Channel;
import app.m.advise.endpoint.rest.model.Department;
import app.m.advise.endpoint.rest.model.DepartmentAdvisor;
import app.m.advise.endpoint.rest.model.Doctor;
import app.m.advise.endpoint.rest.model.Feedback;
import app.m.advise.endpoint.rest.model.Hospital;
import app.m.advise.endpoint.rest.model.Message;
import app.m.advise.endpoint.rest.model.Patient;
import app.m.advise.endpoint.rest.model.User;
import app.m.advise.service.api.firebase.FUser;
import app.m.advise.service.api.firebase.FirebaseService;
import app.m.advise.service.api.gemini.conf.GeminiConf;
import app.m.advise.service.file.FileStorageService;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

public class TestUtils {
  public static final String DOCTOR_1_ID = "doctor1_id";
  public static final String DOCTOR1_AUTHENTICATION_ID = "user1_authentication_id";
  public static final String PATIENT1_AUTHENTICATION_ID = "patient1_authentication_id";
  public static final String HOSPITAL1_ID = "hospital1_id";
  public static String DOCTOR1_TOKEN = "doctor1_token";
  public static String PATIENT1_TOKEN = "patient1_token";
  public static String BAD_TOKEN = "bad_token";
  public static String CHANNEL1_ID = "channel1_id";
  public static String CHANNEL2_ID = "channel2_id";
  public static String MESSAGE_ID = "message1_id";
  public static String MESSAGE2_ID = "message2_id";

  public static ApiClient anApiClient(String token, int serverPort) {
    ApiClient client = new ApiClient();
    client.setScheme("http");
    client.setHost("localhost");
    client.setPort(serverPort);
    client.setRequestInterceptor(
        httpRequestBuilder -> httpRequestBuilder.header("Authorization", "Bearer " + token));
    return client;
  }

  public static void setGeminiConf(GeminiConf geminiConfMock) {
    when(geminiConfMock.getModel())
        .thenReturn(new GenerativeModel("dummy", new VertexAI("dummy", "dummy")));
    when(geminiConfMock.generateContent(any()))
        .thenReturn(
            GenerateContentResponse.newBuilder()
                .addAllCandidates(
                    List.of(
                        new Candidate[] {
                          Candidate.newBuilder()
                              .setContent(
                                  Content.newBuilder()
                                      .addParts(Part.newBuilder().setText("Hi").build())
                                      .build())
                              .build()
                        }))
                .build());
  }

  public static void setFirebaseService(FirebaseService firebaseService) {
    when(firebaseService.getUserByBearer(DOCTOR1_TOKEN))
        .thenReturn(new FUser(DOCTOR1_AUTHENTICATION_ID, "user1@email.com"));
    when(firebaseService.getUserByBearer(PATIENT1_TOKEN))
        .thenReturn(new FUser(PATIENT1_AUTHENTICATION_ID, "patient1@email.com"));
  }

  public static void setFileStorageService(FileStorageService fileStorageService)
      throws IOException {
    when(fileStorageService.downloadFile(any())).thenReturn("photo.png".getBytes());
    when(fileStorageService.uploadFile(any(), any())).thenReturn("photo.png");
  }

  public static User user1() {
    return new User()
        .id(DOCTOR_1_ID)
        .birthDate(null)
        .firstName("Ny Hasina")
        .lastName("VAGNO")
        .nic("nyhasina14")
        .email("user1@email.com")
        .photoId("photo_id")
        .sex(User.SexEnum.MALE)
        .role(DOCTOR)
        .authenticationId(DOCTOR1_AUTHENTICATION_ID);
  }

  public static Channel channel1() {
    return new Channel().id(CHANNEL1_ID).creator(doctor1().getId()).invited(patient1().getId());
  }

  public static Channel channel2() {
    return new Channel().id(CHANNEL2_ID).creator(doctor1().getId()).invited(patient2().getId());
  }

  public static Message message() {
    return new Message()
        .id(MESSAGE_ID)
        .senderId(doctor1().getId())
        .receiverId(patient1().getId())
        .content("Hello")
        .attachment(null);
  }

  public static Message message2() {
    return new Message()
        .id(MESSAGE2_ID)
        .senderId(doctor1().getId())
        .receiverId(patient1().getId())
        .content("How are you?")
        .attachment(null);
  }

  public static Doctor doctor1() {
    return new Doctor()
        .id(DOCTOR_1_ID)
        .birthDate(null)
        .firstName("Ny Hasina")
        .lastName("VAGNO")
        .nic("nyhasina14")
        .email("user1@email.com")
        .photoId("photo_id")
        .registryNumber("123456")
        .department(department())
        .role(Doctor.RoleEnum.DOCTOR)
        .sex(Doctor.SexEnum.MALE)
        .authenticationId(DOCTOR1_AUTHENTICATION_ID);
  }

  public static Feedback feedback() {
    return new Feedback().sender(patient2()).score(4).creationDatetime(now()).comment("Nice");
  }

  public static Doctor doctor2() {
    return new Doctor()
        .id("doctor2_id")
        .birthDate(null)
        .firstName("Ny Hasina2")
        .lastName("VAGNO")
        .nic("nyhasina15")
        .email("user2@email.com")
        .photoId("photo2_id")
        .registryNumber("123457")
        .department(department())
        .role(Doctor.RoleEnum.DOCTOR)
        .sex(Doctor.SexEnum.FEMININE)
        .authenticationId("user2_authentication_id");
  }

  public static Department department() {
    return new Department()
        .id(hospital1().getId())
        .name(hospital1().getName())
        .contact(hospital1().getContact())
        .advisor(hospital1().getAdvisor());
  }

  public static User advisor() {
    return new User()
        .id("advisor1_id")
        .birthDate(null)
        .firstName("John")
        .lastName("Doe")
        .nic("advisor")
        .email("advisor@email.com")
        .photoId("photo_id")
        .role(ADVISOR)
        .sex(User.SexEnum.MALE)
        .authenticationId("auth_id");
  }

  public static User toCreate() {
    return new User()
        .id("user2_id")
        .birthDate(null)
        .firstName("user2")
        .lastName("user2_lastname")
        .nic("user214")
        .email("user2@email.com")
        .photoId("photo2_id")
        .role(PATIENT)
        .sex(User.SexEnum.MALE)
        .authenticationId("user2_auth_id");
  }

  public static Appointment appointment1() {
    return new Appointment()
        .id("appointment_id")
        .summary("Asthma")
        .from(null)
        .to(null)
        .organizer(doctor1())
        .participant(patient1());
  }

  public static Appointment appointment2() {
    return new Appointment()
        .id("appointment2_id")
        .summary("Cancer")
        .from(null)
        .to(null)
        .organizer(doctor2())
        .participant(patient2());
  }

  public static Patient patient1() {
    return new Patient()
        .id("patient1_id")
        .firstName("Nicolas")
        .lastName("Jokic")
        .email("patient1@email.com")
        .birthDate(null)
        .authenticationId("patient1_authentication_id")
        .photoId("photo_id")
        .nic("151616232626")
        .role(Patient.RoleEnum.PATIENT)
        .sex(Patient.SexEnum.MALE)
        .doctorId(doctor1().getId());
  }

  public static Patient patient2() {
    return new Patient()
        .id("patient2_id")
        .firstName("Russel")
        .lastName("Westbrook")
        .email("patient2@email.com")
        .birthDate(null)
        .authenticationId("patient2_authentication_id")
        .photoId("photo2_id")
        .nic("151616232627")
        .role(Patient.RoleEnum.PATIENT)
        .sex(Patient.SexEnum.MALE)
        .doctorId(doctor2().getId());
  }

  public static Hospital hospital1() {
    return new Hospital()
        .id(HOSPITAL1_ID)
        .name("HJRA")
        .stat("STAT123456789")
        .nif("NIF123456789")
        .contact("+261324063616")
        .advisor(null);
  }

  public static Hospital hospital2() {
    return new Hospital()
        .id("hospital2_id")
        .name("CHU")
        .stat("STAT1234567810")
        .nif("NIF1234567810")
        .contact("+261324063617")
        .advisor(new DepartmentAdvisor().schemas(advisor()));
  }

  public static int anAvailablePort() {
    try {
      return new ServerSocket(0).getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
