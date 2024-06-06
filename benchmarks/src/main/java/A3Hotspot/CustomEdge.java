package A3Hotspot;


public class CustomEdge{

    private long timeStamp;
    private boolean isStatic;

    CustomVertex source;
    CustomVertex target;

    public CustomEdge(boolean isStatic, long timeStamp) {
        this.isStatic = isStatic;
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public boolean getIsStatic() {
        return this.isStatic;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    @Override
    public String toString() {
        return "(" + getIsStatic() + " : " + getTimeStamp() + "   " +  super.toString() +  ")";
    }

    public CustomVertex getSource() {
        return source;
    }

    public CustomVertex getTarget() {
        return target;
    }

}
