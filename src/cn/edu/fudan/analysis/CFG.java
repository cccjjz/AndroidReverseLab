package cn.edu.fudan.analysis;

import org.jf.dexlib2.dexbacked.instruction.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.SwitchElement;

import java.util.*;


public class CFG {

    /**
     * The method that the control flow graph is built on
     */
    private Method targetMethod = null;

    /**
     * All the basic blocks EXCEPT catch blocks.
     */
    private HashSet<BasicBlock> blocks = new HashSet<BasicBlock>();

    /**
     * Entry block of this method
     * */
    private BasicBlock entryBB;

    private CFG(){}

    public static String classType2Name(String s) {
        if (s == null) return "";

        if (s.startsWith("L"))
            s = s.substring(1);

        String res = s.replace(";", "").replace("$", "~").replace("/",".");
        return res;
    }

    public static String methodSignature2Name(Method m) {
        String temp = m.getName() + "(";
        List<? extends CharSequence> parameters = m.getParameterTypes();
        List<String> params = new ArrayList<>();
        for (CharSequence p : parameters) {
            String param = p.toString();
            String suffix = "";
            if (param.startsWith("["))
            {
                suffix = "[]";
                param = param.substring(1);
            }
            switch (param)
            {
                case "B":
                    params.add("byte" + suffix);
                    break;
                case "C":
                    params.add("char" + suffix);
                    break;
                case "D":
                    params.add("double" + suffix);
                    break;
                case "F":
                    params.add("float" + suffix);
                    break;
                case "I":
                    params.add("int" + suffix);
                    break;
                case "J":
                    params.add("long" + suffix);
                    break;
                case "S":
                    params.add("short" + suffix);
                    break;
                case "V":
                    params.add("void" + suffix);
                    break;
                case "Z":
                    params.add("boolean" + suffix);
                    break;
                default:
                    String tmp = classType2Name(param);

                    if (tmp.contains("~"))
                        tmp = tmp.substring(tmp.lastIndexOf('~') + 1);

                    params.add(tmp + suffix);
                    break;
            }
        }

        temp += String.join(",", params);
        temp += ")";
        return temp;
    }

    public static CFG createCFG(Method method) {
        CFG cfg = new CFG();
        cfg.targetMethod = method;

        Iterable<? extends Instruction> instructions = cfg.targetMethod.getImplementation().getInstructions();

        for (Instruction i : instructions) {
            DexBackedInstruction dbi = (DexBackedInstruction) i;

            int offset = 0;
            switch (dbi.opcode) {
                case GOTO:
                    offset = ((DexBackedInstruction10t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", offset: " + offset);
                    break;
                case GOTO_16:
                    offset = ((DexBackedInstruction20t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", offset: " + offset);
                    break;
                case GOTO_32:
                    offset = ((DexBackedInstruction30t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", offset: " + offset);
                    break;
                case IF_EQ:
                case IF_NE:
                case IF_LT:
                case IF_GE:
                case IF_GT:
                case IF_LE:
                    offset = dbi.instructionStart + dbi.getOpcode().format.size;
                    System.out.println(dbi.getOpcode() + ", offset1: " + offset);
                    offset = ((DexBackedInstruction22t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", offset2: " + offset);
                    break;
                case IF_EQZ:
                case IF_NEZ:
                case IF_LTZ:
                case IF_GEZ:
                case IF_GTZ:
                case IF_LEZ:
                    offset = dbi.instructionStart + dbi.getOpcode().format.size;
                    System.out.println(dbi.getOpcode() + ", offset1: " + offset);
                    offset = ((DexBackedInstruction21t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", offset2: " + offset);
                    break;
                case PACKED_SWITCH:
                case SPARSE_SWITCH:
                case FILL_ARRAY_DATA:
                    offset = ((DexBackedInstruction31t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", switch payload offset: " + offset);
                    break;
                case PACKED_SWITCH_PAYLOAD:
                case SPARSE_SWITCH_PAYLOAD:
                    // Since switch-payloads actually are just data, not an instruction.
                    // Though dexlib treat them as special instructions
                    // (sparse-switch-payload & packed-switch-payload),
                    // we should not include them in our CFG.

                    // Take the following switch instruction for example.
                    // 0xAA switch : switch_payload_0
                    // ...
                    // 0xBB switch_payload_0:
                    // 0x1-> 20(offset)
                    // 0x6-> 50(offset)
                    // The offset in a payload instruction points to the instruction
                    // whose address is relative to the address of the switch opcode(0xAA),
                    // not of this table(0xBB).
                    List<? extends SwitchElement> switchElements = null;
                    if (dbi instanceof DexBackedPackedSwitchPayload)
                        switchElements = ((DexBackedPackedSwitchPayload) dbi).getSwitchElements();
                    else
                        switchElements = ((DexBackedSparseSwitchPayload) dbi).getSwitchElements();

                    for (SwitchElement s : switchElements) {
                        /*
                         * !!! Important:
                         * According to sparse-switch-payload Format :
                         * The targets are relative to the address of the switch opcode, not of this table.
                         */

                        System.out.println(dbi.getOpcode() + ", offset: " + s.getOffset());
                    }
                    break;
            }
//            linkBlock(cfg, cfg.getEntryBB(), offset);
        }

        return cfg;
    }

    /**
     * link an edge from BasicBlock (bb) to a BasicBlock started at offset
     * */
    private static void linkBlock(CFG cfg, BasicBlock bb, int offset) {
        for (BasicBlock basicBlock : cfg.blocks) {
            if (basicBlock.getStartAddress() == offset) {
                bb.addSuccessor(basicBlock);
                return;
            }
        }
        // Typically, no exception will be thrown.
        throw new RuntimeException("no basic block found at offset: " + offset);
    }

    public BasicBlock getEntryBB() {return entryBB;}

    public Method getTargetMethod(){
        return this.targetMethod;
    }

    public HashSet<BasicBlock> getBasicBlocks() {
        return blocks;
    }
}
