package com.orangetoolz.cuspro.controller;

import com.orangetoolz.cuspro.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    CustomerService customerService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam(value = "file") MultipartFile file) {

        if (file.isEmpty())
            return new ResponseEntity<>("File is Empty", HttpStatus.BAD_REQUEST);

        try {
            customerService.processFile(file);
            return new ResponseEntity<>("Successfully File Uploaded", HttpStatus.OK);
        } catch (IOException exception) {
            return new ResponseEntity<>("Exception occurred", HttpStatus.BAD_REQUEST);
        }
    }
}
