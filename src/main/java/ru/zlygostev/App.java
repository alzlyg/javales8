package ru.zlygostev;

import ru.zlygostev.server.MyServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        MyServer server = new MyServer();
    }
}
