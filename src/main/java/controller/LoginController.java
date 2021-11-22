package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

public class LoginController implements Controller{
    @Override
    public void service(HttpRequest request, HttpResponse response) {
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

    private boolean isAccessible(String id, String password) {
        if(id == null || "".equals(id) || password == null || "".equals(password)) return false;
        User user = DataBase.findUserById(id);
        if(user == null) return false;

        return user.getPassword().equals(password);
    }
}
