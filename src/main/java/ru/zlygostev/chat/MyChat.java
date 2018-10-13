package ru.zlygostev.chat;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;
import static com.sun.javafx.fxml.expression.Expression.add;


public class MyChat extends JFrame {
    private JTextField chatField;
    private JTextArea chatHistory;
    private final String SERVER_ADDR = "localhost";
    private final String cmdAuthokString = "/authok";
    private final String cmdEndSessionString = "/end";
    private final String msgAuthokString = "Авторизация пройдена успешно";
    private final String msgErrSendString = "Ошибка отправки сообщения";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private Boolean authorized;
    private DataInputStream in;
    private DataOutputStream out;

    public void setAuthorized(Boolean authorizedd) {
        this.authorized = authorized;
    }

    public MyChat() {
        setBounds(600, 300, 500, 500);
        setTitle("Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        chatHistory = new JTextArea();
        chatHistory.setEditable(false);
        chatHistory.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(chatHistory);
        add(jsp, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);
        JButton jbSend = new JButton("SEND");
        bottomPanel.add(jbSend, BorderLayout.EAST);
        chatField = new JTextField();
        bottomPanel.add(chatField, BorderLayout.CENTER);
        jbSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!chatField.getText().trim().isEmpty()) {
                    sendMsg();
                    chatField.grabFocus();
                }
            }
        });
        chatField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMsg();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    out.writeUTF("/end");
                    socket.close();
                    out.close();
                    in.close();
                } catch (IOException exc) {
                }
            }
        });
        setVisible(true);
        start();
    }

    public void start() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            setAuthorized(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith(cmdAuthokString)) {
                            setAuthorized(true);
                            chatHistory.append("\n" + msgAuthokString + "\n");
                            break;
                        }
                        chatHistory.append(str + "\n");
                    }
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals(cmdEndSessionString)) {
                            break;
                        }
                        chatHistory.append(str + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setAuthorized(false);
                }
            }
        }).start();
    }

    public void sendMsg() {
        try {
            out.writeUTF(chatField.getText());
            chatField.setText("");
        } catch (IOException e) {
            System.out.println(msgErrSendString);
        }
    }
}
