package com.example.videotest1;

import android.content.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private List<Socket> socketList;
    private SocketListener socketListener;
    Context context;

    Server(Context context, int port) {
        this.context = context;
        this.socketList = new ArrayList<>();
        this.socketListener = new SocketListener(port, this);
    }

    public void start() {

        this.socketListener.start();
    }

    public void stop() {
        this.socketListener.stop();
    }

    public void addClient(Socket socket) {
        if (!socketList.contains(socket)) {
            socketList.add(socket);
        }
    }

    public void streamToClients(byte[] data) {
        for (Socket socket : this.socketList) {
            if (socket == null || socket.isClosed()) {
                socketList.remove(socket);
            }
            else {
                try {
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(data);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
