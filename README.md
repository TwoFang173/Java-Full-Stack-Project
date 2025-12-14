# Final Project
Final project groups of 3-4

Open front-end via vscode
Open back-end via intelij

Run both together + mongodb in order to get full stack project running

## Team members
- **Marco Garcia** - Block List Feature - [YOUR_VIDEO_LINK_HERE]
- **airwalker3000** - Friends List Feature - [link to recording]
- **Mr Blackson** - Delete Chat Feature - [link to recording]

## Features

### Block List (Marco Garcia)
Allows users to block and unblock other users, maintaining a personal block list.

- **Backend Endpoints**: 
  - `POST /block` - Block a user (JSON: `{"blocker": "username", "blocked": "username"}`)
  - `DELETE /block` - Unblock a user (JSON: `{"blocker": "username", "blocked": "username"}`)
  - `GET /blocks?user=<username>` - Get list of blocked users
  
- **Frontend**: 
  - Page: `/blocks` (accessible at `http://localhost:3000/blocks`)
  - Features: Block users, view blocked list, unblock users
  
- **Database**: 
  - Collection: `blocks` in MongoDB `Homework2` database
  - Documents: `{ blocker: String, blocked: String, timestamp: Long }`

### Friends List (airwalker3000)
[Teammate to fill in]

### Delete Chat (Mr Blackson)
[Teammate to fill in]

## How to Run

1. **Start MongoDB:**
   ```bash
   docker run -d --name mongo -p 27017:27017 mongo:latest
   ```

2. **Start Backend (IntelliJ):**
   - Open `back-end` folder in IntelliJ
   - Run `Server.java`
   - Server will start on `http://localhost:1299`

3. **Start Frontend (VSCode or terminal):**
   ```bash
   cd front-end
   npm install
   npm run dev
   ```
   - Frontend will start on `http://localhost:3000`

4. **Access the Application:**
   - Main app: `http://localhost:3000`
   - Block List feature: `http://localhost:3000/blocks`

## Known Issues
- Minor checkstyle warnings in `GroupChatHandler.java` (teammate's code) - wildcard imports


