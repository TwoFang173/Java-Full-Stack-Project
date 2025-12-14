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

        // --- 1. Auth check ---
        AuthFilter.AuthResult authResult = AuthFilter.doFilter(request);
        if (!authResult.isLoggedIn) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.UNAUTHORIZED);
        }

        // --- 2. Parse conversationId from body ---
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

        // --- 3. Delete messages ---
        MessageDao.getInstance().deleteByConversationId(conversationId);

        // --- 4. Delete conversation ---
        boolean deleted = ConversationDao.getInstance()
                .deleteByConversationId(conversationId);

        // --- 5. Build response ---
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
