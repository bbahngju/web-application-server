package util;

import model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;

public class IOUtils {
    /**
     * @param BufferedReader는
     *            Request Body를 시작하는 시점이어야
     * @param contentLength는
     *            Request Header의 Content-Length 값이다.
     * @return
     * @throws IOException
     */
    public static String readData(BufferedReader br, int contentLength) throws IOException {
        char[] body = new char[contentLength];
        br.read(body, 0, contentLength);
        return String.copyValueOf(body);
    }

    public static StringBuilder readUserList(Collection<User> userList) {
        int idx = 3;
        StringBuilder br = new StringBuilder();

        for(User user : userList) {
            br.append("<tr>");
            br.append("\n");
            br.append("<th scope=\"row\">").append(idx).append("</th> <td>").append(user.getUserId()).append("</td> <td>").append(user.getName()).append("</td> <td>").append(user.getEmail()).append("</td><td><a href=\"#\" class=\"btn btn-success\" role=\"button\">수정</a></td>");
            br.append("</tr>");
            br.append("\n");

            idx++;
        }

        return br;
    }
}
