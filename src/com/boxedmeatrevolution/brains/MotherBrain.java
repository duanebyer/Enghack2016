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
            ObjectInputStream inputStream = new ObjectInputStream(_sockets.get(i).getInputStream());
            _outputStreams.add(outputStream);
            _inputStreams.add(inputStream);
        }
    }

    public void dispatch(Task task, TaskFinishedEventListener listener) throws IOException {
        Integer min = Collections.min(_numTasksPerSocket);
        int minIndex = _numTasksPerSocket.indexOf(min);

        ObjectOutputStream outputStream = _outputStreams.get(minIndex);
        outputStream.writeObject(task);
        outputStream.flush();

        _tasks.add(task);
        _taskListeners.add(listener);
        _socketForTask.add(_sockets.get(minIndex));

        int socketIndex = _sockets.indexOf(_sockets.get(minIndex));
        _numTasksPerSocket.set(socketIndex, _numTasksPerSocket.get(socketIndex) + 1);
    }

    public void update() throws IOException, ClassNotFoundException {
        for (int inputIndex = 0; inputIndex < _inputStreams.size(); ++inputIndex) {

            if (_inputStreams.get(inputIndex).available() == 0) {
                break;
            }
            long taskId = _inputStreams.get(inputIndex).readLong();
            Serializable result = (Serializable) _inputStreams.get(inputIndex).readObject();

            if (result != null) {
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
        }
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
