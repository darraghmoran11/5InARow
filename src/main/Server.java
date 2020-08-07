package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

/**
 * The Server for a two-player network game of 5-in-a-row. The project uses a
 * client-server architecture implemented using Java. I used some Server/Client
 * Theory Adaptions from Deitel and Deitel "Java How to Program" book. A new
 * application-level protocol called C5P (Connect 5 protocol) was created, which
 * is entirely plain text. The messages are displayed below:
 *
 * Client -> Server MOVE <n> QUIT
 * Server -> Client WELCOME <char> VALID_MOVE OTHER_PLAYER_MOVED <n>
 *           OTHER_PLAYER_LEFT VICTORY DEFEAT TIE MESSAGE <text>
 *
 * @author Darragh Moran
 */
public class Server {

    public static void main(String[] args) throws Exception {
        try (var listener = new ServerSocket(5890)) {
            System.out.println("Game Server is Running...");
            System.out.println("Listening on IP Address: "+ listener.getInetAddress());
            System.out.println("Listening on Port: "+ listener.getLocalSocketAddress());
            System.out.println("Waiting on Connections... ");
            var pool = Executors.newFixedThreadPool(200);
            while (true) {
                Game game = new Game();
                pool.execute(game.new Player(listener.accept(), 'X'));
                pool.execute(game.new Player(listener.accept(), 'O'));
            }
        }
    }
}

class Game {

    // Board cells numbered 0-8, top to bottom, left to right; null if empty
    //private Player[] board = new Player[9];
    private Player[] board = {
            null, null, null, null, null, null,null, null, null,
            null, null, null, null, null, null,null, null, null,
            null, null, null, null, null, null,null, null, null,
            null, null, null, null, null, null,null, null, null,
            null, null, null, null, null, null,null, null, null,
            null, null, null, null, null, null,null, null, null};

    Player currentPlayer;

    public boolean hasWinner() {
        // horizontalCheck
        for (int j = 0 ; j< 10-5 ; j++){//column
            for (int i = 0 ; i < 54 ; i+=9){//row
                if (    board[i + j]!= null &&
                        board[i +j]== board[i +j+1] &&
                        board[i +j] == board[i+j+2] &&
                        board[i +j] ==  board[i+j+3] &&
                        board[i +j] ==  board[i+j+4]){
                    System.out.println("Horizontal win.");
                    return true;
                }
            }
        }
        // verticalCheck
        for (int i = 0 ; i< 27 ; i+=9){
            for (int j = 0 ; j < 10 ; j++){
                if (    board[i  + j]!= null &&
                        board[i +j]== board[i+9 +j] &&
                        board[i +j] == board[i+(18) +j] &&
                        board[i +j] ==  board[i+(27) +j] &&
                        board[i +j] ==  board[i+(36) +j]){
                    System.out.println("Vertical win.");
                    return true;
                }
            }
        }
        // ascendingDiagonalCheck
        for (int i = 27 ; i< 54 ; i+=9){
            for (int j = 0 ; j <5 ; j++){
                if (    board[i + j]!= null &&
                        board[i +j]== board[(i-9) +j+1] &&
                        board[(i-9) +j+1] == board[i-18 +j+2] &&
                        board[(i-18) +j+2] ==  board[(i-27) +j+3]  &&
                        board[(i-27) +j+3] ==  board[(i-36) +j+4]){
                    System.out.println("Accending Diagonal.");
                    return true;
                }
            }
        }
        // descendingDiagonalCheck
        for (int i = 27 ; i< 54 ; i+=9){
            for (int j = 4 ; j < 9; j++){
                if (    board[i  + j]!= null &&
                        board[i +j]== board[(i-9) +j-1 ] &&
                        board[(i-9) +j-1 ] == board[(i-18) +j-2] &&
                        board[(i-18) +j-2] ==  board[(i-27) +j-3]  &&
                        board[(i-27) +j-3] ==  board[(i-36) +j-4]){
                    System.out.println("Descending Diagonal.");
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if the board squares are full
     */
    public boolean boardFilledUp() {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == null) {
                return false;
            }
        }
        System.out.println("All squares on the board are full.");
        return true;
    }

    /**
     * This is to check to see if the move is legal
     */
    public synchronized void move(int location, Player player) {
        if (player != currentPlayer) {
            throw new IllegalStateException("Not your turn");
        } else if (player.opponent == null) {
            throw new IllegalStateException("You don't have an opponent yet");
        } else if (board[location] != null) {
            throw new IllegalStateException("Cell already occupied");
        }
        board[location] = currentPlayer;
        currentPlayer = currentPlayer.opponent;
    }

    /**
     * Players are identified by the marks 'X' and 'O'. The player communicates with
     * the client using a socket with a Scanner and PrintWriter
     */
    class Player implements Runnable {
        char mark;
        Player opponent;
        Socket socket;
        Scanner input;
        PrintWriter output;

        public Player(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
        }

        @Override
        public void run() {
            try {
                setup();
                processCommands();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (opponent != null && opponent.output != null) {
                    opponent.output.println("OTHER_PLAYER_LEFT");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        private void setup() throws IOException {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("WELCOME " + mark);
            if (mark == 'X') {
                currentPlayer = this;
                output.println("MESSAGE Waiting for opponent to connect");
            } else {
                opponent = currentPlayer;
                opponent.opponent = this;
                opponent.output.println("MESSAGE Your move");
            }
        }

        private void processCommands() {
            while (input.hasNextLine()) {
                var command = input.nextLine();
                if (command.startsWith("QUIT")) {
                    return;
                } else if (command.startsWith("MOVE")) {
                    processMoveCommand(Integer.parseInt(command.substring(5)));
                }
            }
        }

        private void processMoveCommand(int location) {
            try {
                move(location, this);
                output.println("VALID_MOVE");
                opponent.output.println("OPPONENT_MOVED " + location);
                if (hasWinner()) {
                    output.println("VICTORY");
                    opponent.output.println("DEFEAT");
                } else if (boardFilledUp()) {
                    output.println("TIE");
                    opponent.output.println("TIE");
                }
            } catch (IllegalStateException e) {
                output.println("MESSAGE " + e.getMessage());
            }
        }
    }
}
