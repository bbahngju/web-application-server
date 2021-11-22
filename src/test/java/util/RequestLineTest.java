package util;

import http.RequestLine;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class RequestLineTest {
    @Test
    public void create_method() {
        RequestLine line = new RequestLine("GET /index.html HTTP/1.1");
        assertEquals("GET", line.getMethod());
        assertEquals("/index.html", line.getPath());

        line = new RequestLine("POST /login.html HTTP/1.1");
        assertEquals("POST", line.getMethod());
        assertEquals("/login.html", line.getPath());
    }

    @Test
    public void create_path_and_params() {
        RequestLine line = new RequestLine("GET /user/create?userId=javajigi&password=pass HTTP/1.1");
        assertEquals("/user/create", line.getPath());

        Map<String, String> params = line.getParams();
        assertEquals(2, params.size());
    }
}
