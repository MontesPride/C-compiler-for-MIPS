package gen;

import java.util.*;

public class Labeller {
    private String label;
    private int count = 0;
    private static Set<String> labels = new HashSet<>();
    private static final int leadingZeros = 9;

    public Labeller (String label) { this.label = label; }

    public String addLabel(String label) {
        label = this.label + "_" + label;

        if (labels.contains(label)) {
            throw new RuntimeException("Duplicate label has been generated - " + label + " - (try increasing the number of leading zeros)");
        }

        labels.add(label);
        return label;
    }

    public String enumLabel() {
        String prefix = String.format(String.format("%%0%sd", leadingZeros), count);
        count++;

        return addLabel(prefix);
    }

    public String enumLabel(String context) {
        String prefix = String.format(String.format("%%s_%%0%sd", leadingZeros), context, count);
        count++;

        return addLabel(prefix);
    }

    public static void verifyLabel(String prefix) {
        if (!labels.contains(prefix)) {
            throw new RuntimeException("Attempting to use a label that has not been created yet!");
        }
    }
}
