package com.boxedmeatrevolution.brains;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aidan on 2016-06-25.
 */
public final class Brain {

    public Brain(Socket socket) throws IOException {
        _socket = socket;
        _inputStream = new ObjectInputStream(socket.getInputStream());
        _outputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    public void update() throws IOException, ClassNotFoundException {
        final Task nextTask = receiveTask();
        if (nextTask != null) {

            int tempTaskIndex = _tasks.size();
            for (int i = 0; i < _tasks.size(); ++i) {
                if (_tasks.get(i) == null) {
                    tempTaskIndex = i;
                    break;
                }
            }
            final int taskIndex = tempTaskIndex;

            if (taskIndex == _tasks.size()) {
                _tasks.add(null);
                _taskResults.add(null);
                _threads.add(null);
            }

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Serializable result = nextTask.call();
                    _taskResults.set(taskIndex, result);
                }
            });

            _tasks.set(taskIndex, nextTask);
            _threads.set(taskIndex, thread);
        }

        for (int i = 0; i < _threads.size(); ++i) {
            if (!_threads.get(i).isAlive()) {

                sendTaskFinished(_tasks.get(i).getId(), _taskResults.get(i));

                _tasks.set(i, null);
                _taskResults.set(i, null);
                _threads.set(i, null);

                while (_tasks.get(_tasks.size() - 1) == null) {
                    _tasks.remove(_tasks.size() - 1);
                    _taskResults.remove(_tasks.size() - 1);
                    _threads.remove(_tasks.size() - 1);
                }
            }
        }
    }

    private Task receiveTask() throws IOException, ClassNotFoundException {
        if (_inputStream.available() != 0) {
            return (Task) _inputStream.readObject();
        }
        return null;
    }

    private void sendTaskFinished(long taskId, Serializable result) throws IOException {
        _outputStream.writeLong(taskId);
        _outputStream.writeObject(result);
    }

    private final List<Task> _tasks = new ArrayList<>();
    private final List<Serializable> _taskResults = new ArrayList<>();
    private final List<Thread> _threads = new ArrayList<>();

    private ObjectInputStream _inputStream;
    private ObjectOutputStream _outputStream;
    private Socket _socket;

}
