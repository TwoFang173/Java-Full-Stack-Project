'use client';

import React from 'react';

interface Props {
  currentUser?: string;
  targetUser?: string;
  conversationId?: string; // <- added
}

interface MessageDto {
  fromId?: string;
  toId?: string;
  message?: string;
  conversationId?: string;
  groupId?: string;

  // UI helpers
  from?: string;
  to?: string;
  text?: string;
  time?: string;
}

const GROUP_ID = 'demo-group';
const JAVA_BASE = 'http://localhost:1299';

export default function ChatBar({ currentUser, targetUser, conversationId: propConversationId }: Props) {
  const [messages, setMessages] = React.useState<MessageDto[]>([]);
  const [text, setText] = React.useState('');
  const messagesRef = React.useRef<HTMLDivElement | null>(null);

  if (!currentUser || !targetUser) return null;

  // Compute conversationId from users if not passed
  const ids = [currentUser, targetUser].slice().sort();
  const conversationId = propConversationId ?? `${ids[0]}_${ids[1]}`;

  React.useEffect(() => {
    if (messagesRef.current) {
      messagesRef.current.scrollTop = messagesRef.current.scrollHeight;
    }
  }, [messages]);

  // Load direct messages
  async function loadConversation() {
    try {
      const res = await fetch(`/api/getConversation?conversationId=${encodeURIComponent(conversationId)}`);
      const data = await res.json();
      if (data && data.status) {
        // Map messages to UI-friendly format
        setMessages(
            data.data.map((m: any) => ({
              from: m.fromId,
              to: m.toId,
              text: m.message,
              time: new Date(m.timestamp || Date.now()).toLocaleTimeString(),
            }))
        );
      }
    } catch (err) {
      console.error('Failed to load conversation:', err);
    }
  }

  React.useEffect(() => {
    loadConversation();
  }, [currentUser, targetUser, conversationId]);

  async function send() {
    if (!text.trim()) return;

    const payload = {
      fromId: currentUser,
      toId: targetUser,
      message: text.trim(),
      conversationId,
    };

    // Optimistic UI
    setMessages((prev) => [
      ...prev,
      { from: currentUser, to: targetUser, text: text.trim(), time: new Date().toLocaleTimeString() },
    ]);
    setText('');

    try {
      await fetch('/api/sendMessage', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
    } catch (err) {
      console.error('Failed to send message:', err);
    }
  }

  // Group chat functions unchanged
  async function sendGroup() {
    if (!text.trim()) return;

    const payload = { fromId: currentUser, groupId: GROUP_ID, message: text.trim() };

    setMessages((prev) => [
      ...prev,
      { from: currentUser, text: text.trim(), time: new Date().toLocaleTimeString() },
    ]);
    setText('');

    try {
      const res = await fetch(`${JAVA_BASE}/sendGroupMessage`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload),
      });
      if (!res.ok) console.error('Group send failed:', await res.text());
    } catch (err) {
      console.error(err);
    }
  }

  async function loadGroupMessages() {
    try {
      const res = await fetch(`${JAVA_BASE}/getGroupMessages?groupId=${GROUP_ID}`, { credentials: 'include' });
      if (!res.ok) {
        console.error('Failed to load group messages:', await res.text());
        return;
      }
      const data = await res.json();
      setMessages(
          data.data.map((m: any) => ({
            from: m.fromId,
            text: m.message,
            time: new Date(m.timestamp || Date.now()).toLocaleTimeString(),
          }))
      );
    } catch (err) {
      console.error(err);
    }
  }

  return (
      <div style={{ marginTop: 24 }}>
        <h2>Chat with {targetUser}</h2>

        <div
            ref={messagesRef}
            style={{ height: 160, overflowY: 'auto', border: '1px solid #ccc', padding: 8 }}
        >
          {messages.map((m, i) => (
              <div key={i}>
                <small>{m.time} • {m.from}</small>
                <div>{m.text}</div>
              </div>
          ))}
        </div>

        <textarea
            value={text}
            onChange={(e) => setText(e.target.value)}
            style={{ width: '100%', marginTop: 8 }}
        />

        <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
          <button onClick={send}>Send Direct</button>
          <button onClick={sendGroup}>Send Group</button>
          <button onClick={loadGroupMessages}>Load Group</button>
        </div>
      </div>
  );
}
