package com.orangetoolz.cuspro.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class InvalidCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String city;
    private String state;
    private String zipCode;
    private String phone;
    @NotEmpty(message = "Email can't be empty")
    private String email;
    private String ipAddress;

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || this.getClass() != obj.getClass()) return false;
        InvalidCustomer invalidCustomer = (InvalidCustomer) obj;
        return Objects.equals(phone, invalidCustomer.phone) && Objects.equals(email, invalidCustomer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phone,email);
    }
}
