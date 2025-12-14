'use client';

import { useState } from 'react';

export default function BlockList() {
  const [blocker, setBlocker] = useState('');
  const [blocked, setBlocked] = useState('');
  const [blocks, setBlocks] = useState<string[]>([]);
  const [message, setMessage] = useState('');

  const handleBlock = async () => {
    try {
      const response = await fetch('http://localhost:1299/block', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ blocker, blocked }),
      });
      const data = await response.json();
      setMessage(data.message || 'User blocked successfully');
      refreshBlocks();
    } catch (error) {
      setMessage('Error blocking user');
    }
  };

  const handleUnblock = async (blockedUser: string) => {
    try {
      const response = await fetch('http://localhost:1299/block', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ blocker, blocked: blockedUser }),
      });
      const data = await response.json();
      setMessage(data.message || 'User unblocked successfully');
      refreshBlocks();
    } catch (error) {
      setMessage('Error unblocking user');
    }
  };

  const refreshBlocks = async () => {
    if (!blocker) return;
    try {
      const response = await fetch(`http://localhost:1299/blocks?user=${blocker}`);
      const data = await response.json();
      const blocksList = JSON.parse(data.message);
      setBlocks(blocksList);
      setMessage('Blocks list refreshed');
    } catch (error) {
      setMessage('Error fetching blocks');
      setBlocks([]);
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
      <h1>Block List Management</h1>
      
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          placeholder="Your username (blocker)"
          value={blocker}
          onChange={(e) => setBlocker(e.target.value)}
          style={{ padding: '8px', marginRight: '10px', width: '200px' }}
        />
        <input
          type="text"
          placeholder="User to block"
          value={blocked}
          onChange={(e) => setBlocked(e.target.value)}
          style={{ padding: '8px', marginRight: '10px', width: '200px' }}
        />
        <button onClick={handleBlock} style={{ padding: '8px 16px' }}>
          Block User
        </button>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <button onClick={refreshBlocks} style={{ padding: '8px 16px' }}>
          Refresh Blocks List
        </button>
      </div>

      {message && (
        <div style={{ padding: '10px', backgroundColor: '#f0f0f0', marginBottom: '20px' }}>
          {message}
        </div>
      )}

      <h2>Blocked Users for {blocker || '...'}</h2>
      {blocks.length === 0 ? (
        <p>No blocked users</p>
      ) : (
        <ul>
          {blocks.map((user) => (
            <li key={user} style={{ marginBottom: '10px' }}>
              {user}{' '}
              <button onClick={() => handleUnblock(user)} style={{ marginLeft: '10px' }}>
                Unblock
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}