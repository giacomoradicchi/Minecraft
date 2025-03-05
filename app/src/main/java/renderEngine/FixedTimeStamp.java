package renderEngine;

public class FixedTimeStamp {
    // Serve a mantenere una frequenza di aggiornamento fissa
    
    private final float FIXED_TIMESTEP;
    private float accumulator = 0;

    public FixedTimeStamp(float hertz) {
        this.FIXED_TIMESTEP = 1/hertz;
    }

    public void accumulateTime(float frameTime) {
        accumulator += frameTime;
    }

    public boolean shouldUpdate() {
        if(accumulator >= FIXED_TIMESTEP) {
            accumulator -= FIXED_TIMESTEP;
            return true;
        }

        return false;
    }

    public float getFixedTimeStep() {
        return FIXED_TIMESTEP;
    }
}
