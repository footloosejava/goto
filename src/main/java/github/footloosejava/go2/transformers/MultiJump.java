/*
 * MultiJump.java
 *
 * Created on June 30, 2007, 2:55 PM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 */
package github.footloosejava.go2.transformers;

import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * @author footloosejava
 */
public class MultiJump {

    public static final boolean debug = false;
    public static final boolean debug_fetch = debug && false;

    static void makeMultiJump(
        final List<AbstractInsnNode> jumps,
        final MethodInsnNode min,
        final String workingClassName,
        final InsnList ins,
        final AbstractInsnNode METHOD_ROOT_NODE) {

        // if(true) return;

        if (debug) {
            System.out.println("Found multi jump!");
        }

        AbstractInsnNode pNode = min;
        LinkedList<Integer> stack = new LinkedList<>();

        while (true) {

            int priorOpcode = pNode.getOpcode();
            if (debug_fetch) {
                System.out.println("Prior Opcode = " + priorOpcode);
            }

            pNode = (AbstractInsnNode) pNode.getPrevious();

            int opc = pNode.getOpcode();
            if (debug_fetch) {
                System.out.println("Working Opcode=" + opc);
            }

            // get Labels!
            if (priorOpcode == Op.Op_iaStore // IASTORE
                && (opc == Op.Op_biPush || opc == Op.Op_siPush || (opc >= Op.Op_iConst_0 && opc <= Op.Op_iConst_5))
                && (pNode.getType() == AbstractInsnNode.INT_INSN || pNode.getType() == AbstractInsnNode.INSN)) {

                int value = Op.getOperand(workingClassName, pNode);
                if (debug) {
                    System.out.println(pNode.getOpcode() + " - INT VALUE =" + value);
                }
                stack.add(value);
            }

            if (pNode.getType() == AbstractInsnNode.INT_INSN && pNode.getOpcode() == Op.Op_newArray) {

                if (debug) {
                    System.out.println("Stack=" + Arrays.toString(stack.toArray()));
                }
                if (debug) {
                    System.out.println("Found array Start");
                }

                AbstractInsnNode sizeNode = (AbstractInsnNode) pNode.getPrevious();
                if (debug) {
                    System.out.println("Size Node: " + sizeNode);
                }

                int size = Op.getOperand(workingClassName, pNode.getPrevious());
                stack.add(size);
                if (debug) {
                    System.out.println("ArraySize=" + size);
                }

                AbstractInsnNode varRegisterNode = (AbstractInsnNode) sizeNode.getPrevious();
                if (debug) {
                    System.out.println("Var Regsiter Node: " + varRegisterNode);
                }

                int register;
                int varRegOpcode = varRegisterNode.getOpcode();
                if (debug) {
                    System.out.println("LOAD OPCODE =" + varRegOpcode);
                }
                stack.add(varRegOpcode);

                if (varRegisterNode.getType() == AbstractInsnNode.VAR_INSN) {
                    VarInsnNode vin = (VarInsnNode) varRegisterNode;
                    register = vin.var;
                } else {
                    register = -1 * Op.getOperand(workingClassName, varRegisterNode);
                }

                if (debug) {
                    System.out.println("Operand Register: " + register);
                }
                stack.add(register);

                // done finding
                break;
            }

        }


        // If we have stuff on stack to process
        if (stack.size() > 0) {
            if (debug) {
                System.out.println("Making ArrayCode!");
                System.out.println("Stack=" + Arrays.toString(stack.toArray()));
            }

            int store = stack.removeLast();
            if (debug) {
                System.out.println("Store: " + store);
            }

            int loadCode = stack.removeLast();
            if (debug) {
                System.out.println("LoadCode: " + loadCode);
            }

            int size = stack.removeLast();
            if (debug) {
                System.out.println("Size: " + size);
            }

            if (size >= 0) {
                if (debug) {
                    System.out.println("Value of J is in register: " + store);
                }

                // build inssn
                InsnList items = new InsnList();

                LabelNode endIFLabel = new LabelNode();
                LabelNode nextIfLabel = null;

                // element compared
                int i = 0;

                do {

                    if (nextIfLabel != null) {
                        items.add(nextIfLabel);
                        if (debug) {
                            System.out.println("Added nextIf target label.");
                        }
                    }

                    if (debug) {
                        System.out.println("i=" + i);
                    }

                    int arg = stack.removeLast();
                    if (debug) {
                        System.out.println("ARG:" + arg);
                    }

                    if (stack.size() == 0) {
                        nextIfLabel = endIFLabel; // put at end of this if
                    } else {
                        nextIfLabel = new LabelNode(); // put at end of this if
                    }


                    // load variable ref for compare
                    items.add(new VarInsnNode(loadCode, store));

                    //
                    if (i == 0) {
                        // do no insert
                        if (debug) {
                            System.out.println("No ref insert for if here...");
                        }
                    } else if (i > 0 && i <= 5) {
                        // insert short style
                        items.add(new InsnNode(Op.Op_iConst_0 + i));
                        if (debug) {
                            System.out.println("Ref inserted for if = " + (Op.Op_iConst_0 + i));
                        }

                    } else {
                        // insert long style
                        items.add(new IntInsnNode(Op.Op_siPush, i));
                        if (debug) {
                            System.out.println("siPush Ref inserted for if = " + i);
                        }
                    }


                    // use ifne as first since compare to zero
                    if (i == 0) {
                        items.add(new JumpInsnNode(Op.Op_ifne, nextIfLabel));
                    } else {
                        items.add(new JumpInsnNode(Op.Op_if_icmpne, nextIfLabel)); // zero
                    }

                    items.add(new VarInsnNode(Op.Op_aLoad, 0));

                    // array element address
                    if (arg >= 0 && arg <= 5) {
                        arg = Op.Op_iConst_0 + arg;
                        if (debug) {
                            System.out.println("Used QUICK CODE for address " + arg + ",code " + arg);
                        }
                        items.add(new InsnNode(arg));
                    } else {
                        items.add(new IntInsnNode(Op.Op_siPush, arg));
                    }

                    items.add(new MethodInsnNode(Op.Op_invokevirtual, workingClassName, "_goto", "(I)V", false));

                    if (!stack.isEmpty()) {
                        items.add(new JumpInsnNode(Op.Op_goto, endIFLabel));
                    }

                    i++; // incr stack
                } while (!stack.isEmpty());

                items.add(endIFLabel);
                ins.insert(METHOD_ROOT_NODE, items);
            }

            /*
             mv.visitVarInsn(ILOAD, 4);
             Label l12 = new Label();
             mv.visitJumpInsn(IFNE, l12);  // if new next iflabel
             mv.visitVarInsn(ALOAD, 0);
             mv.visitIntInsn(SIPUSH, 555);
             mv.visitMethodInsn(INVOKEVIRTUAL, "wm/gotos/examples/TestImpl", "jump", "(I)V");
             Label l13 = new Label();
             mv.visitJumpInsn(GOTO, l13);                                         *
             mv.visitLabel(l12); // next if label
             *
             mv.visitVarInsn(ILOAD, 4);
             mv.visitInsn(ICONST_1);
             Label l14 = new Label();
             mv.visitJumpInsn(IF_ICMPNE, l14);
             mv.visitVarInsn(ALOAD, 0);
             mv.visitIntInsn(SIPUSH, 444);
             mv.visitMethodInsn(INVOKEVIRTUAL, "wm/gotos/examples/TestImpl", "jump", "(I)V");
             mv.visitJumpInsn(GOTO, l13);
             mv.visitLabel(l14);
             */
        }
    }
}