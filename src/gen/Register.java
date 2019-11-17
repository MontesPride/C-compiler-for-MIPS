package gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author sfilipiak
 */
public class Register implements AutoCloseable {

    /*
     * definition of registers
     */

    public static final Register v0 = new Register(2,"v0");
    public static final Register[] paramRegs = {
            new Register(4,"a0"),
            new Register(5,"a1"),
            new Register(6,"a2"),
            new Register(7,"a3")};

    public static final List<Register> tmpRegs = new ArrayList<Register>();
    static {
        for (int i=8; i<=15; i++)
            tmpRegs.add(new Register(i,"t"+(i-8)));
        for (int i=16; i<=23; i++)
            tmpRegs.add(new Register(i,"s"+(i-16)));
        for (int i=24; i<=25; i++)
            tmpRegs.add(new Register(i,"t"+(i-24+8)));
    }

    public static final Register gp = new Register(28,"gp");
    public static final Register sp = new Register(29,"sp");
    public static final Register fp = new Register(30,"fp");
    public static final Register ra = new Register(31,"ra");

    public static final Register[] unfreeable = {
            // syscall register
            v0,

            // argument registers
            paramRegs[0], paramRegs[1], paramRegs[2], paramRegs[3],

            // pointers and return address
            gp, sp, fp, ra,
    };

    public static final List<Register> savedRegisters = new ArrayList<>();
    static {
        // Add all temporaries and unfreeable
        savedRegisters.addAll(tmpRegs);
        savedRegisters.addAll(Arrays.asList(unfreeable));

        // Remove stack pointer though
        savedRegisters.remove(sp);

        // And remove syscall
        savedRegisters.remove(v0);
    }

    private final int num;      // register number
    private final String name;  // register name


    private Register(int num, String name) {
        this.num = num;
        this.name = name;
    }

    public String toString() {
        return "$"+name;
    }

    public void free() {
        for (Register r : unfreeable) {
            if (r == this) {
                throw new RuntimeException("Attempted to free an unfreeable register!");
            }
        }
        Helper.registers.free(this);
    }

    // Allows try-with-resources to be used to free registers
    @Override
    public void close() {
        free();
    }

    public void loadImmediate(int i) { Helper.writer.li(this, i); }

    public void loadAddress(String label) { Helper.writer.la(this, label); }

    public void loadByte(Register fromAddress, int offset) { Helper.writer.lb(this, fromAddress, offset); }

    public void loadWord(Register fromAddress, int offset) { Helper.writer.lw(this, fromAddress, offset); }

    public void storeByteAt(Register toAddress, int offset) { Helper.writer.sb(this, toAddress, offset); }

    public void storeWordAt(Register toAddress, int offset) { Helper.writer.sw(this, toAddress, offset); }

    public void set(Register fromAddress) { Helper.writer.move(this, fromAddress); }

    public void moveTo(Register toAddress) { Helper.writer.move(toAddress, this); }

    public void jump() { Helper.writer.jr(this); }

    public void add(int i) { Helper.writer.add(this, this, i); }

    public void add(Register register) { Helper.writer.add(this, this, register); }

    public void sub(int i) { Helper.writer.sub(this, this, i); }

    public void sub(Register register) { Helper.writer.sub(this, this, register); }

    public void mul(int multiplier) { Helper.writer.mul(this, this, multiplier); }

}
