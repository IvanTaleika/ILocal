package ILocal.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class ResponseService {

    public void sendNotFound(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND,"Not Found");
    }

    public void sendBadRequest(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Bad Request");
    }

    public void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"You are not authorize");
    }

    public void sendForbidden(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN,"Forbidden error");
    }


}
