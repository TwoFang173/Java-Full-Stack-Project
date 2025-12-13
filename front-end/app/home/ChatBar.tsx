'use client';

import React from 'react';

interface Props {
  currentUser?: string;
  targetUser?: string;
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

// ✅ THIS IS THE IMPORTANT PART
const JAVA_BASE = 'http://localhost:1299';

export default function ChatBar({ currentUser, targetUser }: Props) {
  const [messages, setMessages] = React.useState<MessageDto[]>([]);
  const [text, setText] = React.useState('');
  const messagesRef = React.useRef<HTMLDivElement | null>(null);

  if (!currentUser || !targetUser) return null;

  const ids = [currentUser, targetUser].slice().sort();
  const conversationId = `${ids[0]}_${ids[1]}`;

  React.useEffect(() => {
    if (messagesRef.current) {
      messagesRef.current.scrollTop = messagesRef.current.scrollHeight;
    }
  }, [messages]);

  /* ===================== DIRECT MESSAGES (UNCHANGED) ===================== */

  async function loadConversation() {
    const res = await fetch(
      `/api/getConversation?conversationId=${encodeURIComponent(conversationId)}`
    );
    const data = await res.json();
    setMessages(data.data);
  }

  React.useEffect(() => {
    loadConversation();
  }, [currentUser]);

  async function send() {
    if (!text.trim()) return;

    const payload = {
      fromId: currentUser,
      toId: targetUser,
      message: text.trim(),
      conversationId,
    };

    setMessages((prev) => [
      ...prev,
      {
        from: currentUser,
        to: targetUser,
        text: text.trim(),
        time: new Date().toLocaleTimeString(),
      },
    ]);

    setText('');

    await fetch('/api/sendMessage', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
  }

  /* ===================== GROUP CHAT (FIXED) ===================== */

  async function sendGroup() {
    if (!text.trim()) return;

    const payload = {
      fromId: currentUser,
      groupId: GROUP_ID,
      message: text.trim(),
    };

    setMessages((prev) => [
      ...prev,
      {
        from: currentUser,
        text: text.trim(),
        time: new Date().toLocaleTimeString(),
      },
    ]);

    setText('');

    const res = await fetch(`${JAVA_BASE}/sendGroupMessage`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      console.error('Group send failed:', await res.text());
    }
  }

  async function loadGroupMessages() {
    const res = await fetch(
      `${JAVA_BASE}/getGroupMessages?groupId=${GROUP_ID}`,
      { credentials: 'include' }
    );

    if (!res.ok) {
      console.error('Failed to load group messages:', await res.text());
      return;
    }

    const data = await res.json();

    setMessages(
      data.data.map((m: any) => ({
        from: m.fromId,
        text: m.message,
        time: new Date(m.timestamp).toLocaleTimeString(),
      }))
    );
  }

  return (
    <div style={{ marginTop: 24 }}>
      <h2>Group Chat ({GROUP_ID})</h2>

      <div
        ref={messagesRef}
        style={{
          height: 160,
          overflowY: 'auto',
          border: '1px solid #ccc',
          padding: 8,
        }}
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
