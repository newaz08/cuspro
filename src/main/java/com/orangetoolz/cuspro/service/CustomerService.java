package com.orangetoolz.cuspro.service;

import com.orangetoolz.cuspro.common.CustomerFileDataPrepare;
import com.orangetoolz.cuspro.common.CustomerSet;
import com.orangetoolz.cuspro.entity.Customer;
import com.orangetoolz.cuspro.entity.InvalidCustomer;
import com.orangetoolz.cuspro.repository.CustomerRepository;
import com.orangetoolz.cuspro.repository.InvalidCustomerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class CustomerService implements CustomerFileImportService{

    private static final int CHUNK_SIZE = 10000;

    @Autowired
    private CustomerFileDataPrepare customerFileDataPrepare;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    InvalidCustomerRepository invalidCustomerRepository;
    @Autowired
    ModelMapper modelMapper;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    public void processFile(MultipartFile file) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        Set<Customer> chunkOfCustomer = new HashSet<>(CHUNK_SIZE);
        List<InvalidCustomer> chunkOfInvalidCustomer = new ArrayList<>(CHUNK_SIZE);

        Set<Customer> validCustomer = new CustomerSet<>();
        while ((line = reader.readLine()) != null) {

            Customer customer = customerFileDataPrepare.parseFile(line);
            if(customer != null) {
                if(validCustomer.add(customer)) {
                    chunkOfCustomer.add(customer);
                    if(chunkOfCustomer.size() == CHUNK_SIZE) {
                        Set<Customer> chunkCopy = new HashSet<>(chunkOfCustomer);
                        executorService.execute(() -> importValidCustomer(chunkCopy));
                        chunkOfCustomer.clear();
                    }
                } else {
                    InvalidCustomer invalidCustomer = modelMapper.map(customer, InvalidCustomer.class);
                    chunkOfInvalidCustomer.add(invalidCustomer);
                    if(chunkOfInvalidCustomer.size() == CHUNK_SIZE) {
                        List<InvalidCustomer> invalidCustomerCopy = new ArrayList<>(chunkOfInvalidCustomer);
                        executorService.execute(() -> importInvalidCustomer(invalidCustomerCopy));
                        chunkOfInvalidCustomer.clear();
                    }
                }
            }
        }

        if(!chunkOfCustomer.isEmpty()) {
            Set<Customer> chunkCopy = new HashSet<>(chunkOfCustomer);
            executorService.execute(() -> importValidCustomer(chunkCopy));
            chunkOfCustomer.clear();
        }

        if(!chunkOfInvalidCustomer.isEmpty()) {
            List<InvalidCustomer> invalidCustomerCopy = new ArrayList<>(chunkOfInvalidCustomer);
            executorService.execute(() -> importInvalidCustomer(invalidCustomerCopy));
            chunkOfInvalidCustomer.clear();
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
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

}
