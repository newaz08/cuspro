package com.orangetoolz.cuspro.service;

import com.orangetoolz.cuspro.common.CustomerFileDataPrepare;
import com.orangetoolz.cuspro.entity.Customer;
import com.orangetoolz.cuspro.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
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

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    public void processFile(MultipartFile file) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        Set<Customer> chunkOfCustomer = new HashSet<>(CHUNK_SIZE);

        while ((line = reader.readLine()) != null) {

            Customer customer = customerFileDataPrepare.parseFile(line);
            if(customer != null) {
                chunkOfCustomer.add(customer);
                if(chunkOfCustomer.size() == CHUNK_SIZE) {
                    executorService.execute(() -> importValidCustomer(chunkOfCustomer));
                    chunkOfCustomer.clear();
                }
            }
        }

        if(!chunkOfCustomer.isEmpty()) {
            executorService.execute(() -> importValidCustomer(chunkOfCustomer));
            chunkOfCustomer.clear();
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
    public void importInvalidCustomer(Set<Customer> invalidCustomers) {

    }
}
