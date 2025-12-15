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

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {

        // Expect body like:
        // {
        //   "toIds": ["user1", "user2"],
        //   "message": "hello"
        // }

        MultiSendPayload payload =
                GsonTool.GSON.fromJson(request.getBody(), MultiSendPayload.class);

        AuthFilter.AuthResult auth = AuthFilter.doFilter(request);
        if (!auth.isLoggedIn) {
            return new ResponseBuilder().setStatus(StatusCodes.UNAUTHORIZED);
        }

        UserDao userDao = UserDao.getInstance();
        MessageDao messageDao = MessageDao.getInstance();
        ConversationDao conversationDao = ConversationDao.getInstance();

        var sender = userDao.query("userName", auth.userName)
                .stream()
                .findFirst()
                .orElse(null);

        if (sender == null) {
            return new ResponseBuilder()
                    .setStatus("200 OK")
                    .setBody(new RestApiAppResponse<>(false, null, "Invalid sender"));
        }

        List<ConversationDto> updatedConversations = new ArrayList<>();

        for (String toId : payload.toIds) {

            var receiver = userDao.query("userName", toId)
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (receiver == null) {
                continue; // skip invalid users
            }

            String conversationId =
                    ConversationDto.makeUniqueId(sender.getUserName(), toId);

            ConversationDto conversation =
                    conversationDao.query("conversationId", conversationId)
                            .stream()
                            .findFirst()
                            .orElse(new ConversationDto(
                                    sender.getUserName(),
                                    toId
                            ));

            conversation.setMessageCount(conversation.getMessageCount() + 1);
            conversationDao.put(conversation);

            MessageDto messageDto = new MessageDto();
            messageDto.setFromId(sender.getUserName());
            messageDto.setToId(toId);
            messageDto.setConversationId(conversationId);
            messageDto.setMessage(payload.message);

            messageDao.put(messageDto);

            receiver.setMessagesRecieved(receiver.getMessagesRecieved() + 1);
            sender.setMessagesSent(sender.getMessagesSent() + 1);

            userDao.put(receiver);
            updatedConversations.add(conversation);
        }

        userDao.put(sender);

        return new ResponseBuilder()
                .setStatus("200 OK")
                .setBody(new RestApiAppResponse<>(
                        true,
                        updatedConversations,
                        null
                ));
    }

    /**
     * Local payload class ONLY for parsing request body.
     * Not a Mongo DTO.
     */
    private static class MultiSendPayload {
        List<String> toIds;
        String message;
    }
}
