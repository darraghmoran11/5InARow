package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * The Client for a two-player network game of 5-in-a-row. The project uses a
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
public class Client {

    private JFrame frame = new JFrame("Connect 5");
    private JLabel messageLabel = new JLabel("...");

    private Square[] board = new Square[54]; //6 rows x 9 columns
    private Square currentSquare;

    private Socket socket;
    private Scanner in;
    private PrintWriter out;
    private String name;

    /**
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name){
        this.name = name;
    }

    public Client(String serverAddress) throws Exception {

        socket = new Socket(serverAddress, 5890);
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);

        messageLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(messageLabel, BorderLayout.SOUTH);

        var boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);
        boardPanel.setLayout(new GridLayout(6, 9, 2, 2));
        for (var i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentSquare = board[j];
                    out.println("MOVE " + j);
                }
            });
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
    }

    /**
     * This method listens for messages from the server and displays the moves to each
     * player during the game.
     */
    public void play() throws Exception {
        try {
            var response = in.nextLine();
            var mark = response.charAt(8);
            var opponentMark = mark == 'X' ? 'O' : 'X';
            frame.setTitle("Connect 5: Player " + name);
            while (in.hasNextLine()) {
                response = in.nextLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Valid move, please wait");
                    currentSquare.setText(mark);
                    currentSquare.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    var loc = Integer.parseInt(response.substring(15));
                    board[loc].setText(opponentMark);
                    board[loc].repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                } else if (response.startsWith("VICTORY")) {
                    JOptionPane.showMessageDialog(frame, "You are the Winner!!!!!");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    JOptionPane.showMessageDialog(frame, "Sorry you lost");
                    break;
                } else if (response.startsWith("DRAW")) {
                    JOptionPane.showMessageDialog(frame, "The game is a DRAW");
                    break;
                } else if (response.startsWith("OTHER_PLAYER_LEFT")) {
                    JOptionPane.showMessageDialog(frame, "The other player has left.");
                    break;
                }
            }
            out.println("QUIT");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
            frame.dispose();
        }
    }

    /**
     * Gives the Players the opportunity for a rematch.
     */
    private boolean playAgain() {

        int response = JOptionPane.showConfirmDialog(frame,
                "Want to play again?",
                "GAME OVER" ,
                JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;

    }

    static class Square extends JPanel {
        JLabel label = new JLabel();

        public Square() {
            setBackground(Color.white);
            setLayout(new GridBagLayout());
            label.setFont(new Font("Arial", Font.BOLD, 40));
            add(label);
        }

        public void setText(char text) {
            label.setForeground(text == 'X' ? Color.BLUE : Color.RED);
            label.setText(text + "");
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Client has started up.");
        while (true) {
            String serverAddress = (args.length == 0) ? "localhost" : args[1];
            Scanner scan = new Scanner(System.in);
            Client client = new Client(serverAddress);
            System.out.println("Please enter your name: ");
            client.setName(scan.nextLine());
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setSize(480, 320);
            client.frame.setVisible(true);
            client.frame.setResizable(false);
            client.play();

            if (!client.playAgain()) {
                break;
            }
        }
    }
}