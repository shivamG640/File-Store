package org.test;

import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.io.File;

import java.util.*;

@jakarta.ws.rs.Path("/files")
public class FileStoreResource {

    @Inject
    FileService fileService;

    @POST
    @jakarta.ws.rs.Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public RestResponse<String> uploadFiles(@RestForm(FileUpload.ALL) List<FileUpload> files) {
        return fileService.uploadFiles(files);
    }
    @GET
    @jakarta.ws.rs.Path("/download/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public RestResponse<File> downloadFile(@RestPath String filename) {
        return fileService.downloadFile(filename);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> listFiles() {
        return fileService.listFiles();
    }

    @DELETE
    @jakarta.ws.rs.Path("/delete/{filename}")
    public RestResponse<String> deleteFile(@RestPath String filename) {
        return fileService.deleteFile(filename);
    }

    @POST
    @jakarta.ws.rs.Path("/update")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public RestResponse<String> updateOrCreateFile(@RestForm FileUpload file) {
        return fileService.updateOrCreateFile(file);
    }

    @GET
    @jakarta.ws.rs.Path("/wordcount")
    public RestResponse<Long> getTotalWordCount() {
        return fileService.getTotalWordCount();
    }

    @GET
    @jakarta.ws.rs.Path("/frequentwords")
    public RestResponse<List<Map.Entry<String, Long>>> getMostFrequentWords() {
        return fileService.getMostFrequentWords();
    }
}
