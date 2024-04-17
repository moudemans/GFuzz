import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.GregorianCalendar;

public class intGenerator extends Generator<String> {


    public intGenerator() {
        super(String.class); // Register the type of objects that we can create
    }

    @Override
    public String generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        return  sourceOfRandomness.nextInt(200) + "";
    }
}
