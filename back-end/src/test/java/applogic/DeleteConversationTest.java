// java
package applogic;

import dto.AuthDto;
import dto.ConversationDto;
import handler.GsonTool;
import handler.HandlerFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import request.ParsedRequest;
import response.StatusCodes;
import util.MockTestUtils;

import java.util.List;
import java.util.HashMap;

public class DeleteConversationTest {

    @Test(singleThreaded = true)
    public void deleteConversationTest() {
        var testUtils = new MockTestUtils();

        // prepare conversation to delete
        var conversation = new ConversationDto();
        conversation.setId(String.valueOf(Math.random()));
        conversation.setTitle("test-conversation");

        // mock DAO to return the conversation when queried
        Mockito.doReturn(List.of(conversation))
                .when(testUtils.mockConversationDao)
                .query("id", conversation.getId());

        // prepare and mock valid auth
        var auth = new AuthDto();
        auth.setUserName("tester");
        auth.setHash(String.valueOf(Math.random()));
        Mockito.doReturn(List.of(auth))
                .when(testUtils.mockAuthDao)
                .query("hash", auth.getHash());

        // build request for deletion
        ParsedRequest parsedRequest = new ParsedRequest();
        parsedRequest.setPath("/deleteConversation");
        parsedRequest.setBody(GsonTool.GSON.toJson(conversation));
        // attach cookie/header expected by the handler (adjust key if your ParsedRequest API differs)
        try {
            var headers = new HashMap<String, String>();
            headers.put("Cookie", auth.getHash());
            parsedRequest.setHeaders(headers);
        } catch (NoSuchMethodError | NullPointerException ignored) {
            // fallback for different ParsedRequest API
        }

        var handler = HandlerFactory.getHandler(parsedRequest);
        var builder = handler.handleRequest(parsedRequest);
        var res = builder.build();

        // expect successful deletion
        Assert.assertEquals(res.status, StatusCodes.OK);

        // verify deletion was invoked with the expected id
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(testUtils.mockConversationDao).delete(idCaptor.capture());
        Assert.assertEquals(idCaptor.getValue(), conversation.getId());
    }
}
