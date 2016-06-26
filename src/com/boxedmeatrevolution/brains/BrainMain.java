package com.boxedmeatrevolution.brains;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by aidan on 2016-06-25.
 */
public final class BrainMain {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("DESKTOP-5889S5H", 9122);

        System.out.println("HEYOOOOO");

        Brain brain = new Brain(socket);

        while (true) {}
    }

}
