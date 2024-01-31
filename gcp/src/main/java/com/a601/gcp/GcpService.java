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
public class GcpService {

  public static Map<String, Object> detectFace(String imgPath) throws IOException {
    Map<String, Object> result = new HashMap<>();
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
          result.put("status", "error");
          result.put("errorMessage", res.getError().getMessage());
          return result;
        }
        String imageInformation = "";
        for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
          imageInformation = String.format(
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
        result.put("status", "success");
        result.put("imgInfo", imageInformation);
      }
    }
    return result;
  }
}
