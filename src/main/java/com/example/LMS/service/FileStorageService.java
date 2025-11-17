package com.example.LMS.service;

import com.example.LMS.enums.ImageType;
import com.example.LMS.enums.ObjectType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path baseDir = Paths.get(System.getProperty("user.dir") + "/upload");

    public FileStorageService() throws IOException {
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
    }

    public String save(MultipartFile file, ObjectType objectType, ImageType fileType) throws IOException {
        Path folder = baseDir.resolve(objectType.name().toLowerCase()).resolve(fileType.name().toLowerCase());

        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = folder.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn web (có thể map với /uploads/ trong controller)
        return "/uploads/" + objectType.name().toLowerCase() + "/" + fileType.name().toLowerCase() + "/" + filename;
    }

    public void deleteFiles(List<String> files){
        if(files != null && !files.isEmpty()){
            for(String file : files){
                try{
                    Path path = Paths.get(System.getProperty("user.dir") + file);
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
