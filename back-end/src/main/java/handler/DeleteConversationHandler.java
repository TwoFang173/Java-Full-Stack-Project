package handler;

import auth.AuthFilter;
import dao.MessageDao;
import dao.ConversationDao;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

public class DeleteConversationHandler implements BaseHandler {

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {

        AuthFilter.AuthResult authResult = AuthFilter.doFilter(request);
        if (!authResult.isLoggedIn) {
            return new ResponseBuilder().setStatus(StatusCodes.UNAUTHORIZED);
        }

        String conversationId = request.getQueryParam("conversationId");
        if (conversationId == null || conversationId.isBlank()) {
            return new ResponseBuilder().setStatus(StatusCodes.BAD_REQUEST);
        }

        MessageDao messageDao = MessageDao.getInstance();
        messageDao.deleteByConversationId(conversationId); // delete messages first

        ConversationDao conversationDao = ConversationDao.getInstance();
        boolean deleted = conversationDao.deleteByConversationId(conversationId);

        if (deleted) {
            var res = new RestApiAppResponse<>(true, "Conversation deleted", null);
            return new ResponseBuilder().setStatus("200 OK").setBody(res);
        } else {
            var res = new RestApiAppResponse<>(false, null, "Conversation not found");
            return new ResponseBuilder().setStatus(StatusCodes.NOT_FOUND).setBody(res);
        }
    }
}
