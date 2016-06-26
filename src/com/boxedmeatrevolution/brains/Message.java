package com.boxedmeatrevolution.brains;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by aidan on 2016-06-26.
 */
public final class Message implements Serializable {

    public Message(Type type, Object data) {
        this.type = type;
        this.data = data;
    }

    public enum Type implements Serializable {
        BRAIN_REQUEST_TASK,
        MOTHER_SEND_RESULT,
        BRAIN_SEND_RESULT,
        MOTHER_REQUEST_TASK
    }

    public static class BrainRequestTask implements Serializable {
        public BrainRequestTask(Task task) {
            this.task = task;
        }
        Task task;
    }

    public static class MotherSendResult implements Serializable {
        public MotherSendResult(UUID taskId, Serializable result) {
            this.taskId = taskId;
            this.result = result;
        }
        UUID taskId;
        Serializable result;
    }

    public static class BrainSendResult implements Serializable {
        public BrainSendResult(UUID taskId, Serializable result) {
            this.taskId = taskId;
            this.result = result;
        }
        UUID taskId;
        Serializable result;
    }

    public static class MotherRequestTask implements Serializable {
        public MotherRequestTask(Task task) {
            this.task = task;
        }
        Task task;
    }

    Type type;
    Object data;

}
