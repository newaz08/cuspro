package com.orangetoolz.cuspro.service;

import com.orangetoolz.cuspro.entity.Customer;

import java.util.Set;

public interface CustomerFileImportService {

    void importValidCustomer(Set<Customer> validCustomers);
    void importInvalidCustomer(Set<Customer> invalidCustomers);
}
