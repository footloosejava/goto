/*
 * NodeTransform.java
 *
 * Created on June 27, 2007, 7:38 PM
 * Copyright footloosejava.
 * Email: footloosejava@gmail.com
 */
package github.footloosejava.go2.transformers;

import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * @author footloosejava
 */
public class NodeTransform {

    static final boolean debug = false;

    static final String MARK_JUMP = "_goto";
    static final String MARK_LABEL = "_label";

    public synchronized static boolean makeChanges(final ClassNode cn) throws LabelNotFound, DuplicateLabel {


        // we will put all nodes found where markers for labels and nodes are present
        final List<AbstractInsnNode> labels = new ArrayList<>();
        final List<AbstractInsnNode> jumps = new ArrayList<>();

        final Map<Integer, LabelNode> labelRefs = new HashMap<>();

        boolean hasJumps = false;

        for (Object o : cn.methods) {

            MethodNode mn = (MethodNode) o;


            extractJumpData(labels, cn, jumps, mn);
            // process

            // do we have any go2 to process???
            // if not, exit routine and ignore class
            if (jumps.size() > 0) {
                hasJumps = true;

                if (debug) {
                    System.out.println("************ Process Jumps *************");
                }

                insertLabelNodes(labelRefs, labels, mn, cn);

                // connect to which lables!
                makeJumpsToLabels(jumps, mn, labelRefs, cn);
            }

            jumps.clear();
            labels.clear();
            labelRefs.clear();

        }
        return hasJumps;
    }

    private static void makeJumpsToLabels(final List<AbstractInsnNode> jumps,
                                          final MethodNode mn, final Map<Integer, LabelNode> myRefs,
                                          final ClassNode cn) throws LabelNotFound {


        // join labels to jump data
        if (debug) {
            System.out.println("Inserting Jump Sequences:");
        }

        for (AbstractInsnNode node : jumps) {
            AbstractInsnNode pNode = node.getPrevious();


            int labelNumber = Op.getOperand(cn.name, pNode);

            if (debug) {
                System.out.println("\tGoto target: " + labelNumber);
            }

            if (!myRefs.containsKey(labelNumber)) {
                throw new LabelNotFound("Class: " + cn.name + ".  Label not found for goto (jump) to: " + labelNumber);
            }

            LabelNode labelNode = myRefs.get(labelNumber);

            if (labelNode != null) {
                JumpInsnNode jumpNode = new JumpInsnNode(Op.Op_goto, labelNode);
                mn.instructions.insert(node, jumpNode);
            }
        }
    }

    private static void insertLabelNodes(
        final Map<Integer, LabelNode> labelRefs,
        final List<AbstractInsnNode> labels,
        final MethodNode mn,
        final ClassNode cn)
        throws DuplicateLabel {


        // insert labels
        if (debug) {
            System.out.println("Inserting label refs.");
        }

        // map to keep all labelNode objects inserted before label refs in.
        labelRefs.clear();

        for (AbstractInsnNode node : labels) {
            AbstractInsnNode operandNode = (AbstractInsnNode) node.getPrevious();
            int labelNumber = Op.getOperand(cn.name, operandNode);

            if (debug) {
                System.out.println("\tAdded labelNumber: " + labelNumber);
            }


            if (labelRefs.containsKey(labelNumber)) {
                throw new DuplicateLabel("Class " + cn.name + " has duplicate "
                    + "line numbers labeled: " + labelNumber);
            }


            AbstractInsnNode loadANode = operandNode.getPrevious(); // we need to back up one more to before the push instruction

            LabelNode labelNode = new LabelNode();
            labelRefs.put(labelNumber, labelNode);

            if (debug) {
                System.out.println("NODE BEFORE LABEL INSERT: " + loadANode + "," + loadANode.getType() + "," + loadANode.getOpcode());
            }
            mn.instructions.insert(loadANode.getPrevious(), labelNode);
            // mn.instructions.insertBefore(labelNode,new JumpInsnNode(167,labelNode));
        }
    }

    private static void extractJumpData(final List<AbstractInsnNode> labels, final ClassNode cn, final List<AbstractInsnNode> jumps, final MethodNode mn) {

        final String workingClassName = cn.name.replace('.', '/');

        InsnList ins = mn.instructions;
        if (debug) {
            System.out.println("Now trying to find multiJump...");
        }

        ListIterator iterator = mn.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode WHILE_NODE = (AbstractInsnNode) iterator.next();

            //if(debug) System.out.println("Look - Node: " + WHILE_NODE);

            if (WHILE_NODE.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode min = (MethodInsnNode) WHILE_NODE;

                if (debug) {
                    System.out.println("Desc: " + min.desc);
                }

                if (min.owner.equals(workingClassName)) {
                    if (debug) {
                        System.out.println(" > owner: " + min.owner + ". name: " + min.name + " ");
                    }
                    if (min.name.equals("_multiGoto")) {
                        // wow!
                        MultiJump.makeMultiJump(jumps, min, workingClassName, ins, WHILE_NODE);
                    }
                }
            }
        }

        if (debug) {
            System.out.println("Now trying to find jump and label...");
        }

        iterator = mn.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode WHILE_NODE = (AbstractInsnNode) iterator.next();

            // if(debug) System.out.println("Look - Node: " + WHILE_NODE);


            if (WHILE_NODE.getType() == AbstractInsnNode.METHOD_INSN) {

                MethodInsnNode min = (MethodInsnNode) WHILE_NODE;

                if (min.owner.equals(workingClassName)) {
                    if (debug) {
                        System.out.println("Desc: " + min.desc
                            + " > owner: " + min.owner + ". name: " + min.name + " ");
                    }

                    if (min.name.equals(MARK_JUMP)) {

                        jumps.add(min);

                        AbstractInsnNode pNode = (AbstractInsnNode) min.getPrevious();
                        int labelNumber = Op.getOperand(cn.name, pNode);
                        if (debug) {
                            System.out.println("******* FOUND JUMP : " + labelNumber);
                        }
                        Op.qualifyLabelNumber(labelNumber, cn.name);

                    } else if (min.name.equals(MARK_LABEL)) {

                        labels.add(min);
                        AbstractInsnNode pNode = (AbstractInsnNode) min.getPrevious();

                        // if(debug)System.out.println("\tpNode: " + pNode );
                        // if(debug)System.out.println("\tpNode Opcode: " + pNode.getOpcode());

                        int labelNumber = Op.getOperand(cn.name, pNode);
                        Op.qualifyLabelNumber(labelNumber, cn.name);
                        if (debug) {
                            System.out.println("******* FOUND LABEL: " + labelNumber);
                        }
                    }
                }
            }
        }
    }
}
