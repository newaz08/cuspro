package com.orangetoolz.cuspro.repository;

import com.orangetoolz.cuspro.entity.InvalidCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvalidCustomerRepository extends JpaRepository<InvalidCustomer, Long> {

    @Query("SELECT ic FROM InvalidCustomer ic")
    List<InvalidCustomer> findAllInvalidCustomer();
}
