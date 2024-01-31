package com.a601.gcp;

import static com.a601.gcp.DetectFacesGcs.detectFacesGcs;

import java.io.IOException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class Controller {

    @GetMapping("/gcp")
    public void initializeSession() throws IOException {
        System.out.println("Hello World");
        detectFacesGcs("https://png.pngtree.com/thumb_back/fw800/background/20230424/pngtree-sad-old-lady-wiping-her-eye-with-a-tissue-image_2501431.jpg");
    }
}
