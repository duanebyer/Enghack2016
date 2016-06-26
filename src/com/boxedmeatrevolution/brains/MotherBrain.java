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
        
        synchronized(_lock) {
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
            }
        }
        
        for (int i = 0; i < _sockets.size(); ++i) {
            
            final ObjectInputStream inputStream = _inputStreams.get(i);
            final ObjectOutputStream outputStream = _outputStreams.get(i);
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        long taskId = 0;
                        try {
                            Message message = (Message) inputStream.readObject();
                            if (message.type == Message.Type.BRAIN_SEND_RESULT) {
                                Message.BrainSendResult data = (Message.BrainSendResult) message.data;
                                taskFinished(data.taskId, data.result);
                            }
                            else if (message.type == Message.Type.BRAIN_REQUEST_TASK) {
                                final Task task = ((Message.BrainRequestTask) message.data).task;
                                dispatch(task, new TaskFinishedEventListener() {
                                    @Override
                                    public void onTaskFinished(Serializable result) throws IOException {
                                        synchronized (MotherBrain.this._lock) {
                                            outputStream.writeObject(new Message(Message.Type.MOTHER_SEND_RESULT, new Message.MotherSendResult(task.getId(), result)));
                                            outputStream.flush();
                                        }
                                    }
                                });
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                            System.exit(0);
                        }
                    }
                }

                private void taskFinished(long taskId, Serializable result) throws IOException {
                    synchronized(MotherBrain.this._lock) {
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
            }).start();
        }
    }

    public void dispatch(Task task, TaskFinishedEventListener listener) throws IOException {
        synchronized(_lock) {
            Integer min = Collections.min(_numTasksPerSocket);
            int minIndex = _numTasksPerSocket.indexOf(min);

            final ObjectOutputStream outputStream = _outputStreams.get(minIndex);

            outputStream.writeObject(new Message(Message.Type.MOTHER_REQUEST_TASK, new Message.MotherRequestTask(task)));
            outputStream.flush();

//            final ObjectInputStream inputStream = _inputStreams.get(minIndex);
//
//            final Socket socket = _sockets.get(minIndex);

            _tasks.add(task);
            _taskListeners.add(listener);
            _socketForTask.add(_sockets.get(minIndex));

            int socketIndex = _sockets.indexOf(_sockets.get(minIndex));
            _numTasksPerSocket.set(socketIndex, _numTasksPerSocket.get(socketIndex) + 1);
        }
    }

    public static interface TaskFinishedEventListener {
        void onTaskFinished(Serializable result) throws IOException;
    }

    private List<Socket> _socketForTask = new ArrayList<>();
    private List<Task> _tasks = new ArrayList<>();
    private List<TaskFinishedEventListener> _taskListeners = new ArrayList<>();

    private List<ObjectInputStream> _inputStreams;
    private List<ObjectOutputStream> _outputStreams;
    private List<Socket> _sockets;
    private List<Integer> _numTasksPerSocket;
    
    private final Integer _lock = 3;
}
