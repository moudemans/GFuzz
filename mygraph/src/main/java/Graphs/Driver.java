package Graphs;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Driver {

    private static final int mutation_count = 10000;
    private static final String input_dir = "examplesCoverage/src/main/resources/P9-GMARK/";
    private static final String output_dir = "examplesCoverage/src/main/resources/P9-GMARK-MUTATED2/";
    private static final String output_file_name = "mutated_";
    private static Random r = new Random();

    public static void main(String[] args) {
        ArrayList<String> files_in_dir = new ArrayList<>(get_files_in_dir());

        for (int i = 0; i < mutation_count; i++) {
            int random_file_index = r.nextInt(files_in_dir.size());
            String random_file_path = files_in_dir.get(random_file_index);

            GraphMutator.mutateFile(input_dir , random_file_path, output_dir , output_file_name + i + ".ser", null);
        }

    }

    private static Set<String> get_files_in_dir() {
        return Stream.of(new File(input_dir).listFiles())
                .filter(file -> !file.isDirectory())
                .filter(file -> file.getName().contains(".ser"))
                .filter(file -> file.getName().contains("test"))
                .map(File::getName)
                .collect(Collectors.toSet());
    }

}
