package com.orangetoolz.cuspro.common;

import com.orangetoolz.cuspro.entity.Customer;
import org.springframework.stereotype.Service;

/**
 * @author Newaz Sharif
 */

@Service
public class CustomerFileDataPrepare {

    public Customer parseFile(String line) {

        String [] fields = line.split(",");

        if(fields.length == 8) {
            Customer customer = new Customer();
            customer.setFirstName(fields[0]);
            customer.setLastName(fields[1]);
            customer.setCity(fields[2]);
            customer.setState(fields[3]);
            customer.setZipCode(fields[4]);
            customer.setPhone(fields[5]);
            customer.setEmail(fields[6]);
            customer.setIpAddress(fields[7]);

            return customer;
        }

        return null;
    }
}
