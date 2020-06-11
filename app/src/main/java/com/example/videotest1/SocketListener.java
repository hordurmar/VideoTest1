package com.example.videotest1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class SocketListener {
    private boolean running;
    private int port;
    private Server server;
    private Thread socketListenerThread;

    SocketListener(int port, Server server) {
        running = false;
        this.port = port;
        this.server = server;
        socketListenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                acceptConnections();
            }
        });
    }

    synchronized void start() {
        running = true;
        socketListenerThread.start();
    }

    void stop() {
        running = false;
        try {
            socketListenerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void acceptConnections() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(this.port);
            serverSocket.setSoTimeout(1000);
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    this.server.addClient(socket);
                }
                catch (IOException e) {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
