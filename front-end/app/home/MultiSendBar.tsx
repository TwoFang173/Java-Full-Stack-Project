'use client';

import React from 'react';

interface Props {
  currentUser?: string;
}

export default function MultiSendBar({ currentUser }: Props) {
  const [targets, setTargets] = React.useState('');
  const [message, setMessage] = React.useState('');
  const [status, setStatus] = React.useState<string | null>(null);

  if (!currentUser) return null;

  async function send() {
    const toIds = targets
      .split(',')
      .map(s => s.trim())
      .filter(Boolean);

    if (!toIds.length || !message.trim()) {
      setStatus('Missing users or message');
      return;
    }

    const res = await fetch('/api/sendMultiMessage', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        toIds,
        message: message.trim(),
      }),
    });

    const data = await res.json();

    if (data.status) {
      setStatus('Message sent to multiple users');
      setMessage('');
      setTargets('');
    } else {
      setStatus(data.message || 'Failed to send');
    }
  }

  return (
    <div style={{ marginTop: 20, padding: 12, border: '1px solid #ddd', borderRadius: 6 }}>
      <h3>Multi-Send Message</h3>

      <input
        placeholder="user1, user2, user3"
        value={targets}
        onChange={e => setTargets(e.target.value)}
        style={{ width: '100%', marginBottom: 8 }}
      />

      <textarea
        placeholder="Message"
        value={message}
        onChange={e => setMessage(e.target.value)}
        style={{ width: '100%', marginBottom: 8 }}
      />

      <button onClick={send}>Send to All</button>

      {status && <div style={{ marginTop: 8 }}>{status}</div>}
    </div>
  );
}
