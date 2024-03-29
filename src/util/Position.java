package util;


/**
 * @author sfilipiak
 */
public class Position {

    final int line;
    final int column;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return line+":"+column;
    }

}
