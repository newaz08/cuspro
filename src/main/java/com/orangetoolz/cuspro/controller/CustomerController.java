package com.orangetoolz.cuspro.controller;

import com.orangetoolz.cuspro.entity.Customer;
import com.orangetoolz.cuspro.entity.InvalidCustomer;
import com.orangetoolz.cuspro.service.CustomerServiceImplementation;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Newaz Sharif
 */

@RestController
@RequestMapping("/api/customer")
@Api(tags = "Customers")
public class CustomerController {

    @Autowired
    CustomerServiceImplementation customerService;

    @ApiOperation(value = "Upload valid and invalid customer data into separate table based on email and phone validation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully file uploaded"),
            @ApiResponse(code = 400, message = "File is Empty")
    })
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam(value = "file") @ApiParam(value = "comma separated text file", required = true)
                        MultipartFile file) {

        if (file.isEmpty())
            return new ResponseEntity<>("File is Empty", HttpStatus.BAD_REQUEST);

        try {
            customerService.processFile(file);
            return new ResponseEntity<>("Successfully File Uploaded", HttpStatus.OK);
        } catch (IOException exception) {
            return new ResponseEntity<>("Exception occurred", HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "Export valid customer based on email and phone validation")
    @ApiResponse(code = 200, message = "", response = Customer.class)
    @PostMapping("/valid-data-export")
    public void customerDataExport(HttpServletResponse response) {

        customerService.exportValidCustomer(response);
    }

    @ApiOperation(value = "Export invalid customer based on email and phone validation")
    @ApiResponse(code = 200, message = "", response = InvalidCustomer.class)
    @PostMapping("/invalid-data-export")
    public void customerInvalidDataExport(HttpServletResponse response)  {

        customerService.exportInvalidCustomer(response);
    }
}
