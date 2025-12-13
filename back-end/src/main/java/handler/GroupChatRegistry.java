package handler;

public final class GroupChatRegistry {

    private static final GroupChatHandler INSTANCE = new GroupChatHandler();

    private GroupChatRegistry() {}

    public static GroupChatHandler getInstance() {
        return INSTANCE;
    }
}
