package com.orangetoolz.cuspro.common;

import com.orangetoolz.cuspro.entity.Customer;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Newaz Sharif
 */

public class CustomerSet <E extends Customer> extends HashSet<E> {

    @Override
    public boolean add(E e) {
        if(!isValidProperty(e.getEmail(), ApplicationConstant.EMAIL_REGEX)
                || !isValidProperty(e.getPhone(), ApplicationConstant.US_PHONE_REGEX)) {
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

