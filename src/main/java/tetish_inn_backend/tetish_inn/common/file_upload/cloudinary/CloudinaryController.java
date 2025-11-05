package tetish_inn_backend.tetish_inn.common.file_upload.cloudinary;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tetish_inn_backend.tetish_inn.common.utils.ApiResponse;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cloudinary")
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Object>> upload(
            @RequestParam("file") MultipartFile file) throws IOException {

        String url = cloudinaryService.uploadFile(file);
        return ResponseEntity.ok().body(ApiResponse.success("File uploaded successfully", url));
    }
}
