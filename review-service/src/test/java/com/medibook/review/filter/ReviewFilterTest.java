package com.medibook.review.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

class ReviewFilterTest {

    @Test
    void filter_skips_without_header() throws Exception {
        InternalRequestFilter filter = new InternalRequestFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("X-Internal-Secret")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/reviews");
        when(response.getWriter()).thenReturn(mock(java.io.PrintWriter.class));

        filter.doFilter(request, response, chain);
        org.mockito.Mockito.verify(chain, org.mockito.Mockito.never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
