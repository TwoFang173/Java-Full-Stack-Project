package handler;

import auth.AuthFilter;
import dao.ConversationDao;
import dao.UserDao;
import dto.UserDto;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

import java.util.ArrayList;

public class GetConversationsHandler implements BaseHandler {

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {

        AuthFilter.AuthResult authResult = AuthFilter.doFilter(request);
        if (!authResult.isLoggedIn) {
            return new ResponseBuilder().setStatus(StatusCodes.UNAUTHORIZED);
        }

        UserDto userDto = UserDao.getInstance().query("userName", authResult.userName)
                .stream()
                .findFirst()
                .orElse(null);

        if (userDto == null) {
            return new ResponseBuilder().setStatus(StatusCodes.NOT_FOUND);
        }

        ConversationDao conversationDao = ConversationDao.getInstance();
        var convos = new ArrayList<>(conversationDao.query("toId", userDto.getUserName()));
        var convos2 = conversationDao.query("fromId", userDto.getUserName());

        convos.addAll(convos2);

        var res = new RestApiAppResponse<>(true, convos, null);
        return new ResponseBuilder().setStatus("200 OK").setBody(res);
    }
}
