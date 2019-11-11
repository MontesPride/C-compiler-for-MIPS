package gen;

import java.util.*;

public class PrefixAdder {
    private String prefix;
    private int count = 0;
    private static Set<String> prefixes = new HashSet<>();

    public PrefixAdder (String prefix) { this.prefix = prefix; }

    public String addPrefix(String prefix) {
        prefix = this.prefix + "_" + prefix;

        if (prefixes.contains(prefix)) {
            throw new RuntimeException("Somehow generated duplicate label " + prefix);
        }

        prefixes.add(prefix);
        return prefix;
    }

    public String num() {
        String prefix = String.format("%03d", count);
        count++;

        return addPrefix(prefix);
    }

    public String num(String context) {
        String prefix = String.format("%s_%03d", context, count);
        count++;

        return addPrefix(prefix);
    }

    public static void verifyPrefix(String prefix) {
        if (!prefixes.contains(prefix)) {
            throw new RuntimeException("Attempting to use label, but could not be found!");
        }
    }
}
