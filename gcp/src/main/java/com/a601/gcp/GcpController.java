package com.a601.gcp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/gcp"})
@CrossOrigin(origins = "*")
public class GcpController {

  private final GcpService gcpService;

  @PostMapping("/img")
  public ResponseEntity<String> analysisImage(@RequestBody(required = false) Map<String, Object> params)
      throws IOException {
    Map<String, Object> result = gcpService.detectFace(params.get("imgPath").toString());
    if (result.get("status").toString().equals("fail")) {
      return new ResponseEntity<>("Face Detect Fail", HttpStatus.NOT_FOUND);
    }
    if (result.get("status").toString().equals("error")) {
      return new ResponseEntity<>(result.get("errorMessage").toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(result.get("score").toString(), HttpStatus.OK);
  }

  @PostMapping("/save")
  public void decode(@RequestBody(required = false) Map<String, Object> params) throws IOException {
    byte[] decodedBytes = Base64.getDecoder().decode(params.get("base64Data").toString());

    // 파일 이름으로 conferenceId와 현재 시간을 사용
    LocalDateTime currentTime = LocalDateTime.now(); // 현재 시간을 가져옴
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME; // 날짜와 시간을 ISO 8601 형식으로 포맷
    String formattedDateTime = currentTime.format(formatter); // 현재 시간을 포맷에 맞게 변환

    // 임시 파일 객체 생성
    File tempFile = File.createTempFile("image_" + formattedDateTime, ".jpg");
    // 임시 파일 저장
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write(decodedBytes);
    }

    // MultipartFile로 변환
    // 파라미터 이름
    // 파일명
    // 파일 타입 - null 지정 시 자동 추론 - 필요한 경우 image/jpeg, image/png 등으로 지정
    // 파일 데이터 - 임시 파일 읽어와서 활용
    MultipartFile multipartFile = new MockMultipartFile("file", tempFile.getName(), null, decodedBytes);

    String desktopPath = "C:\\Users\\SSAFY\\Desktop\\";
    String fileName = 15 + formattedDateTime + ".jpg";
    String filePath = desktopPath + fileName;
    File file = new File(filePath);
    try (InputStream inputStream = multipartFile.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(file)) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    } finally {
      // 파일 사용 후 임시 파일 삭제
      if (tempFile.exists()) {
        tempFile.delete();
      }
    }
  }
}
