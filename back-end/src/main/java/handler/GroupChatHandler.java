package handler;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class GroupChatHandler {

    private static final int DEFAULT_HISTORY_LIMIT = 100;

    private final ConcurrentMap<String, Group> groups = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Consumer<Message>> userSenders = new ConcurrentHashMap<>();

    /* ================= USER MANAGEMENT ================= */

    public void registerUser(String userId, Consumer<Message> sender) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(sender);
        userSenders.put(userId, sender);
    }

    public void unregisterUser(String userId) {
        if (userId == null) return;
        userSenders.remove(userId);
        groups.values().forEach(g -> g.removeMember(userId));
    }

    /* ================= GROUP MANAGEMENT ================= */

    public boolean createGroup(String groupId) {
        return groups.putIfAbsent(groupId, new Group(groupId, DEFAULT_HISTORY_LIMIT)) == null;
    }

    public boolean joinGroup(String userId, String groupId) {
        if (!userSenders.containsKey(userId)) return false;
        Group group = groups.computeIfAbsent(groupId,
                id -> new Group(id, DEFAULT_HISTORY_LIMIT));
        return group.addMember(userId);
    }

    public boolean leaveGroup(String userId, String groupId) {
        Group group = groups.get(groupId);
        return group != null && group.removeMember(userId);
    }

    /* ================= MESSAGING ================= */

    public boolean sendMessage(String senderId, String groupId, String text) {
        Group group = groups.get(groupId);
        if (group == null) return false;

        if (!group.isMember(senderId)) {
            return false; // prevent spoofing
        }

        Message msg = new Message(
                senderId,
                groupId,
                text,
                Instant.now().toEpochMilli()
        );

        group.appendHistory(msg);
        broadcast(group, msg);
        return true;
    }

    private void broadcast(Group group, Message msg) {
        for (String member : group.snapshotMembers()) {
            Consumer<Message> sender = userSenders.get(member);
            if (sender == null) continue;

            try {
                sender.accept(msg);
            } catch (Exception e) {
                // auto-cleanup broken connections
                unregisterUser(member);
            }
        }
    }

    /* ================= QUERIES ================= */

    public List<Message> getHistory(String groupId) {
        Group group = groups.get(groupId);
        return group == null ? List.of() : group.snapshotHistory();
    }

    public Set<String> getGroupMembers(String groupId) {
        Group group = groups.get(groupId);
        return group == null ? Set.of() : group.snapshotMembers();
    }

    /* ================= INNER CLASSES ================= */

    private static final class Group {
        private final String id;
        private final Set<String> members = ConcurrentHashMap.newKeySet();
        private final ConcurrentLinkedDeque<Message> history = new ConcurrentLinkedDeque<>();
        private final int limit;

        Group(String id, int limit) {
            this.id = id;
            this.limit = Math.max(1, limit);
        }

        boolean addMember(String userId) {
            return members.add(userId);
        }

        boolean removeMember(String userId) {
            return members.remove(userId);
        }

        boolean isMember(String userId) {
            return members.contains(userId);
        }

        void appendHistory(Message m) {
            history.addLast(m);
            while (history.size() > limit) {
                history.pollFirst();
            }
        }

        Set<String> snapshotMembers() {
            return Set.copyOf(members);
        }

        List<Message> snapshotHistory() {
            return List.copyOf(history);
        }
    }

    public static final class Message {
        private final String senderId;
        private final String groupId;
        private final String text;
        private final long timestamp;

        public Message(String senderId, String groupId, String text, long timestamp) {
            this.senderId = senderId;
            this.groupId = groupId;
            this.text = text;
            this.timestamp = timestamp;
        }

        public String getSenderId() { return senderId; }
        public String getGroupId() { return groupId; }
        public String getText() { return text; }
        public long getTimestamp() { return timestamp; }
    }
}
