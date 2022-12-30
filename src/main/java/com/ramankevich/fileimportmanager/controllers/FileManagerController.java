package com.ramankevich.fileimportmanager.controllers;

import com.ramankevich.fileimportmanager.exceptions.CustomException;
import com.ramankevich.fileimportmanager.services.FileManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/files")
public class FileManagerController {

    @Autowired
    FileManagerService fileManagerService;

    @ResponseBody
    @RequestMapping(value = "/upload-multiple", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadMultipleFiles(@RequestParam("file") MultipartFile[] files) {
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (!fileExtension.equals("csv") && !fileExtension.equals("xml") && !fileExtension.equals("txt")) {
                throw new CustomException(400, "Error: Unsupported file format. Only CSV, XML, and TXT files are allowed.");
            }
        }

        return fileManagerService.processFiles(files);
    }

    @ResponseBody
    @RequestMapping(value = "/records", method = RequestMethod.GET)
    public List<String[]> getAllRecords() {
        return fileManagerService.getRecords();
    }
}
