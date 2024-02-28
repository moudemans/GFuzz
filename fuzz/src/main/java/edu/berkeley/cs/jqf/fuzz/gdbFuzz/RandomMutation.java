package edu.berkeley.cs.jqf.fuzz.gdbFuzz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RandomMutation implements GDBFuzzMutation{
    @Override
    public void mutate(File inputFile, File nextInputFile) throws IOException {

    }

    @Override
    public ArrayList<String> mutateFile(File inputFile) throws IOException {
        return null;
    }

    @Override
    public void mutate(ArrayList<String> rows) {

    }

    @Override
    public void randomGenerateRows(ArrayList<String> rows) {

    }

    @Override
    public void writeFile(File outputFile, List<String> fileRows) throws IOException {

    }

    @Override
    public void deleteFile(String currentFile) throws IOException {

    }
}
