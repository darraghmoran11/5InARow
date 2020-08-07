# 5 In A Row Challenge - Client/Server
# Summary
A server/client implementation of the game 5-in-a-row (variation of Connect 4)

The project uses a client-server architecture implemented using Java. I used some Server/Client Theory Adaptions from Deitel and Deitel "Java How to Program" book. A new application-level protocol called C5P (Connect 5 protocol) was created, which is entirely plain text. The messages are displayed below:

Client -> Server MOVE <n> QUIT
Server -> Client WELCOME <char> VALID_MOVE OTHER_PLAYER_MOVED <n>
          OTHER_PLAYER_LEFT VICTORY DEFEAT TIE MESSAGE <text>

# Instructions to play the game:
1. Download the code. Open a terminal and direct into the folder which contains the 5-in-a-row game.
2. Run the server side connection to host the game. The command in the terminal is: java Server.java
3. To play with 2 people, another 2 terminals need to be opened. One for each client to connect.
4. For Player one to connect to the Client in the first termianl use this command: java Client.java. The player can then enter their name in the terminal.
5. For Player two to connect to the Client in the first termianl use this command: java Client.java. The player can then enter their name in the terminal.
6. Once both players have entered their names they will appear above the GUI which the game opens to show it has commenced. There are instructions displayed on this to show the users what to do.
7. Once the game is finished and there is a winner or there is a draw. The users will be given the option for a rematch. The users can enter there names in the terminal again and begin to play.
