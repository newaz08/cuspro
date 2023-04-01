package com.orangetoolz.cuspro.service;

import javax.servlet.http.HttpServletResponse;

public interface FileExportService {

    void exportValidCustomer(HttpServletResponse response);
    void exportInvalidCustomer(HttpServletResponse outputStream);
}
