package handler;

import auth.AuthFilter;
import dao.ConversationDao;
import dao.MessageDao;
import dto.BaseDto;
import dto.ConversationDto;
import handler.GsonTool;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

import java.util.List;

public class DeleteConversationHandler implements BaseHandler {

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {

        AuthFilter.AuthResult authResult = AuthFilter.doFilter(request);
        if (!authResult.isLoggedIn) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.UNAUTHORIZED);
        }

        ConversationDto dto;
        try {
            dto = GsonTool.GSON.fromJson(request.getBody(), ConversationDto.class);
        } catch (Exception e) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST);
        }

        String conversationId = dto.getConversationId();
        if (conversationId == null || conversationId.isBlank()) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST);
        }

        // Delete messages
        MessageDao.getInstance().deleteByConversationId(conversationId);

        // Delete conversation
        boolean deleted = ConversationDao.getInstance()
                .deleteByConversationId(conversationId);

        if (deleted) {
            RestApiAppResponse<BaseDto> res =
                    new RestApiAppResponse<>(true, List.of(), "Conversation deleted");
            return new ResponseBuilder()
                    .setStatus(StatusCodes.OK)
                    .setBody(res);
        } else {
            RestApiAppResponse<BaseDto> res =
                    new RestApiAppResponse<>(false, List.of(), "Conversation not found");
            return new ResponseBuilder()
                    .setStatus(StatusCodes.NOT_FOUND)
                    .setBody(res);
        }
    }
}
