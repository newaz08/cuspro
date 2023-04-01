package com.orangetoolz.cuspro.service;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Newaz Sharif
 */

public interface FileExportService {

    void exportValidCustomer(HttpServletResponse response);
    void exportInvalidCustomer(HttpServletResponse outputStream);
}
