package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import dto.MessageDto;
import org.bson.Document;
import com.mongodb.client.model.Filters;

import java.util.function.Supplier;

// TODO fill this out
public class MessageDao extends BaseDao<MessageDto> {

    private static MessageDao instance;
    private static Supplier<MessageDao> instanceSupplier = () -> {
        return new MessageDao(MongoConnection.getCollection("MessageDao"));
    };

    private MessageDao(MongoCollection<Document> collection) {
        super(collection);
    }

    public static MessageDao getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = instanceSupplier.get();
        return instance;
    }

    public static void setInstanceSupplier(Supplier<MessageDao> instanceSupplier){
        MessageDao.instanceSupplier = instanceSupplier;
    }

    @Override
    Supplier<MessageDto> getFromDocument(Document document) {
        var auth = new MessageDto();
        auth.fromDocument(document);
        return () -> auth;
    }

    // Nicholas Blackson
    // Deletes all messages for the given conversationId and returns the deleted count.
    public long deleteByConversationId(String conversationId) {
        DeleteResult result = collection.deleteMany(Filters.eq("conversationId", conversationId));
        return result.getDeletedCount();
    }
}
