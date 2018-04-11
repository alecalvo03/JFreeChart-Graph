package com.company;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Usuario on 16/03/2018.
 */

public class JavaSocket {

    private String serverAddress = "192.168.1.10";
    Socket s;

    public JavaSocket() throws IOException {
        s = new Socket(serverAddress, 27010);
    }

    public  void writeToServer(String msg){
        PrintWriter out = null;
        try {
            out = new PrintWriter(s.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (out != null) {
            out.println(msg);
        }
    }
}
