package tetish_inn_backend.tetish_inn.common.file_upload;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        long MAX_FILE_SIZE = 100 * 1024 * 1024;

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("Failed to store file. The file size exceeds the limit of 100 MB.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String filename = timestamp + "_" + originalFilename;
        String uploadDir = System.getProperty("user.home") + "/tet_store/files/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(filename);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new IOException("Failed to store file " + filename, e);
        }
    }
}



