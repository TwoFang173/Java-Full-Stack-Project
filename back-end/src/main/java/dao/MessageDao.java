package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import dto.MessageDto;
import org.bson.Document;
import com.mongodb.client.model.Filters;

import java.util.function.Supplier;

public class MessageDao extends BaseDao<MessageDto> {

    private static MessageDao instance;
    private static Supplier<MessageDao> instanceSupplier = () -> {
        return new MessageDao(MongoConnection.getCollection("MessageDao"));
    };

    private MessageDao(MongoCollection<Document> collection) {
        super(collection);
    }

    public static MessageDao getInstance() {
        if (instance != null) return instance;
        instance = instanceSupplier.get();
        return instance;
    }

    // Allows injection of mocks in tests
    public static void setInstanceSupplier(Supplier<MessageDao> supplier) {
        instanceSupplier = supplier;
        instance = null;
    }

    @Override
    Supplier<MessageDto> getFromDocument(Document document) {
        MessageDto dto = new MessageDto();
        dto.fromDocument(document);
        return () -> dto;
    }

    // Deletes all messages for a conversationId. Returns the number deleted.
    public long deleteByConversationId(String conversationId) {
        DeleteResult result = collection.deleteMany(Filters.eq("conversationId", conversationId));
        return result.getDeletedCount();
    }
}