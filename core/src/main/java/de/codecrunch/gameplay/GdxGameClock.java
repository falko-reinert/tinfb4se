package de.codecrunch.gameplay;

import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class GdxGameClock implements GameClock {
    private Task task;

    @Override
    public void start(final Runnable tick) {
        task = new Task() {
            @Override
            public void run() {
                tick.run();
            }
        };
        Timer.instance().scheduleTask(task, 0, 1);
        Timer.instance().start();
    }

    @Override
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
