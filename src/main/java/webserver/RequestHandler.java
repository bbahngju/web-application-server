package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;


public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            DataOutputStream dos = new DataOutputStream(out);

            String requestUrl = br.readLine().split(" ")[1];

            byte[] body;
            Map<String, String> headerInfo = HttpRequestUtils.parseHeader(br);
            int contentLength = Integer.parseInt(headerInfo.getOrDefault("Content-Length", "0").trim());
            String type = headerInfo.getOrDefault("Accept", null);
            String responseType = "text/html";
            Map<String, String> cookies = util.HttpRequestUtils.parseCookies(headerInfo.getOrDefault("Cookie", null));

            if(type.contains("text/css")) {
                responseType = "text/css";
            }
            if("/".equals(requestUrl)) {
                body = "Welcome to My Page :>".getBytes();
                response200Header(dos, responseType, body.length);
                responseBody(dos, body);
            }
            else if("/user/create".equals(requestUrl)) {
                String params = IOUtils.readData(br, contentLength);
                User user = createUser(params);
                DataBase.addUser(user);
                response302Header(dos, "/index.html");
            }
            else if("/user/login".equals(requestUrl)) {
                String params = IOUtils.readData(br, contentLength);
                Map<String, String> login = loginParams(params);
                String checkId = login.getOrDefault("userId", null);
                String checkPassword = login.getOrDefault("password", null);

                if(isAccessible(checkId, checkPassword)) {
                    response302LoginSuccessHeader(dos, "/index.html");
                }
                else {
                    responseResource(dos, responseType, "/user/login_failed.html");
                }
            }
            else if("/user/list".equals(requestUrl)) {
                boolean isCookie = Boolean.parseBoolean(cookies.getOrDefault("logined", "false"));
                if(!isCookie) {
                    responseResource(dos, responseType, "/user/login.html");
                    return;
                }
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border='1'>");
                for (User user : users) {
                    sb.append("<tr>");
                    sb.append("<td>" + user.getUserId() + "</td>");
                    sb.append("<td>" + user.getName() + "</td>");
                    sb.append("<td>" + user.getEmail() + "</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");

                body = sb.toString().getBytes();
                response200Header(dos, responseType, body.length);
                responseBody(dos, body);
            }
            else {
                responseResource(dos, responseType, requestUrl);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private User createUser(String params) {
        Map<String, String> param = HttpRequestUtils.parseQueryString(params);
        return new User(param.get("userId"), param.get("password"), param.get("name"), param.get("email"));
    }

    private Map<String, String> loginParams(String params) {
        return HttpRequestUtils.parseQueryString(params);
    }

    private boolean isAccessible(String id, String password) {
        if(id == null || password == null) return false;
        User user = DataBase.findUserById(id);
        if(user == null) return false;

        return user.getPassword().equals(password);
    }

    private void responseResource(DataOutputStream dos, String responseType, String url) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, responseType, body.length);
        responseBody(dos, body);
    }

    private void response200Header(DataOutputStream dos, String type, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + type + ";charset=utf-8 \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302LoginSuccessHeader(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: " + url + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
