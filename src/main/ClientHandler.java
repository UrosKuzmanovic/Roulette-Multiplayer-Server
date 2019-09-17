package main;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread {

    Socket socket;

    private BufferedReader clientInput;
    private PrintStream clientOutput;

    private String username = "user";
    private double balance = 1000;
    private List<FieldInfo> betList = new ArrayList<>();

    public ClientHandler(Socket s) {
        socket = s;
    }

    public void run() {

        do {
            int random = Server.generateRandomIntIntRange(100, 999);
            username = "user" + random;
        } while (isUser(username));

        try {
            this.clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientOutput = new PrintStream(socket.getOutputStream());

            System.out.println("Welcome " + username);
            Server.onlineUsers.add(new OnlineUsers(username, balance));
            for (int i = 0; i < Server.onlineUsers.size(); i++) {
                System.out.println(Server.onlineUsers.get(i).getUsername());
            }

            boolean test = true;
            while (true) {
                int minutes = Server.minutes;
                int seconds = Server.seconds;
                if (minutes != 0 || seconds != 0)
                    test = false;
                JSONObject time = createJSON(0, minutes + ":" + seconds);
                String jsonTime = createJSONString(time);
                clientOutput.println(jsonTime);
                if (minutes == 0 && seconds == 0 && !test) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int number = Server.number;
                    JSONObject num = createJSON(1, number + "");
                    String jsonNum = createJSONString(num);
                    clientOutput.println(jsonNum);
                    double total = 0;
                    for (int i = 0; i < betList.size(); i++) {
                        if (number == betList.get(i).getNumber()) {
                            double win = betList.get(i).getBet() * 36.0;
                            balance += win;
                            total += win;
                            updateBalance(Server.onlineUsers, username, (Math.round(balance) * 100.0) / 100.0);
                        }
                    }
                    JSONObject profit = createJSON(2, total + "");
                    String jsonProfit = createJSONString(profit);
                    clientOutput.println(jsonProfit);
                    sendOnlineUsers();
                    betList.clear();
                    test = true;
                }

                String jsonString = null;
                try {
                    jsonString = clientInput.readLine();
                    System.out.println(jsonString);
                } catch (IOException e) {
                }
                if (jsonString != null) {
                    JSONObject json = toJSON(jsonString);
                    int type = json.getInt("type");

                    if (type == 4) { // primanje uloga
                        String field = json.getString("value");
                        String[] list = field.split(":");
                        int number = Integer.parseInt(list[0]);
                        double bet = Double.parseDouble(list[1]);
                        balance -= bet;
                        updateBalance(Server.onlineUsers, username, (Math.round(balance) * 100.0) / 100.0);
                        System.out.println("Number: " + number);
                        System.out.println("Bet: " + bet);
                        FieldInfo fi = new FieldInfo(number, bet);
                        betList.add(fi);
                    } else if (type == 5) { // promena imena
                        String newName = json.getString("value");
                        if (!(newName.equals("*****"))) {
                            replaceUsername(username, newName);
                            System.out.println(username + " is now " + newName);
                            username = newName;
                        }
                    } else if (type == 6) { // igrac napusta server
                        System.out.println(username + " je napustio server.");
                        removeUser(Server.onlineUsers, username);
                        clientOutput.close();
                        clientInput.close();
                        break;
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    private void sendOnlineUsers() {
        String users = "";
        for (int i = 0; i < Server.onlineUsers.size(); i++) {
            String username = Server.onlineUsers.get(i).getUsername();
            double balance = Server.onlineUsers.get(i).getBalance();
            users = users + username + "\t\t" + balance + " RSD" + "\n";
        }
        JSONObject userList = createJSON(3, users);
        String jsonUsers = createJSONString(userList);
        clientOutput.println(jsonUsers);
    }

    public void updateBalance(List<OnlineUsers> list, String username, double balance) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUsername().equals(username)) {
                list.get(i).setBalance(balance);
            }
        }
    }

    public void removeUser(List<OnlineUsers> list, String username) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUsername().equals(username)) {
                list.remove(i);
            }
        }
    }

    private JSONObject createJSON(int type, String value) {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("value", value);
        return json;
    }

    private String createJSONString(JSONObject json) {
        return json.toString();
    }

    private JSONObject toJSON(String string) {
        return new JSONObject(string);
    }

    private boolean isUser(String username) {
        for (int i = 0; i < Server.onlineUsers.size(); i++) {
            if (Server.onlineUsers.get(i).getUsername().equals(username))
                return true;
        }
        return false;
    }

    private void replaceUsername(String oldName, String newName) {
        for (int i = 0; i < Server.onlineUsers.size(); i++) {
            if (Server.onlineUsers.get(i).getUsername().equals(oldName))
                Server.onlineUsers.get(i).setUsername(newName);
        }
    }
}
