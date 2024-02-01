package com.a601.gcp;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
// @Transactional(readOnly = true)
public class GcpService {

  public static Map<String, Object> detectFace(String imgPath) throws IOException {
    Map<String, Object> result = new HashMap<>();
    List<AnnotateImageRequest> requests = new ArrayList<>();
    ImageSource imgSource = ImageSource.newBuilder().setImageUri(imgPath).build();
    Image img = Image.newBuilder().setSource(imgSource).build();
    Feature feat = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);
    ImageAnnotatorClient client = ImageAnnotatorClient.create();
    BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
    List<AnnotateImageResponse> responses = response.getResponsesList();
    AnnotateImageResponse res = responses.get(0);

    if (res.toString().isEmpty()) {
      result.put("status", "fail");
      return result;
    }
    if (res.hasError()) {
      result.put("status", "error");
      result.put("errorMessage", res.getError().getMessage());
      return result;
    }
    FaceAnnotation annotation = res.getFaceAnnotationsList().get(0);
    result.put("status", "success");
    result.put("score", calculateScore(annotation));
    return result;
  }

  private static int calculateScore(FaceAnnotation annotation) {
    int joy = annotation.getJoyLikelihoodValue();
    int sorrow = annotation.getSorrowLikelihoodValue();
    int anger = annotation.getAngerLikelihoodValue();
    int surprise = annotation.getSurpriseLikelihoodValue();
    int underExposure = annotation.getUnderExposedLikelihoodValue();
    int blur = annotation.getBlurredLikelihoodValue();
    int hat = annotation.getHeadwearLikelihoodValue();
    double confidence = annotation.getDetectionConfidence();

    // 가중치
    double[] weights = {0.3, 0.2, 0.2, 0.1, 0.1, 0.1, 0};

    // 각 정보에 대한 점수와 가중치를 곱한 후 합산
    double weightedSum = (joy * weights[0] +
        (6 - sorrow) * weights[1] +
        (6 - anger) * weights[2] +
        surprise * weights[3] +
        (6 - underExposure) * weights[4] +
        (6 - blur) * weights[5] +
        hat * weights[6]) * confidence;

    // 최종 점수를 0에서 100 사이로 매핑
    return (int) Math.round(weightedSum * 20);
  }
}
