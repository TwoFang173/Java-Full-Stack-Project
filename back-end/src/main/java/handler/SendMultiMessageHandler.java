package handler;

import auth.AuthFilter;
import dao.ConversationDao;
import dao.MessageDao;
import dao.UserDao;
import dto.ConversationDto;
import dto.MessageDto;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

import java.util.ArrayList;
import java.util.List;

public class SendMultiMessageHandler implements BaseHandler {

    private static class MultiSendBody {
        public List<String> toIds;
        public String message;
    }

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {
        AuthFilter.AuthResult auth = AuthFilter.doFilter(request);
        if (!auth.isLoggedIn) {
            return new ResponseBuilder().setStatus(StatusCodes.UNAUTHORIZED);
        }

        MultiSendBody body = GsonTool.GSON.fromJson(request.getBody(), MultiSendBody.class);

        if (body == null || body.toIds == null || body.toIds.isEmpty() || body.message == null) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(false, null, "Invalid payload"));
        }

        UserDao userDao = UserDao.getInstance();
        MessageDao messageDao = MessageDao.getInstance();
        ConversationDao conversationDao = ConversationDao.getInstance();

        var fromUser = userDao.query("userName", auth.userName)
                .stream().findFirst().orElse(null);

        if (fromUser == null) {
            return new ResponseBuilder()
                    .setStatus("200 OK")
                    .setBody(new RestApiAppResponse<>(false, null, "Invalid sender"));
        }

        List<ConversationDto> createdConversations = new ArrayList<>();

        for (String toId : body.toIds) {
            var toUser = userDao.query("userName", toId)
                    .stream().findFirst().orElse(null);

            if (toUser == null) continue;

            String conversationId =
                    ConversationDto.makeUniqueId(fromUser.getUserName(), toId);

            ConversationDto convo = conversationDao
                    .query("conversationId", conversationId)
                    .stream()
                    .findFirst()
                    .orElse(new ConversationDto(fromUser.getUserName(), toId));

            convo.setMessageCount(convo.getMessageCount() + 1);
            conversationDao.put(convo);

            MessageDto msg = new MessageDto();
            msg.setFromId(fromUser.getUserName());
            msg.setToId(toId);
            msg.setMessage(body.message);
            msg.setConversationId(conversationId);

            messageDao.put(msg);

            toUser.setMessagesRecieved(toUser.getMessagesRecieved() + 1);
            fromUser.setMessagesSent(fromUser.getMessagesSent() + 1);

            userDao.put(toUser);
            userDao.put(fromUser);

            createdConversations.add(convo);
        }

        return new ResponseBuilder()
                .setStatus("200 OK")
                .setBody(new RestApiAppResponse<>(true, createdConversations, null));
    }
}
