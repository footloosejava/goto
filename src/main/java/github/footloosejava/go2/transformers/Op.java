/*
 * Op.java
 *
 * Created on June 30, 2007, 2:56 PM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 */

package github.footloosejava.go2.transformers;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author footloosejava
 */
public class Op {

    static final boolean debug = false;
    static final String name = Op.class.getName();

    public static final int Op_iConst_0 = 3;
    public static final int Op_iConst_5 = 8;
    public static final int Op_biPush = 16;
    public static final int Op_siPush = 17;
    public static final int Op_aLoad = 25;
    public static final int Op_iaStore = 79;
    public static final int Op_ifne = 154; // if ne zero
    public static final int Op_if_icmpne = 160;
    public static final int Op_goto = 167;
    public static final int Op_invokevirtual = 182;
    public static final int Op_newArray = 188;


    /**
     * goto a7 branchbyte1, branchbyte2 [no change] goes to another instruction at branchoffset (signed short constructed from unsigned bytes branchbyte1 << 8 + branchbyte2)
     * goto_w c8 branchbyte1, branchbyte2, branchbyte3, branchbyte4 [no change] goes to another instruction at branchoffset (signed int constructed from unsigned bytes branchbyte1 << 24 + branchbyte2 << 16 + branchbyte3 << 8 + branchbyte4)
     */


    static int getOperand(String clazz, final AbstractInsnNode node) {

        int labelNumber = -1;

        if (debug) System.out.println(name + " > Node to be evaluated in switch is: " + node);

        if (node.getType() == AbstractInsnNode.VAR_INSN) {
            VarInsnNode vis = (VarInsnNode) node;
            int opvis = vis.getOpcode();

            System.out.println(name + " > vis opcode=" + opvis);
        }

        if (node.getType() == AbstractInsnNode.INT_INSN) {

            IntInsnNode iin = (IntInsnNode) node;
            labelNumber = iin.operand;

        } else if (node.getType() == AbstractInsnNode.INSN) {


            InsnNode insn = (InsnNode) node;
            // System.out.println("INSN NODE: " + insn);
            // System.out.print("\t");

            int opcode = insn.getOpcode();

            if (debug) System.out.println(name + " > Opcode for jump is: " + opcode);
            switch (opcode) {
//                case 2:
//                    labelNumber = -1;
//                    break;
                case 3:
                    labelNumber = 0;
                    break;

                case 4:
                    labelNumber = 1;
                    break;

                case 5:
                    labelNumber = 2;
                    break;

                case 6:
                    labelNumber = 3;
                    break;
                case 7:
                    labelNumber = 4;
                    break;
                case 8:
                    labelNumber = 5;
                    break;
            }
        }
        return labelNumber;
    }


    static void qualifyLabelNumber(final int labelNumber, String clazz) throws IllegalArgumentException {
        if (labelNumber < 0) {
            throw new IllegalArgumentException("In class: " + clazz
                + " > label=" + labelNumber + ". Can not do jump to a negative label number in code!");
        }
    }
}
