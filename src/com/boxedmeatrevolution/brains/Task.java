package com.boxedmeatrevolution.brains;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by aidan on 2016-06-25.
 */
public abstract class Task implements Serializable {

    public Task() {
    }

    public UUID getId() {
        return _id;
    }

    public List<Serializable> requestTasks(List<Task> tasks) throws IOException {
        return _listener.onTaskRequested(tasks);
    }

    public void registerListener(TaskRequestedEventListener listener) {
        _listener = listener;
    }

    public static interface TaskRequestedEventListener {
        List<Serializable> onTaskRequested(List<Task> task) throws IOException;
    }
    public abstract Serializable call() throws IOException;

    private UUID _id = UUID.randomUUID();

    private TaskRequestedEventListener _listener = null;

}
