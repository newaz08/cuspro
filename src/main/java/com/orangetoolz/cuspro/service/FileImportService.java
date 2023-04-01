package com.orangetoolz.cuspro.service;

import com.orangetoolz.cuspro.entity.Customer;
import com.orangetoolz.cuspro.entity.InvalidCustomer;

import java.util.List;
import java.util.Set;

public interface FileImportService {

    void importValidCustomer(Set<Customer> validCustomers);
    void importInvalidCustomer(List<InvalidCustomer> invalidCustomers);
}
