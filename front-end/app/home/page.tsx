'use client';

import React from "react";
import ChatBar from "./ChatBar";
import ChatList from "./ChatList";
import MultiSendBar from './MultiSendBar';

interface UserDto {
    userName: string;
    totalConversations: number;
    messagesSent: number;
    messagesRecieved: number;
}

type Conversation = {
    id: string;
    title?: string;
    // add other fields if your API returns them
};

export default function Home() {
    const [user, setUser] = React.useState<UserDto | null>(null);
    const [loading, setLoading] = React.useState(true);
    const [activeChatUser, setActiveChatUser] = React.useState<string | null>(null);

    const [conversations, setConversations] = React.useState<Conversation[]>([]);
    const [loadingConvos, setLoadingConvos] = React.useState(false);
    const [deletingId, setDeletingId] = React.useState<string | null>(null);
    const [convoError, setConvoError] = React.useState<string | null>(null);

    React.useEffect(() => {
        fetch("/api/getUser", { credentials: 'include' })
            .then((res) => res.json())
            .then((apiRes) => {
                setUser(apiRes?.data?.[0] ?? null);
            })
            .catch((err) => {
                console.error(err);
            })
            .finally(() => setLoading(false));
    }, []);

    React.useEffect(() => {
        setLoadingConvos(true);
        fetch("/api/conversations", { credentials: 'include' })
            .then((res) => res.json())
            .then((apiRes) => {
                if (apiRes && apiRes.status) setConversations(apiRes.data || []);
                else setConvoError(apiRes?.message || 'Failed to load conversations');
            })
            .catch(() => setConvoError('Failed to load conversations'))
            .finally(() => setLoadingConvos(false));
    }, []);

    const formatNumber = (n?: number) => (typeof n === 'number' ? n.toLocaleString() : '—');

    function handleDeleteConversation(conversationId: string) {
        if (!confirm('Delete this conversation? This cannot be undone.')) return;
        setDeletingId(conversationId);
        fetch(`/api/deleteConversation?conversationId=${encodeURIComponent(conversationId)}`, {
            method: 'DELETE',
            credentials: 'include',
        })
            .then(res => res.json())
            .then(apiRes => {
                if (apiRes && apiRes.status) {
                    setConversations(prev => prev.filter(c => c.id !== conversationId));
                } else {
                    setConvoError(apiRes?.message || 'Failed to delete conversation');
                }
            })
            .catch(() => setConvoError('Failed to delete conversation'))
            .finally(() => setDeletingId(null));
    }

    // Early return when loading finished but no user data
    if (!loading && !user) {
        return (
            <div style={{ padding: 20 }}>
                <h1 style={{ margin: 0 }}>Welcome!</h1>
                <p style={{ marginTop: 8 }}>No user data available.</p>
            </div>
        );
    }

    return (
        <div style={{ padding: 20 }}>
            <h1 style={{ margin: 0 }}>Welcome{user ? `, ${user.userName}` : ''}!</h1>
            <p style={{ marginTop: 8 }}>Here's your dashboard overview.</p>

            {loading ? (
                <p>Loading dashboard…</p>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16, marginTop: 16 }}>
                    <div style={{ padding: 16, border: '1px solid #e6e6e6', borderRadius: 8 }}>
                        <h3 style={{ margin: 0 }}>Total Conversations</h3>
                        <div style={{ fontSize: 20, fontWeight: 600, marginTop: 8 }}>{formatNumber(user?.totalConversations)}</div>
                    </div>

                    <div style={{ padding: 16, border: '1px solid #e6e6e6', borderRadius: 8 }}>
                        <h3 style={{ margin: 0 }}>Messages Sent</h3>
                        <div style={{ fontSize: 20, fontWeight: 600, marginTop: 8 }}>{formatNumber(user?.messagesSent)}</div>
                    </div>

                    <div style={{ padding: 16, border: '1px solid #e6e6e6', borderRadius: 8 }}>
                        <h3 style={{ margin: 0 }}>Messages Received</h3>
                        <div style={{ fontSize: 20, fontWeight: 600, marginTop: 8 }}>{formatNumber(user?.messagesRecieved)}</div>
                    </div>
                </div>
            )}
<MultiSendBar currentUser={user?.userName} />

            {/* Conversations section */}
            <div style={{ marginTop: 20 }}>
                <h2 style={{ marginBottom: 8 }}>Conversations</h2>
                {convoError && <div style={{ color: 'red', marginBottom: 8 }}>{convoError}</div>}
                {loadingConvos && <div>Loading conversations…</div>}
                {!loadingConvos && conversations.length === 0 && <div>No conversations</div>}
                <ul style={{ listStyle: 'none', padding: 0, marginTop: 12 }}>
                    {conversations.map(c => (
                        <li key={c.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 12px', border: '1px solid #eee', borderRadius: 6, marginBottom: 8 }}>
                            <div>
                                <div style={{ fontWeight: 600 }}>{c.title || '(no title)'}</div>
                                <div style={{ fontSize: 12, color: '#666' }}>{c.id}</div>
                            </div>
                            <div>
                                <button onClick={() => setActiveChatUser(c.title ?? c.id)} style={{ marginRight: 8 }}>Open</button>
                                <button
                                    onClick={() => handleDeleteConversation(c.id)}
                                    disabled={deletingId !== null}
                                    style={{ background: '#e53e3e', color: 'white', border: 'none', padding: '6px 10px', borderRadius: 4, cursor: 'pointer' }}
                                >
                                    {deletingId === c.id ? 'Deleting…' : 'Delete'}
                                </button>
                            </div>
                        </li>
                    ))}
                </ul>
            </div>

            {!activeChatUser && (
                <div style={{ marginTop: 20 }}>
                    <ChatList currentUser={user?.userName} onOpenChat={(username) => setActiveChatUser(username)} />
                </div>
            )}

            {activeChatUser ? (
                <div style={{ marginTop: 20 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div style={{ fontWeight: 600 }}>Chat with {activeChatUser}</div>
                        <button onClick={() => setActiveChatUser(null)} style={{ padding: '6px 10px' }} aria-label="Back to chat list">
                            Back to chats
                        </button>
                    </div>
                    <ChatBar currentUser={user?.userName} targetUser={activeChatUser} />
                </div>
            ) : null}
        </div>
    );
}