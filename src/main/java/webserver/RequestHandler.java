package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.HttpRequest;
import model.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            String requestPath = request.getPath();
            log.debug("requestPath: " + requestPath);
            boolean isLogin = false;

            if("/".equals(requestPath)) {
                response.forwardBody("Welcome to My Page :>");
            }
            else if("/user/create".equals(requestPath)) {
                User user = createUser(request);
                DataBase.addUser(user);
                response.sendRedirect("/index.html");
            }
            else if("/user/login".equals(requestPath)) {
                String checkId = request.getParameter("userId");
                String checkPassword = request.getParameter("password");

                if(isAccessible(checkId, checkPassword)) {
                    response.addHeader("Set-Cookie", "logined=true");
                    response.sendRedirect("/index.html");
                }
                else {
                    response.addHeader("Set-Cookie", "logined=false");
                    response.forward("/user/login_failed.html");
                }
            }
            else if("/user/list".equals(requestPath)) {
                if(request.isCookie()) {
                    Map<String, String> cookies = util.HttpRequestUtils.parseCookies(request.getHeader("Cookie"));
                    String login = cookies.getOrDefault("logined", null);
                    if("true".equals(login)) {
                        isLogin = true;
                    }
                }

                if(!isLogin) {
                    response.sendRedirect("/user/login.html");
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

                response.forwardBody(sb.toString());
            }
            else {
                response.forward(requestPath);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private User createUser(HttpRequest request) {
        return new User(request.getParameter("userId"), request.getParameter("password"),
                request.getParameter("name"), request.getParameter("email"));
    }

    private boolean isAccessible(String id, String password) {
        if(id == null || "".equals(id) || password == null || "".equals(password)) return false;
        User user = DataBase.findUserById(id);
        if(user == null) return false;

        return user.getPassword().equals(password);
    }
}
