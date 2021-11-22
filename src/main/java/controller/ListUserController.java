package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

import java.util.Collection;
import java.util.Map;

public class ListUserController implements Controller{
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        boolean isLogin = false;

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
            sb.append("<td>").append(user.getUserId()).append("</td>");
            sb.append("<td>").append(user.getName()).append("</td>");
            sb.append("<td>").append(user.getEmail()).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        response.forwardBody(sb.toString());
    }
}
