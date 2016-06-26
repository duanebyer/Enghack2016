package com.boxedmeatrevolution.brains;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by aidan on 2016-06-25.
 */
public final class MotherBrain {

    public MotherBrain(List<Socket> sockets) throws IOException {
        _sockets = new ArrayList<>(sockets);
        _inputStreams = new ArrayList<>();
        _outputStreams = new ArrayList<>();
        _numTasksPerSocket = new ArrayList<>();
        for (int i = 0; i < _sockets.size(); ++i) {
            _numTasksPerSocket.add(0);
            _sockets.get(i).getInputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(_sockets.get(i).getOutputStream());
            outputStream.flush();
            final ObjectInputStream inputStream = new ObjectInputStream(_sockets.get(i).getInputStream());
            _outputStreams.add(outputStream);
            _inputStreams.add(inputStream);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        long taskId = 0;
                        try {
                            taskId = inputStream.readLong();
                            Serializable result = (Serializable) inputStream.readObject();
                            taskFinished(taskId, result);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(0);
                        }
                    }
                }

                private synchronized void taskFinished(long taskId, Serializable result) {
                    int taskIndex = -1;
                    for (int i = 0; i < _tasks.size(); ++i) {
                        if (_tasks.get(i).getId() == taskId) {
                            taskIndex = i;
                            break;
                        }
                    }

                    _taskListeners.get(taskIndex).onTaskFinished(result);

                    _tasks.remove(taskIndex);
                    _taskListeners.remove(taskIndex);
                    Socket taskSocket = _socketForTask.remove(taskIndex);
                    int socketIndex = _sockets.indexOf(taskSocket);
                    _numTasksPerSocket.set(socketIndex, _numTasksPerSocket.get(socketIndex) - 1);
                }
            }).start();
        }
    }

    public void dispatch(Task task, TaskFinishedEventListener listener) throws IOException {
        Integer min = Collections.min(_numTasksPerSocket);
        int minIndex = _numTasksPerSocket.indexOf(min);

        final ObjectOutputStream outputStream = _outputStreams.get(minIndex);
        outputStream.writeObject(task);
        outputStream.flush();

        final ObjectInputStream inputStream = _inputStreams.get(minIndex);

        final Socket socket = _sockets.get(minIndex);

        _tasks.add(task);
        _taskListeners.add(listener);
        _socketForTask.add(_sockets.get(minIndex));

        int socketIndex = _sockets.indexOf(_sockets.get(minIndex));
        _numTasksPerSocket.set(socketIndex, _numTasksPerSocket.get(socketIndex) + 1);
    }

    public static interface TaskFinishedEventListener {
        void onTaskFinished(Serializable result);
    }

    List<Socket> _socketForTask = new ArrayList<>();
    List<Task> _tasks = new ArrayList<>();
    List<TaskFinishedEventListener> _taskListeners = new ArrayList<>();

    List<ObjectInputStream> _inputStreams;
    List<ObjectOutputStream> _outputStreams;
    List<Socket> _sockets;
    List<Integer> _numTasksPerSocket;
}
