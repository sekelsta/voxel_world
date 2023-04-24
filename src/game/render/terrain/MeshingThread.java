package sekelsta.game.render.terrain;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import sekelsta.engine.Pair;
import sekelsta.game.terrain.ChunkPos;

public class MeshingThread extends Thread {
    public final BlockingQueue<Pair<ArrayList<ChunkPos>, ArrayList<TerrainMeshData>>> completed = new LinkedBlockingQueue<>();

    private BlockingQueue<ArrayList<ChunkPos>> tasks = new LinkedBlockingQueue<>();
    private Function<ArrayList<ChunkPos>, ArrayList<TerrainMeshData>> function;
    private boolean running = true;
    private ArrayList<ChunkPos> currentTask = null;

    public MeshingThread(String name, Function<ArrayList<ChunkPos>, ArrayList<TerrainMeshData>> function) {
        super(name);
        setDaemon(true);
        this.function = function;
    }

    @Override
    public void run() {
        while(running) {
            try {
                currentTask = tasks.take();
                ArrayList<TerrainMeshData> result = function.apply(currentTask);
                if (result != null) {
                    completed.add(new Pair<ArrayList<ChunkPos>, ArrayList<TerrainMeshData>>(currentTask, result));
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

    public synchronized void queueTask(ArrayList<ChunkPos> task) {
        if (!tasks.contains(task) && !task.equals(currentTask)) {
            tasks.add(task);
        }
    }

    public synchronized void queueTask(ChunkPos pos) {
        for (ArrayList<ChunkPos> task : tasks) {
            if (task.contains(pos)) {
                return;
            }
        }
        if (currentTask != null && currentTask.contains(pos)) {
            return;
        }
        for (Pair<ArrayList<ChunkPos>, ArrayList<TerrainMeshData>> result : completed) {
            if (result.getKey().contains(pos)) {
                return;
            }
        }

        ArrayList<ChunkPos> t = new ArrayList<>();
        t.add(pos);
        queueTask(t);
    }
}
