import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class UndoneTask {
    private final ArrayList<Task> taskList = new ArrayList<>();
    private final AtomicBoolean isEnd = new AtomicBoolean(false);

    public void waitForTask() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void notifyScheduler() {
        synchronized (this) {
            notifyAll();
        }
    }

    public void add(Task task) {
        synchronized (this) {
            taskList.add(task);
            notifyAll();
        }
    }

    public Task get() {
        Task task = null;
        synchronized (this) {
            if (!taskList.isEmpty()) {
                task = taskList.get(0);
                taskList.remove(0);
            }
        }
        return task;
    }

    public boolean isEmpty() {
        synchronized (this) {
            return taskList.isEmpty();
        }
    }

    public void addToFront(Quest quest) {
        synchronized (this) {
            taskList.add(0, quest);
            notifyAll();
        }
    }

    public void setEnd() {
        this.isEnd.set(true);
        notifyScheduler();
    }

    public boolean isEnd() {
        return isEnd.get();
    }

    public Reset getReset() {
        Reset mark = null;
        synchronized (this) {
            for (Task task : taskList) {
                if (task instanceof Reset) {
                    mark = (Reset) task;
                }
            }
            if (mark == null) {
                return null;
            }
            taskList.remove(mark);
            return mark;
        }
    }
}
