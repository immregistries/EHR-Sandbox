package org.immregistries.ehr.logic;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;


public class SpecialCharacterHandlerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        filterChain.doFilter(new SpecialCharRequestWrapper((HttpServletRequest) servletRequest), servletResponse);
    }
}