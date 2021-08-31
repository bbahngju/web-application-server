package model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

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

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line = br.readLine();
        if(line == null) return;

        log.debug("requestLine: " + line);
        method = line.split(" ")[0];
        log.debug("method: " + method);
        if("GET".equals(method)) {
            parsePathAndParamsOfGet(line.split(" ")[1]);
        }
        else {
            path = line.split(" ")[1];
        }
        log.debug("requestPath: " + path);

        line = br.readLine();
        while(!line.equals("")) {
            String[] tokens = line.split(": ");
            headers.put(tokens[0].trim(), tokens[1].trim());
            line = br.readLine();
        }

        if("POST".equals(method)) {
            String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
            params = HttpRequestUtils.parseQueryString(body);
        }
    }

    private void parsePathAndParamsOfGet(String line) {
        String body;

        Matcher m = Pattern.compile("(.*)\\?(.*)").matcher(line);
        if(m.find()) {
            path = m.group(1);
            body = m.group(2);
        }
        else {
            path = line;
            return;
        }

        log.debug("body: " + body);
        for(String b : body.split("&")) {
            String[] tokens = b.split("=");

            params.put(tokens[0].trim(), tokens[1].trim());
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParameter(String name) {
        return params.get(name);
    }
}
