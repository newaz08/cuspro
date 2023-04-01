package com.orangetoolz.cuspro.repository;

import com.orangetoolz.cuspro.entity.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {

    @Query("SELECT c FROM Customer c")
    CopyOnWriteArrayList<Customer> findBetween(Pageable pageable);
}
