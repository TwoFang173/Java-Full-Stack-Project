package handler;

import auth.AuthFilter;
import dto.MessageDto;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

import java.util.List;
import java.util.stream.Collectors;

public class GetGroupMessagesHandler implements BaseHandler {

    private final GroupChatHandler groupChat =
            GroupChatRegistry.getInstance();

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {

        // ---------- AUTH ----------
        AuthFilter.AuthResult auth = AuthFilter.doFilter(request);
        if (!auth.isLoggedIn) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.UNAUTHORIZED);
        }

        // ---------- QUERY ----------
        String groupId = request.getQueryParam("groupId");
        if (groupId == null) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST);
        }

        // ---------- INTERNAL → DTO ----------
        List<MessageDto> messages =
                groupChat.getHistory(groupId)
                        .stream()
                        .map(msg -> {
                            MessageDto dto = new MessageDto();
                            dto.setFromId(msg.getSenderId());
                            dto.groupId = msg.getGroupId();
                            dto.setMessage(msg.getText());
                            dto.setTimestamp(msg.getTimestamp());
                            return dto;
                        })
                        .collect(Collectors.toList());

        return new ResponseBuilder()
                .setStatus(StatusCodes.OK)
                .setBody(
                        new RestApiAppResponse<MessageDto>(
                                true,
                                messages,
                                null
                        )
                );
    }
}
