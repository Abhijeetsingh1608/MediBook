package com.medibook.gateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/*
 * This filter checks each request before it reaches the main logic.
 * It is useful for security, headers, or internal request validation.
 */
public class MutableHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders = new HashMap<>();

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public MutableHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public void putHeader(String name, String value) {
        customHeaders.put(name, value);
    }

    @Override
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public String getHeader(String name) {
        String headerValue = customHeaders.get(name);
        if (headerValue != null) {
            return headerValue;
        }
        return ((HttpServletRequest) getRequest()).getHeader(name);
    }

    @Override
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public Enumeration<String> getHeaderNames() {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> originalHeaderNames = ((HttpServletRequest) getRequest()).getHeaderNames();

        while (originalHeaderNames.hasMoreElements()) {
            String headerName = originalHeaderNames.nextElement();
            headers.put(headerName, ((HttpServletRequest) getRequest()).getHeader(headerName));
        }

        headers.putAll(customHeaders);
        return Collections.enumeration(headers.keySet());
    }

    @Override
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public Enumeration<String> getHeaders(String name) {
        String headerValue = customHeaders.get(name);
        if (headerValue != null) {
            return Collections.enumeration(Collections.singletonList(headerValue));
        }
        return ((HttpServletRequest) getRequest()).getHeaders(name);
    }
}
