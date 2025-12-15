package applogic;

import dto.AuthDto;
import dto.MessageDto;
import dto.UserDto;
import handler.GsonTool;
import handler.HandlerFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import request.ParsedRequest;
import response.StatusCodes;
import util.MockTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendMultiMessageTest {

    @Test(singleThreaded = true)
    public void sendMultiMessageTest() {

        var testUtils = new MockTestUtils();

        // ===== Sender =====
        var sender = new UserDto();
        sender.setUserName("sender_" + Math.random());

        ArrayList<UserDto> senderReturn = new ArrayList<>();
        senderReturn.add(sender);

        // ===== Recipients =====
        var userA = new UserDto();
        userA.setUserName("userA_" + Math.random());

        var userB = new UserDto();
        userB.setUserName("userB_" + Math.random());

        ArrayList<UserDto> userAReturn = new ArrayList<>();
        userAReturn.add(userA);

        ArrayList<UserDto> userBReturn = new ArrayList<>();
        userBReturn.add(userB);

        // ===== Parsed Request =====
        ParsedRequest parsedRequest = new ParsedRequest();
        parsedRequest.setPath("/sendMulti");

        var auth = testUtils.createLogin(sender.getUserName());
        parsedRequest.setCookieValue("auth", auth.getHash());

        // ===== Payload =====
        Map<String, Object> payload = new HashMap<>();
        payload.put("toIds", List.of(userA.getUserName(), userB.getUserName()));
        payload.put("message", "hello everyone");

        parsedRequest.setBody(GsonTool.GSON.toJson(payload));

        // ===== Handler =====
        var handler = HandlerFactory.getHandler(parsedRequest);

        // ===== Mock DAO behavior =====
        Mockito.when(testUtils.mockUserDao.query("userName", sender.getUserName()))
                .thenReturn(senderReturn);

        Mockito.when(testUtils.mockUserDao.query("userName", userA.getUserName()))
                .thenReturn(userAReturn);

        Mockito.when(testUtils.mockUserDao.query("userName", userB.getUserName()))
                .thenReturn(userBReturn);

        // ===== Captors =====
        ArgumentCaptor<MessageDto> messageCaptor =
                ArgumentCaptor.forClass(MessageDto.class);

        ArgumentCaptor<UserDto> userCaptor =
                ArgumentCaptor.forClass(UserDto.class);

        // ===== Execute =====
        var builder = handler.handleRequest(parsedRequest);
        var res = builder.build();

        // ===== Verify auth =====
        Mockito.verify(testUtils.mockAuthDao)
                .query("hash", auth.getHash());

        // ===== Status =====
        Assert.assertEquals(res.status, StatusCodes.OK);
        Assert.assertTrue(builder.getBody().status);

        // ===== Messages =====
        Mockito.verify(testUtils.mockMessageDao, Mockito.times(2))
                .put(messageCaptor.capture());

        Assert.assertEquals(messageCaptor.getAllValues().size(), 2);
        Assert.assertEquals(
                messageCaptor.getAllValues().getFirst().getMessage(),
                "hello everyone"
        );

        // ===== Users updated =====
        Mockito.verify(testUtils.mockUserDao, Mockito.atLeast(3))
                .put(userCaptor.capture());

        // One sender + two receivers
        var updatedUsers = userCaptor.getAllValues();

        UserDto updatedSender =
                updatedUsers.stream()
                        .filter(u -> u.getUserName().equals(sender.getUserName()))
                        .findFirst()
                        .orElseThrow();

        Assert.assertEquals(updatedSender.getMessagesSent(), 2);

        UserDto updatedA =
                updatedUsers.stream()
                        .filter(u -> u.getUserName().equals(userA.getUserName()))
                        .findFirst()
                        .orElseThrow();

        UserDto updatedB =
                updatedUsers.stream()
                        .filter(u -> u.getUserName().equals(userB.getUserName()))
                        .findFirst()
                        .orElseThrow();

        Assert.assertEquals(updatedA.getMessagesRecieved(), 1);
        Assert.assertEquals(updatedB.getMessagesRecieved(), 1);
    }
}
