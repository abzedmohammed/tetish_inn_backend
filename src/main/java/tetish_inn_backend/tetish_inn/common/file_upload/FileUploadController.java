package tetish_inn_backend.tetish_inn.common.file_upload;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tetish_inn_backend.tetish_inn.common.utils.ApiResponse;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/upload")
public class FileUploadController {
    public final FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        String filename = fileUploadService.saveFile(file);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", filename));
    }
}
