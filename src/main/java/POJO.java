import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class POJO {//just random data
    private String s1;
    private String s2;
    private int i1;
    private int i2;

    public static POJO get(int i) {
        return new POJO("field one " + i, "field two " + i, i, i * 10);
    }

    public String getS1() {
        return s1;
    }

    public void setS1(String s1) {
        this.s1 = s1;
    }

    public String getS2() {
        return s2;
    }

    public void setS2(String s2) {
        this.s2 = s2;
    }

    public int getI1() {
        return i1;
    }

    public void setI1(int i1) {
        this.i1 = i1;
    }

    public int getI2() {
        return i2;
    }

    public void setI2(int i2) {
        this.i2 = i2;
    }

    public POJO(String s1, String s2, int i1, int i2) {
        this.s1 = s1;
        this.s2 = s2;
        this.i1 = i1;
        this.i2 = i2;
    }
    final static  Gson gson = new GsonBuilder().create();
    public String getJson() {
        return gson.toJson(this);
    }
}
