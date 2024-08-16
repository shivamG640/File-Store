package org.test;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import jakarta.ws.rs.core.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class FileService {
    private static final Path FILE_STORE_PATH = Paths.get("filestore-uploads");

    public FileService() {
        try {
            Files.createDirectories(FILE_STORE_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public RestResponse<String> uploadFiles(List<FileUpload> files) {
        try {
            for (FileUpload fileUpload : files) {
                Path filePath = FILE_STORE_PATH.resolve(fileUpload.fileName());
                Files.copy(fileUpload.uploadedFile(), filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return ResponseBuilder.ok("Files uploaded successfully").build();
        } catch (Exception e) {
            return ResponseBuilder.<String>create(RestResponse.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to upload files")
                    .build();
        }
    }

    public RestResponse<File> downloadFile(String filename) {
        try {
            Path filePath = FILE_STORE_PATH.resolve(filename);
            if (Files.exists(filePath)) {
                File file = filePath.toFile();
                return ResponseBuilder.ok(file)
                        .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                        .build();
            } else {
                return ResponseBuilder.<File>create(RestResponse.Status.NOT_FOUND)
                        .entity(null)  // Set entity as null since it's a 404
                        .header("Content-Type", MediaType.TEXT_PLAIN)
                        .build();
            }
        } catch (Exception e) {
            return ResponseBuilder.<File>create(RestResponse.Status.INTERNAL_SERVER_ERROR)
                    .entity(null)  // Set entity as null since it's a 500
                    .header("Content-Type", MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    public List<String> listFiles() {
        try {
            return Files.list(FILE_STORE_PATH)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Could not list files", e);
        }
    }

    public RestResponse<String> deleteFile(String filename) {
        try {
            Path filePath = FILE_STORE_PATH.resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return ResponseBuilder.ok("File deleted successfully").build();
            } else {
                return ResponseBuilder.<String>create(RestResponse.Status.NOT_FOUND)
                        .entity("File not found")
                        .build();
            }
        } catch (Exception e) {
            return ResponseBuilder.<String>create(RestResponse.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to delete file")
                    .build();
        }
    }

    public RestResponse<String> updateOrCreateFile(FileUpload file) {
        try {
            Path filePath = FILE_STORE_PATH.resolve(file.fileName());

            // If the file exists, update its contents; otherwise, create a new file
            if (Files.exists(filePath)) {
                Files.write(filePath, Files.readAllBytes(file.uploadedFile()), StandardOpenOption.TRUNCATE_EXISTING);
                return ResponseBuilder.ok("File updated successfully").build();
            } else {
                Files.copy(file.uploadedFile(), filePath);
                return ResponseBuilder.ok("File created successfully: " + file.fileName()).build();
            }
        } catch (IOException e) {
            return ResponseBuilder.<String>create(RestResponse.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to update or create file")
                    .build();
        }
    }

    public RestResponse<Long> getTotalWordCount() {
        long totalWordCount = 0;

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(FILE_STORE_PATH)) {
            for (Path filePath : directoryStream) {
                if (Files.isRegularFile(filePath)) {
                    totalWordCount += WordCounter.countWordsInFile(filePath);
                }
            }
            return ResponseBuilder.ok(totalWordCount).build();
        } catch (IOException e) {
            return ResponseBuilder.<Long>create(RestResponse.Status.INTERNAL_SERVER_ERROR)
                    .entity(0L)  // Returning 0 as the word count in case of error
                    .build();
        }
    }

    public RestResponse<List<Map.Entry<String, Long>>> getMostFrequentWords() {
        Map<String, Long> wordCounts = new HashMap<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(FILE_STORE_PATH)) {
            for (Path filePath : directoryStream) {
                if (Files.isRegularFile(filePath)) {
                    WordCounter.countWordsInFile(filePath, wordCounts);
                }
            }

            // Get the 10 most frequent words
            List<Map.Entry<String, Long>> topWords = wordCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toList());

            return ResponseBuilder.ok(topWords).build();
        } catch (IOException e) {
            return ResponseBuilder.<List<Map.Entry<String, Long>>>create(RestResponse.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.emptyList())
                    .build();
        }
    }

}
