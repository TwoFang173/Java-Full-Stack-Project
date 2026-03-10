# Final Project
Final project groups of 3-4

Open front-end via vscode
Open back-end via intelij

Run both together + mongodb in order to get full stack project running

## Team members
- **Marco Garcia** - Block List Feature - [YOUR_VIDEO_LINK_HERE]
- **Alejandro Cruz-Garcia** - Friends List Feature - [link to recording]
- **Nicholas Blackson** - Delete Chat Feature - [link to recording]
- **Akim Tarasov** - Send Images Chat Feature - [link to recording]


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

### Multi-Send Message (Alejandro Cruz-Garcia)

- **Backend Endpoints**:
  - `POST /sendMulti` – Send the same message to multiple users  
    - Requires authentication cookie (`auth`)
    - Request Body:
      ```json
      {
        "toIds": ["userA", "userB", "userC"],
        "message": "Hello everyone"
      }
      ```
    - Behavior:
      - Creates one message per recipient
      - Updates sender’s `messagesSent` count by number of recipients
      - Updates each recipient’s `messagesRecieved` count by 1
      - Creates or updates a conversation per sender–recipient pair

- **Frontend**:
  - Component: `MultiSendBar`
  - Integrated into: `/home` page
  - Features:
    - Enter multiple recipient usernames
    - Send one message to all selected users
    - Automatically updates conversations list

- **Database**:
  - Collection: `messages`
    ```json
    {
      "fromId": "String",
      "toId": "String",
      "message": "String",
      "timestamp": "Long",
      "conversationId": "String"
    }
    ```
  - Collection: `conversations`
    ```json
    {
      "fromId": "String",
      "toId": "String",
      "conversationId": "String",
      "messageCount": "Number"
    }
    ```

- **Testing**:
  - Test Class: `SendMultiMessageTest`
  - Validates:
    - Authentication is required
    - One message is created per recipient
    - Sender message count increments correctly
    - Each recipient’s received count increments correctly

### Delete Chat (Mr Blackson)
Allows users to delete an entire conversation along with all its messages.

- **Backend Endpoints**: 
  - `POST /deleteConversation` - Delete a conversation (JSON: `{"conversationId": "id_of_conversation"}`)
  
- **Frontend**: 
  - Accessible via the dashboard conversation list by clicking the "Delete" button next to each conversation.
  - Prompts the user to confirm deletion before removing the conversation from the list.
  - Updates the UI immediately after deletion.
  
- **Database**: 
  - Collections: `conversations` and `messages` in MongoDB `Homework2` database.
  - Behavior: Deletes the conversation document and all associated message documents by `conversationId`.

- **Notes**:
  - Requires authentication; only logged-in users can delete conversations.
  - Returns `200 OK` on success, `404 Not Found` if the conversation does not exist, and `401 Unauthorized` if the user is not logged in.

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
- Issues with DeleteConversationHandler.java 



