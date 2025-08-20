package com.file.securepack.utils;

import java.io.File;
import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

@Component
public class TempFileCleanupFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        chain.doFilter(request, response);

        // 在回應完成後刪除臨時檔案
        HttpSession session = req.getSession(false);
        if (session != null) {
            String tempFile = (String) session.getAttribute("tempFile");
            if (tempFile != null) {
                new File(tempFile).delete();
                session.removeAttribute("tempFile");
            }
        }
    }
}
