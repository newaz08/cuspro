package com.orangetoolz.cuspro.service;

import com.orangetoolz.cuspro.common.ApplicationConstant;
import com.orangetoolz.cuspro.common.CustomerFileDataPrepare;
import com.orangetoolz.cuspro.common.CustomerSet;
import com.orangetoolz.cuspro.entity.Customer;
import com.orangetoolz.cuspro.entity.InvalidCustomer;
import com.orangetoolz.cuspro.repository.CustomerRepository;
import com.orangetoolz.cuspro.repository.InvalidCustomerRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class CustomerServiceImplementation implements FileImportService, FileExportService {

    private int fileNumber = 1;
    @Autowired
    private CustomerFileDataPrepare customerFileDataPrepare;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    InvalidCustomerRepository invalidCustomerRepository;
    @Autowired
    ModelMapper modelMapper;

    public CustomerServiceImplementation(CustomerFileDataPrepare customerFileDataPrepare) {
        this.customerFileDataPrepare = customerFileDataPrepare;
    }


    public void processFile(MultipartFile file) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        Set<Customer> chunkOfCustomer = new HashSet<>(ApplicationConstant.IMPORT_DATA_CHUNK_SIZE);
        List<InvalidCustomer> chunkOfInvalidCustomer = new ArrayList<>(ApplicationConstant.IMPORT_DATA_CHUNK_SIZE);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Set<Customer> validCustomer = new CustomerSet<>();
        Set<InvalidCustomer> invalidNotDuplicate = new HashSet<>();

        while ((line = reader.readLine()) != null) {

            processLine(line, chunkOfCustomer, chunkOfInvalidCustomer, executorService, validCustomer, invalidNotDuplicate);
        }

        importRemainingValidCustomerData(chunkOfCustomer, executorService);

        importRemaingInvalidCustomerData(chunkOfInvalidCustomer, executorService);

        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            exception.printStackTrace();
        }
    }

    private void importRemaingInvalidCustomerData(List<InvalidCustomer> chunkOfInvalidCustomer,
                                                  ExecutorService executorService) {
        if(!chunkOfInvalidCustomer.isEmpty()) {
            List<InvalidCustomer> invalidCustomerCopy = new ArrayList<>(chunkOfInvalidCustomer);
            executorService.execute(() -> importInvalidCustomer(invalidCustomerCopy));
            chunkOfInvalidCustomer.clear();
        }
    }

    private void importRemainingValidCustomerData(Set<Customer> chunkOfCustomer, ExecutorService executorService) {
        if(!chunkOfCustomer.isEmpty()) {
            Set<Customer> chunkCopy = new HashSet<>(chunkOfCustomer);
            executorService.execute(() -> importValidCustomer(chunkCopy));
            chunkOfCustomer.clear();
        }
    }

    private void processLine(
            String line, Set<Customer> chunkOfCustomer, List<InvalidCustomer> chunkOfInvalidCustomer,
                    ExecutorService executorService, Set<Customer> validCustomer,
                            Set<InvalidCustomer> invalidNotDuplicate) {
        Customer customer = customerFileDataPrepare.parseFile(line);
        if(customer != null) {
            processCustomerData(chunkOfCustomer, chunkOfInvalidCustomer, executorService,
                                        validCustomer, invalidNotDuplicate, customer);
        }
    }

    private void processCustomerData(
            Set<Customer> chunkOfCustomer, List<InvalidCustomer> chunkOfInvalidCustomer,
                        ExecutorService executorService, Set<Customer> validCustomer,
                                Set<InvalidCustomer> invalidNotDuplicate, Customer customer) {
        if(validCustomer.add(customer)) {
            processValidCustomerData(chunkOfCustomer, executorService, customer);
        } else {
            processInvalidCustomerData(chunkOfInvalidCustomer, executorService, validCustomer,
                                    invalidNotDuplicate, customer);
        }
    }

    private void processInvalidCustomerData(
            List<InvalidCustomer> chunkOfInvalidCustomer, ExecutorService executorService,
                    Set<Customer> validCustomer, Set<InvalidCustomer> invalidNotDuplicate, Customer customer) {
        InvalidCustomer invalidCustomer = modelMapper.map(customer, InvalidCustomer.class);
        if(!validCustomer.contains(customer) && invalidNotDuplicate.add(invalidCustomer)) {
            chunkOfInvalidCustomer.add(invalidCustomer);
            if(chunkOfInvalidCustomer.size() == ApplicationConstant.IMPORT_DATA_CHUNK_SIZE) {
                List<InvalidCustomer> invalidCustomerCopy = new ArrayList<>(chunkOfInvalidCustomer);
                executorService.execute(() -> importInvalidCustomer(invalidCustomerCopy));
                chunkOfInvalidCustomer.clear();
            }
        }
    }

    private void processValidCustomerData(Set<Customer> chunkOfCustomer, ExecutorService executorService, Customer customer) {
        chunkOfCustomer.add(customer);
        if(chunkOfCustomer.size() == ApplicationConstant.IMPORT_DATA_CHUNK_SIZE) {
            Set<Customer> chunkCopy = new HashSet<>(chunkOfCustomer);
            executorService.execute(() -> importValidCustomer(chunkCopy));
            chunkOfCustomer.clear();
        }
    }

    @Override
    public void importValidCustomer(Set<Customer> validCustomers) {
        customerRepository.saveAll(validCustomers);
    }

    @Override
    public void importInvalidCustomer(List<InvalidCustomer> invalidCustomers) {
        invalidCustomerRepository.saveAll(invalidCustomers);
    }

    @Override
    public void exportValidCustomer(HttpServletResponse response) {

        int startChunk = 0;
        int endChunk = ApplicationConstant.EXPORT_DATA_CHUNK_SIZE;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        long startTime = System.currentTimeMillis();
        while (true) {
            Pageable pageable = PageRequest.of(startChunk, ApplicationConstant.EXPORT_DATA_CHUNK_SIZE);
            CopyOnWriteArrayList<Customer> customers = customerRepository.findBetween(pageable);
            if(customers.isEmpty()) {
                break;
            }

            executorService.execute(() -> {
                try {
                    exportCustomerDataIntoOutputStream(customers, response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            startChunk = endChunk + 1;

        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Export valid customer process execution time= "+(endTime - startTime)+" ms");
    }

    @Override
    public void exportInvalidCustomer(HttpServletResponse response) {

        List<InvalidCustomer> invalidCustomers = invalidCustomerRepository.findAllInvalidCustomer();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        long startTime = System.currentTimeMillis();
        executorService.execute(() -> {
            try {
                exportInvalidCustomerDataIntoOutputStream(invalidCustomers, response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Export invalid customer process execution time= "+(endTime - startTime)+" ms");
    }

    private void exportCustomerDataIntoOutputStream(
            CopyOnWriteArrayList<Customer> customers,HttpServletResponse response) throws IOException{

        String fileName ="customers_"+fileNumber+".csv";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName="+fileName);
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_ENCODING, "identity");

        ServletOutputStream outputStream = response.getOutputStream();

        try (CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream), CSVFormat.DEFAULT)) {

            customers.stream().forEach(customer -> {
                try {
                    csvPrinter.printRecord(customer.getFirstName(), customer.getLastName(), customer.getCity(),
                            customer.getState(), customer.getZipCode(),customer.getPhone(), customer.getEmail(),
                            customer.getIpAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
        }
        outputStream.flush();
    }

    private void exportInvalidCustomerDataIntoOutputStream(
            List<InvalidCustomer> invalidCustomers,HttpServletResponse response) throws IOException{

        String fileName ="Invalid_Customers"+".csv";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName="+fileName);
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_ENCODING, "identity");

        ServletOutputStream outputStream = response.getOutputStream();

        try (CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream), CSVFormat.DEFAULT)) {

            invalidCustomers.stream().forEach(invalidCustomer -> {
                try {
                    csvPrinter.printRecord(invalidCustomer.getFirstName(), invalidCustomer.getLastName(),
                            invalidCustomer.getCity(), invalidCustomer.getState(), invalidCustomer.getZipCode(),
                            invalidCustomer.getPhone(), invalidCustomer.getEmail(), invalidCustomer.getIpAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
        }
        outputStream.flush();
    }
}
