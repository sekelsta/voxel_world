package sekelsta.game.render.terrain;

import java.util.concurrent.*;
import java.util.function.Function;

import sekelsta.engine.Pair;

public class TaskThread<In, Out> extends Thread {
    public final BlockingQueue<Pair<In, Out>> completed = new LinkedBlockingQueue<>();

    private BlockingQueue<In> tasks = new LinkedBlockingQueue<>();
    private Function<In, Out> function;
    private boolean running = true;

    public TaskThread(String name, Function<In, Out> function) {
        super(name);
        setDaemon(true);
        this.function = function;
    }

    @Override
    public void run() {
        while(running) {
            try {
                In task = tasks.take();
                Out result = function.apply(task);
                if (result != null) {
                    completed.add(new Pair<In, Out>(task, result));
                }
            }
            catch (InterruptedException e) {}
        }
    }

    public void setDone() {
        running = false;
    }

    public synchronized void queueTask(In task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }
}
