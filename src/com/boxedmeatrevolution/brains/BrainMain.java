package com.boxedmeatrevolution.brains;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by aidan on 2016-06-25.
 */
public final class BrainMain {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("10.213.124.123.23", 8080);
        Brain brain = new Brain(socket);
        while (true) {
            brain.update();
        }
    }

}
