package it.polimi.mw.compinf.http;

import it.polimi.mw.compinf.http.TaskRegistryActor.Task;

import java.io.Serializable;

public interface TaskRegistryMessages {

    class GetTasks implements Serializable {
    }

    class ActionPerformed implements Serializable {
        private final String description;

        public ActionPerformed(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    class CreateTask implements Serializable {
        private final Task task;

        public CreateTask(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }

    class GetTask implements Serializable {
        private final int id;

        public GetTask(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    class DeleteTask implements Serializable {
        private final int id;

        public DeleteTask(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}