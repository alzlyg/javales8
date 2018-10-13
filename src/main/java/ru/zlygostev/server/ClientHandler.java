package ru.zlygostev.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServerIn, Socket socketIn) {
        final MyServer myServer = myServerIn;
        final Socket socket = socketIn;
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        long startTime = System.currentTimeMillis();
                        while (true) { // цикл авторизации
                            int delay = (int) (System.currentTimeMillis() - startTime);
                            if (delay>120000) {
                                break;
                            }
                            String str = in.readUTF();
                            if (str.startsWith("/auth")) {
                                String[] parts = str.split("\\s");
                                String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                                if (nick != null) {
                                    if (!myServer.isNickBusy(nick)) {
                                        ClientHandler.this.sendMsg("/authok " + nick);
                                        name = nick;
                                        myServer.broadcastMsg(name + " зашел в чат");
                                        myServer.subscribe(ClientHandler.this);
                                        break;
                                    } else ClientHandler.this.sendMsg("Учетная запись уже используется");
                                } else {
                                    ClientHandler.this.sendMsg("Неверные логин/пароль");
                                }
                            }
                        }
                        if(!name.equals("")) {
                            while (true) { // цикл получения сообщений
                                String str = in.readUTF();
                                System.out.println("от " + name + ": " + str);
                                if (str.equals("/end")) break;
                                if (str.startsWith("/w")) {
                                    String[] parts = str.split("\\s");
                                    String msg = str.substring(4 + parts[1].length());
                                    myServer.sendMsgByNick(ClientHandler.this, parts[1], name + ": " + msg);
                                    ClientHandler.this.sendMsg(name + ": " + str);
                                    continue;
                                }
                                myServer.broadcastMsg(name + ": " + str);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        myServer.unsubscribe(ClientHandler.this);
                        if(name.equals("")) {
                            myServer.broadcastMsg("Была попытка подключения. В 120 с не уложились");
                            ClientHandler.this.sendMsg("/end");
                        }
                        else {
                            myServer.broadcastMsg(name + " вышел из чата");
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
