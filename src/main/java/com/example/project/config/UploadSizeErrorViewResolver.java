package com.example.project.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.List;
import java.util.Map;

/**
 * {@code spring.servlet.multipart.max-file-size} violations never reach a normal
 * {@code @ExceptionHandler}/{@code @ControllerAdvice}: Spring Security's {@code CsrfFilter} calls
 * {@code request.getParameter(...)} to resolve the CSRF token, which forces Tomcat to eagerly parse
 * the multipart body — and does so before Spring MVC's {@code DispatcherServlet} has resolved a
 * handler, as a plain filter-chain exception rather than a servlet-dispatch one. It surfaces only
 * through the container's {@code /error} dispatch, which this {@link ErrorViewResolver} hooks into
 * (the one extension point {@link org.springframework.boot.webmvc.autoconfigure.error.BasicErrorController}
 * consults for HTML error responses). Only Product Create/Edit's photo upload submits multipart
 * requests today, so this redirects back to whichever of those two pages failed, with a friendly
 * Vietnamese message, instead of the default whitelabel/stack-trace error page.
 */
@Component
public class UploadSizeErrorViewResolver implements ErrorViewResolver {

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        String uri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (uri == null || !isUploadSizeExceeded(exception)) {
            return null;
        }
        RequestContextUtils.getOutputFlashMap(request)
                .put("errorMessages", List.of("Ảnh vượt quá dung lượng cho phép (tối đa 5MB)"));
        return new ModelAndView("redirect:" + uri);
    }

    private boolean isUploadSizeExceeded(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof MaxUploadSizeExceededException) {
                return true;
            }
            String message = cause.getMessage();
            if (message != null && message.contains("SizeLimitExceededException")) {
                return true;
            }
        }
        return false;
    }
}
