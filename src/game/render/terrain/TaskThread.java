package sekelsta.game.render.terrain;

import java.util.concurrent.*;
import java.util.function.*;

import sekelsta.engine.Pair;

public class TaskThread<In, Out> extends Thread {
    public final BlockingQueue<Pair<In, Out>> completed = new LinkedBlockingQueue<>();

    private BlockingQueue<In> tasks = new LinkedBlockingQueue<>();
    private Function<In, Out> function;
    private boolean running = true;
    private In currentTask = null;

    public TaskThread(String name, Function<In, Out> function) {
        super(name);
        setDaemon(true);
        this.function = function;
    }

    @Override
    public void run() {
        while(running) {
            try {
                currentTask = tasks.take();
                Out result = function.apply(currentTask);
                if (result != null) {
                    completed.add(new Pair<In, Out>(currentTask, result));
                }
                synchronized (this) {
                    currentTask = null;
                }
            }
            catch (InterruptedException e) {}
        }
    }

    public void setDone() {
        running = false;
    }

    public synchronized void queueTask(In task) {
        if (!tasks.contains(task) && !task.equals(currentTask)) {
            tasks.add(task);
        }
    }

    public synchronized boolean hasTask(Predicate<In> predicate) {
        for (In task : tasks) {
            if (predicate.test(task)) {
                return true;
            }
        }
        if (currentTask != null && predicate.test(currentTask)) {
            return true;
        }
        for (Pair<In, Out> result : completed) {
            if (predicate.test(result.getKey())) {
                return true;
            }
        }
        return false;
    }
}
