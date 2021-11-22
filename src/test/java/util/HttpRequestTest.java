package util;

import http.HttpRequest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.InputStream;

public class HttpRequestTest {
    private String testDirectory = "./src/test/resources/";

    @Test
    public void request_GET() throws Exception {
        InputStream in = new FileInputStream(testDirectory + "Http_GET.txt");
        HttpRequest request = new HttpRequest(in);

        assertThat(request.getMethod(), is("GET"));
        assertThat(request.getPath(), is("/user/create"));
        assertThat(request.getHeader("Connection"), is("keep-alive"));
        assertThat(request.getParameter("userId"), is("javajigi"));
    }

    @Test
    public void request_POST() throws Exception {
        InputStream in = new FileInputStream(testDirectory + "Http_POST.txt");
        HttpRequest request = new HttpRequest(in);

        assertThat(request.getMethod(), is("POST"));
        assertThat(request.getPath(), is("/user/create"));
        assertThat(request.getHeader("Connection"), is("keep-alive"));
        assertThat(request.getParameter("userId"), is("javajigi"));
    }
}
