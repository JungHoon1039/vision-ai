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
import java.util.List;

public class DetectFacesGcs {

  /*
  얼굴 감지 후 즐거움, 슬픔, 화남, 놀람, 분석신뢰도, 노출정도, 흐림정도, 모자 착용 여부를 출력한다.
  분석신뢰도는 0~1점 사이의 소수로 표현된다.
  그 외의 값은 1~5점 사이의 정수로 표현된다.
  각 정수 값은 아래와 같은 표현으로 대체 가능하다.
  VERY_UNLIKELY, UNLIKELY, POSSIBLE, LIKELY, VERY_LIKELY (분석 실패 시 UNKNOWN)
  */
  public static void detectFacesGcs(String imgPath) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();
    ImageSource imgSource = ImageSource.newBuilder().setImageUri(imgPath).build();
    Image img = Image.newBuilder().setSource(imgSource).build();
    Feature feat = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          System.out.format("Error: %s%n", res.getError().getMessage());
          return;
        }

        for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
          System.out.format(
              "emotions %n"
                  + "joy: %s, point: %s%n"
                  + "sorrow: %s, point: %s%n"
                  + "anger: %s, point: %s%n"
                  + "surprise: %s, point: %s%n"
                  + "information %n"
                  + "confidence: %s%n"
                  + "under_exposed: %s, point: %s%n"
                  + "blurred: %s, point: %s%n"
                  + "headwear: %s, point: %s%n"
              ,
              annotation.getJoyLikelihood(), annotation.getJoyLikelihoodValue(),
              annotation.getSorrowLikelihood(), annotation.getSorrowLikelihoodValue(),
              annotation.getAngerLikelihood(), annotation.getAngerLikelihoodValue(),
              annotation.getSurpriseLikelihood(), annotation.getSurpriseLikelihoodValue(),
              annotation.getDetectionConfidence(),
              annotation.getUnderExposedLikelihood(), annotation.getUnderExposedLikelihoodValue(),
              annotation.getBlurredLikelihood(), annotation.getBlurredLikelihoodValue(),
              annotation.getHeadwearLikelihood(), annotation.getHeadwearLikelihoodValue()
          );
        }
      }
    }
  }
}