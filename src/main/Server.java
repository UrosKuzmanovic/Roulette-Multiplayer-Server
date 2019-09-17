package main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Server extends Thread {

    static int minutes;
    static int seconds;
    static int number;
    static List<OnlineUsers> onlineUsers;

    public static void main(String[] args) throws IOException {
        ServerSocket ss;
        Socket socket;
        onlineUsers = new ArrayList<>();
        Thread thread = new Thread(new Server());
        thread.start();
        ss = new ServerSocket(8000);

        while (true) {
            System.out.println("ServerSocket awaiting connections...");

            socket = ss.accept();
            socket.setSoTimeout(1);

            System.out.println("Connection from " + socket + "!");

            ClientHandler client = new ClientHandler(socket);

            client.start();
        }
    }

    @Override
    public void run() {
        while (true) {
            int time = 30;
            do {
                minutes = time / 60;
                seconds = time % 60;
                System.out.println(minutes + " min, " + seconds + " sec");
                if (minutes == 0 && seconds == 0) {
                    number = generateRandomIntIntRange(0, 36);
                    System.out.println("Izvuceni broj je " + number);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                time = time - 1;
            }
            while (time >= 0);

            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static int generateRandomIntIntRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}