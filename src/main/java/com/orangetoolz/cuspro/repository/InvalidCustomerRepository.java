package com.orangetoolz.cuspro.repository;

import com.orangetoolz.cuspro.entity.InvalidCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidCustomerRepository extends JpaRepository<InvalidCustomer, Long> {
}
