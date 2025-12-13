package applogic;

import handler.GroupChatHandler;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class GroupChatTest {

    /* ================= TEST HELPERS ================= */

    static final class MessageCollector implements Consumer<GroupChatHandler.Message> {
        private final BlockingQueue<GroupChatHandler.Message> received =
                new LinkedBlockingQueue<>();

        @Override
        public void accept(GroupChatHandler.Message message) {
            received.add(message);
        }

        GroupChatHandler.Message poll(long timeoutMs) throws InterruptedException {
            return received.poll(timeoutMs, TimeUnit.MILLISECONDS);
        }

        int size() {
            return received.size();
        }
    }

    /* ================= ASSERT HELPERS ================= */

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FAIL: " + msg);
        System.out.println("PASS: " + msg);
    }

    private static void assertFalse(boolean cond, String msg) {
        assertTrue(!cond, msg);
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if ((expected == null && actual != null) ||
                (expected != null && !expected.equals(actual))) {
            throw new AssertionError(
                    "FAIL: " + msg + " | expected=" + expected + " actual=" + actual);
        }
        System.out.println("PASS: " + msg);
    }

    private static void assertNotNull(Object obj, String msg) {
        if (obj == null) throw new AssertionError("FAIL: " + msg);
        System.out.println("PASS: " + msg);
    }

    /* ================= MAIN ================= */

    public static void main(String[] args) throws Exception {
        GroupChatHandler handler = new GroupChatHandler();

        testCreateJoinSend(handler);
        testHistoryLimit(handler);
        testNoSpoofing(handler);
        testConcurrency(handler);

        System.out.println("\n✅ ALL MANUAL TESTS PASSED");
    }

    /* ================= TESTS ================= */

    private static void testCreateJoinSend(GroupChatHandler handler)
            throws InterruptedException {

        System.out.println("\n--- testCreateJoinSend ---");

        assertTrue(handler.createGroup("g1"), "create group");

        MessageCollector alice = new MessageCollector();
        MessageCollector bob = new MessageCollector();

        handler.registerUser("alice", alice);
        handler.registerUser("bob", bob);

        assertTrue(handler.joinGroup("alice", "g1"), "alice joined");
        assertTrue(handler.joinGroup("bob", "g1"), "bob joined");

        assertTrue(handler.sendMessage("alice", "g1", "hello"),
                "alice sends message");

        assertNotNull(alice.poll(500), "alice received message");
        assertNotNull(bob.poll(500), "bob received message");

        List<GroupChatHandler.Message> history = handler.getHistory("g1");
        assertEquals(1, history.size(), "history size == 1");
        assertEquals("hello", history.get(0).getText(), "history content");
    }

    private static void testHistoryLimit(GroupChatHandler handler) {

        System.out.println("\n--- testHistoryLimit ---");

        handler.createGroup("g_hist");
        handler.registerUser("sender", msg -> {});
        handler.joinGroup("sender", "g_hist");

        for (int i = 0; i < 150; i++) {
            handler.sendMessage("sender", "g_hist", "msg" + i);
        }

        List<GroupChatHandler.Message> history =
                handler.getHistory("g_hist");

        assertEquals(100, history.size(), "history capped at 100");
        assertEquals("msg50", history.get(0).getText(), "first kept msg");
        assertEquals("msg149", history.get(99).getText(), "last kept msg");
    }

    private static void testNoSpoofing(GroupChatHandler handler) {

        System.out.println("\n--- testNoSpoofing ---");

        handler.createGroup("g_sec");
        handler.registerUser("attacker", msg -> {});

        assertFalse(
                handler.sendMessage("attacker", "g_sec", "evil"),
                "non-member cannot send"
        );

        assertEquals(0,
                handler.getHistory("g_sec").size(),
                "history remains empty");
    }

    private static void testConcurrency(GroupChatHandler handler)
            throws InterruptedException {

        System.out.println("\n--- testConcurrency ---");

        handler.createGroup("g_conc");

        int threads = 8;
        int messagesPerThread = 50;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            String user = "u" + i;
            handler.registerUser(user, msg -> {});
            handler.joinGroup(user, "g_conc");
        }

        for (int i = 0; i < threads; i++) {
            final int id = i;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int m = 0; m < messagesPerThread; m++) {
                        handler.sendMessage(
                                "u" + id,
                                "g_conc",
                                "t" + id + "-m" + m
                        );
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS),
                "concurrent tasks finished");

        pool.shutdownNow();

        List<GroupChatHandler.Message> history =
                handler.getHistory("g_conc");

        assertEquals(100, history.size(), "history capped under concurrency");
    }
}
