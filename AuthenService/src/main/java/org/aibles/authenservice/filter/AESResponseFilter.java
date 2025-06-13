package org.aibles.authenservice.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

public class AESResponseFilter implements Filter {

    private final String aesKey;

    public AESResponseFilter(String aesKey) {
        this.aesKey = aesKey;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        CustomHttpServletResponseWrapper responseWrapper = new CustomHttpServletResponseWrapper(httpResponse);

        chain.doFilter(request, responseWrapper);

        if (httpResponse.getStatus() == HttpServletResponse.SC_OK) {
            try {

                String responseBody = responseWrapper.getCaptureAsString();
                String encryptedResponseBody = AESUtils.encrypt(responseBody, aesKey);
                httpResponse.setContentType("application/json");
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.getOutputStream().write(encryptedResponseBody.getBytes());
                httpResponse.getOutputStream().flush();
            } catch (Exception e) {
                httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during response encryption");
            }
        }
    }
}
