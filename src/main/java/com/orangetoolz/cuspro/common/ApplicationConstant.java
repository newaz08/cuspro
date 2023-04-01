package com.orangetoolz.cuspro.common;

public class ApplicationConstant {

    private ApplicationConstant() {

    }
    public static final String EMAIL_REGEX = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    public static final String US_PHONE_REGEX = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";
    public static final int IMPORT_DATA_CHUNK_SIZE = 10000;
    public static final int EXPORT_DATA_CHUNK_SIZE = 100000;
}
