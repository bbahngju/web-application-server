package model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;
import util.RequestLine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private String method;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private RequestLine requestLine;

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();

            if(line == null) {
                return;
            }

            requestLine = new RequestLine(line);

            line = br.readLine();
            while(!line.equals("")) {
                log.debug("headers: " + line);
                String[] tokens = line.split(": ");
                headers.put(tokens[0].trim(), tokens[1].trim());
                line = br.readLine();
            }

            if("POST".equals(getMethod())) {
                String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            } else {
                params = requestLine.getParams();
            }
        } catch (IOException io) {
            log.error(io.getMessage());
        }

    }

    public Boolean isCookie() {
        if(headers.containsKey("Cookie")) {
            log.debug("Cookie: true");
            return true;
        }
        log.debug("Cookie: false");
        return false;
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getMethod() {
        return requestLine.getMethod();
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParameter(String name) {
        return params.get(name);
    }
}
