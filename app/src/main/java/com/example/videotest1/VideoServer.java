package com.example.videotest1;

import android.content.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class VideoServer extends Server {
    VideoServer(Context context, int port) {
        super(context, port);
    }

    @Override
    public void addClient(Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            String httpHeader = "HTTP/1.0 200 OK\r\n"
                    + "Server: VideoTest1\r\n"
                    + "Connection: close\r\n"
                    + "Max-Age: 0\r\n"
                    + "Expires: 0\r\n"
                    + "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, "
                    + "post-check=0, max-age=0\r\n"
                    + "Pragma: no-cache\r\n"
                    + "Access-Control-Allow-Origin:*\r\n"
                    + "Content-Type: multipart/x-mixed-replace; "
                    + "boundary=--36b8cda5-1480-4e2c-8df2-50477bebd28e--\r\n";
            String boundaryLine = "\r\n--36b8cda5-1480-4e2c-8df2-50477bebd28e--\r\n";
            outputStream.write(httpHeader.getBytes());
            outputStream.write(boundaryLine.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.addClient(socket);
    }
}
