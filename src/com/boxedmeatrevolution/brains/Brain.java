package com.boxedmeatrevolution.brains;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by aidan on 2016-06-25.
 */
public final class Brain {

    public Brain(Socket socket) throws IOException {
        final ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        final ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Task task;
                try {
                    task = (Task) inputStream.readObject();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Serializable result = task.call();
                            try {
                                outputStream.writeLong(task.getId());
                                outputStream.writeObject(result);
                                outputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.exit(0);
                            }
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
