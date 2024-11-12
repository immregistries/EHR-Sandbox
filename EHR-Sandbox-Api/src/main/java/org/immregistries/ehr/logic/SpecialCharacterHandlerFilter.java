package org.immregistries.ehr.logic;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.immregistries.ehr.api.ProcessingFlavor;

import java.io.IOException;


public class SpecialCharacterHandlerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (ProcessingFlavor.GLOTTOPHOBIA.isActive()) {
            filterChain.doFilter(new SpecialCharRequestWrapper((HttpServletRequest) servletRequest), servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}