package com.boxedmeatrevolution.brains;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

                try {
                    while(true) {
                        Message message = (Message) inputStream.readObject();
                        if (message.type == Message.Type.MOTHER_SEND_RESULT) {
                            Message.MotherSendResult data = (Message.MotherSendResult) message.data;
                            synchronized (_taskIds) {
                                _taskIds.add(data.taskId);
                                _results.add(data.result);
                            }
                        }
                        else if (message.type == Message.Type.MOTHER_REQUEST_TASK) {
                            final Task task = ((Message.MotherRequestTask) message.data).task;
                            task.registerListener(new Task.TaskRequestedEventListener() {
                                @Override
                                public List<Serializable> onTaskRequested(List<Task> tasks) throws IOException {

                                    for (Task task : tasks) {
                                        synchronized (Brain.this._lock) {
                                            outputStream.writeObject(new Message(Message.Type.BRAIN_REQUEST_TASK, new Message.BrainRequestTask(task)));
                                            outputStream.flush();
                                        }
                                    }

                                    List<Serializable> result = new ArrayList<>();

                                    for (Task task : tasks) {

                                        while (!_taskIds.contains(task.getId())) {
                                        }

                                        synchronized (_taskIds) {
                                            int taskIndex = _taskIds.indexOf(task.getId());
                                            _taskIds.remove(taskIndex);
                                            result.add(_results.remove(taskIndex));
                                        }
                                    }
                                    return result;
                                }
                            });
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Serializable result = task.call();
                                        synchronized (Brain.this._lock) {
                                            outputStream.writeObject(new Message(Message.Type.BRAIN_SEND_RESULT, new Message.BrainSendResult(task.getId(), result)));
                                            outputStream.flush();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        System.exit(0);
                                    }
                                }
                            }).start();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            private List<UUID> _taskIds = new ArrayList<>();
            private List<Serializable> _results = new ArrayList<>();
        }).start();

    }

    private final Integer _lock = 3;
}
