package gen;

import java.io.PrintWriter;
import java.lang.AutoCloseable;

public class OutputWriter implements AutoCloseable {
    private PrintWriter writer;
    private int level = 0;
    private static final int width = 4;

    // Magic stuff
    private String label = ""; // current label
    private boolean wasNewline = false;
    private final boolean debugWriter = System.getenv("DEBUG") != null;

    public OutputWriter(PrintWriter writer) { this.writer = writer; };

    public void newline() {
        if (debugWriter) {
            writer.printf("nop");
        }
        writer.printf("\n");
        wasNewline = true;
    }

    public OutputWriter leadNewline() {
        if (!wasNewline) {
            newline();
        }
        return this;
    }

    @Override
    public void close() {
        wasNewline = false;
        leadNewline();
        level -= 1;
    }






}
