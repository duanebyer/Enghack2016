package com.boxedmeatrevolution.brains;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

/**
 * Created by aidan on 2016-06-25.
 */
public class MotherBrainMain {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket server = new ServerSocket(9122);
        Socket socket = server.accept();

        MotherBrain motherBrain = new MotherBrain(Arrays.asList(socket));

        motherBrain.dispatch(
                new ListSorterTask<>(Arrays.asList(34, 7, 34, 65, 3, 1, 3, 7, 9, 2, 8, 9, 6, 3)),
                new MotherBrain.TaskFinishedEventListener() {
                    @Override
                    public void onTaskFinished(Serializable result) {
                        System.out.println((List<Integer>) result);
                    }
                });

        while (true) {
            motherBrain.update();
        }
    }

}
