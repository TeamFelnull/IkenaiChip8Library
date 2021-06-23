package red.felnull.ikenaichip8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The IKISUGI CHIP-8 emulator library
 *
 * @author MORIMORI0317
 */
public class Chip8 {
    private final List<C8Listener> c8GFXListeners = new ArrayList<>();
    private final List<C8Listener> c8CycleListeners = new ArrayList<>();
    protected boolean[] key = new boolean[16];
    protected boolean drawFlag = true;
    protected boolean[] graphic = new boolean[64 * 32];
    private long cycleSpeed = 2;
    protected final Memory memory;
    private final CPU cpu;
    private boolean run;
    private boolean init;
    private boolean resetFlag;
    private byte[] romData;
    protected boolean nonThrow;
    private boolean pause;

    public Chip8() {
        this.memory = new Memory(this);
        this.cpu = new CPU(this);
    }

    /**
     * Load rom data
     *
     * @param rom Rom data
     */
    public void loadROM(byte[] rom) {
        this.romData = rom;
    }

    /**
     * Ignore any exceptions that occur in the cycle
     *
     * @param nonThrow Whether to non Throwable
     */
    public void setNonThrow(boolean nonThrow) {
        this.nonThrow = nonThrow;
    }

    /**
     * The initialize
     * Not required when running with run or runAsync
     */
    public void init() {
        if (!init) {
            if (romData == null)
                throw new IllegalStateException("No rom data");
            cpu.init();
            memory.init(romData);
            Arrays.fill(graphic, false);
            Arrays.fill(key, false);
            init = true;
        }
    }

    /**
     * The reset;
     */
    public void reset() {
        if (run) {
            resetFlag = true;
        } else {
            init = false;
            init();
        }
    }

    /**
     * Be sure to initialize before running
     */
    public void cycle() {
        cpu.cycle();
    }

    /**
     * Start emulation
     */
    public void run() {
        if (run)
            return;

        this.run = true;
        if (!init)
            init();
        try {
            while (run) {
                if (pause) {
                    Thread.sleep(10);
                    continue;
                }

                if (resetFlag) {
                    init = false;
                    init();
                    resetFlag = false;
                }
                try {
                    cycle();
                } catch (Exception ex) {
                    if (!nonThrow)
                        throw ex;
                }
                if (drawFlag) {
                    c8GFXListeners.forEach(n -> n.update(this));
                    drawFlag = false;
                }
                c8CycleListeners.forEach(n -> n.update(this));
                Thread.sleep(cycleSpeed);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            run = false;
        }
    }

    /**
     * Pause emulation
     */
    public void pause() {
        this.pause = true;
    }

    /**
     * Get if drawn
     * (64*32)
     *
     * @param x Xpos
     * @param y Ypos
     * @return drawn
     */
    public boolean getGraphic(int x, int y) {
        return graphic[(y * 64) + x];
    }

    /**
     * Set drawn
     * (64*32)
     *
     * @param x Xpos
     * @param y YposF
     * @return drawn
     */
    public void setGraphic(int x, int y, boolean draw) {
        graphic[(y * 64) + x] = draw;
    }

    /**
     * Unpause emulation
     */
    public void unpause() {
        this.pause = false;
    }

    /**
     * Start emulation async
     */
    public void runAsync() {
        if (run)
            return;
        RunThread runThread = new RunThread();
        runThread.start();
    }

    /**
     * Stop emulation
     */
    public void stop() {
        this.run = false;
        this.init = false;
    }

    /**
     * Added graphics listener
     * Called when the graphic changes
     *
     * @param listener 　gfx listener
     */
    public void addGFXListener(C8Listener listener) {
        c8GFXListeners.add(listener);
    }

    /**
     * Added cycle listener
     * Called when the one cycle
     *
     * @param listener cycle listener
     */
    public void addCycleListener(C8Listener listener) {
        c8CycleListeners.add(listener);
    }

    /**
     * Whether there is a beep
     *
     * @return Is beeping
     */
    public boolean isBeeping() {
        return cpu.isBeeping();
    }

    /**
     * Whether there is a paused
     *
     * @return Is paused
     */
    public boolean isPause() {
        return pause;
    }

    /**
     * Whether there is a run
     *
     * @return Is run
     */
    public boolean isRun() {
        return run;
    }

    /**
     * Press the key
     * 0,1,2,3
     * 4,5,6,7
     * 8,9,A,B
     * C,D,E,F
     *
     * @param keyNum key number
     */
    public void pressKey(int keyNum) {
        key[keyNum] = true;
    }

    /**
     * Release the key
     * 0,1,2,3
     * 4,5,6,7
     * 8,9,A,B
     * C,D,E,F
     *
     * @param keyNum key number
     */
    public void releaseKey(int keyNum) {
        key[keyNum] = false;
    }

    /**
     * Get the cycle speed
     *
     * @return cycle speed (ms)
     */
    public long getCycleSpeed() {
        return cycleSpeed;
    }

    /**
     * Set the cycle speed
     * Defalt is 2ms
     *
     * @param speed Cycle speed (ms)
     */
    public void setCycleSpeed(long speed) {
        this.cycleSpeed = speed;
    }

    /**
     * Set the cycle speed
     *
     * @param speedHz Cycle speed (Hz)
     */
    public void setCycleSpeedHz(double speedHz) {
        this.cycleSpeed = (long) (1d / speedHz * 1000d);
    }

    private class RunThread extends Thread {
        public RunThread() {
            setName("Chip8 Runner");
        }

        @Override
        public void run() {
            Chip8.this.run();
        }
    }

    public static interface C8Listener {
        void update(Chip8 chip8);
    }
}
