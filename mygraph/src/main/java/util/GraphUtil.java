package util;
import tudcomponents.MyGraph;
import org.apache.commons.lang.SerializationUtils;

import java.util.Random;

public class GraphUtil {

    public static MyGraph bitMutation(MyGraph g, int bitflipCount, Random r) {
        byte[] data = SerializationUtils.serialize(g);

        int MIN_BYTE_VALUE = -128;
        int MAX_BYTE_VALUE = 127;

        if (r == null) {
            r = new Random();
        }

        for (int i = 0; i < bitflipCount; i++) {
            int byteIndex = r.nextInt(data.length);

            int mutation = 1;
            if ( r.nextBoolean() ) {
                    mutation = -1;
            }

            Integer mutatedByteValue =(int) data[byteIndex] + mutation;

            data[byteIndex] = mutatedByteValue.byteValue();
        }

        MyGraph newGraph =(MyGraph) SerializationUtils.deserialize(data);

        return newGraph;
    }

    public static MyGraph byteMutation(MyGraph g, int byteCount, Random r, int max_tries) {


        byte[] data = SerializationUtils.serialize(g);

        int MIN_BYTE_VALUE = -128;
        int MAX_BYTE_VALUE = 127;

        if (r == null) {
            r = new Random();
        }

        for (int i = 0; i < byteCount; i++) {
            int byteIndex = r.nextInt(data.length);
            int byteValue = r.nextInt(256) + MIN_BYTE_VALUE;
            data[byteIndex] = (byte) byteValue;
        }

        try {
            MyGraph newGraph = (MyGraph) SerializationUtils.deserialize(data);
            return newGraph;
        } catch (Exception | NoClassDefFoundError e) {
            // After specified amount of tries, return the original graph
            if (max_tries <= 0) {
                return g;
            }
            return byteMutation(g, byteCount, r, max_tries -1);
        }
    }


}
