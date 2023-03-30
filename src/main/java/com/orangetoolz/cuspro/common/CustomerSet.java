package com.orangetoolz.cuspro.common;

import com.orangetoolz.cuspro.entity.Customer;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerSet <E extends Customer> extends HashSet<E> {

    public static final String EMAIL_REGEX = "^[^\\s@]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String US_PHONE_REGEX = "^\\(?\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{4}$";

    @Override
    public boolean add(E e) {
        if(!isValidProperty(e.getEmail(),EMAIL_REGEX)
                || !isValidProperty(e.getPhone(),US_PHONE_REGEX)) {
            return false;
        }
        return super.add(e);
    }

    private boolean isValidProperty(String email, String regex) {
        Pattern pattern =  Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}

