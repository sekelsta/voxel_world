package sekelsta.game.render.terrain;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import sekelsta.engine.Pair;
import sekelsta.game.terrain.ChunkPos;

public class MeshingThread extends Thread {
    public final BlockingQueue<Set<Pair<ChunkPos, TerrainMeshData>>> completed = new LinkedBlockingQueue<>();

    private BlockingQueue<Set<ChunkPos>> tasks = new LinkedBlockingQueue<>();
    private Function<Set<ChunkPos>, Set<Pair<ChunkPos, TerrainMeshData>>> function;
    private boolean running = true;
    private Set<ChunkPos> currentTask = null;

    public MeshingThread(String name, Function<Set<ChunkPos>, Set<Pair<ChunkPos, TerrainMeshData>>> function) {
        super(name);
        setDaemon(true);
        this.function = function;
    }

    @Override
    public void run() {
        while(running) {
            try {
                currentTask = tasks.take();
                Set<Pair<ChunkPos, TerrainMeshData>> result = function.apply(currentTask);
                if (result != null) {
                    completed.add(result);
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

    public synchronized void queueTask(Set<ChunkPos> task) {
        ArrayList<Set<ChunkPos>> oldTasks = new ArrayList<>();
        ArrayList<Set<ChunkPos>> mergedTasks = new ArrayList<>();
        while (currentTask == null && !tasks.isEmpty()) {
            Thread.yield();
        }
        tasks.drainTo(oldTasks);

        boolean merged = false;
        for (Set<ChunkPos> oldTask : oldTasks) {
            boolean overlap = false;
            for (ChunkPos item : oldTask) {
                if (task.contains(item)) {
                    overlap = true;
                    break;
                }
            }
            if (overlap) {
                task.addAll(oldTask);
            }
            else {
                mergedTasks.add(oldTask);
                merged = true;
            }
        }

        if (merged) {
            tasks.add(task);
        }
        else {
            mergedTasks.add(task);
        }
        tasks.addAll(mergedTasks);
    }

    public synchronized void queueTask(ChunkPos pos) {
        for (Set<ChunkPos> task : tasks) {
            if (task.contains(pos)) {
                return;
            }
        }
        if (currentTask != null && currentTask.contains(pos)) {
            return;
        }
        for (Set<Pair<ChunkPos, TerrainMeshData>> result : completed) {
            for (Pair<ChunkPos, TerrainMeshData>  pair : result) {
                if (pair.getKey().equals(pos)) {
                    return;
                }
            }
        }

        queueTask(Collections.singleton(pos));
    }
}
