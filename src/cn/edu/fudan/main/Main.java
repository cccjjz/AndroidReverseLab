package cn.edu.fudan.main;

import cn.edu.fudan.analysis.CFG;
import cn.edu.fudan.util.Visualization;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class Main {

    public static class Args {
        @Parameter(names = "-apk", description = "Path of target apk")
        public String apkPath;

        @Parameter(names = "-class", description = "Name of target class")
        public String className;

        @Parameter(names = "-method", description = "Name of target method")
        public String methodName;
    }

    private static boolean stringIsNone(String string) {
        return string == null || string.length() == 0;
    }

    public static void main(String[] a) throws Exception{
        Args args = new Args();
        JCommander jCommander = new JCommander(args);
        if (a == null || a.length == 0) {
            jCommander.usage();
            System.exit(-1);
        }
        jCommander.parse(a);

        /* apkPath is a mandatory parameter*/
        if (stringIsNone(args.apkPath)) {
            System.err.println("Miss parameter: apkPath, which is mandatory.");
            jCommander.usage();
            System.exit(-1);
        }

        /* Load apk*/
        File dexFile = new File(args.apkPath);
        if (! dexFile.exists()) {
            throw new RuntimeException("Apk File Not Exists: " + args.apkPath);
        }

        /* className is an optional parameter, if it is not set, dump all classes in the apk*/
        if (stringIsNone(args.className)) {
            System.out.println("className is not given, dump all class names.");
            dumpAllClassNames(dexFile);
            System.exit(0);
        }
        else {
            Set<? extends DexBackedClassDef> classDefSet = DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault()).getClasses();
            ClassDef targetClassDef = null;

            // locate target class
            for (ClassDef classDef : classDefSet) {
                if (CFG.classType2Name(classDef.getType()).equals(args.className)) {
                    targetClassDef = classDef;
                    break;
                }
            }
            // class not located
            if (targetClassDef == null) {
                System.err.println("Target Class Not Exists: " + args.className);
                dumpAllClassNames(dexFile);
                System.exit(-1);
            }

            /* methodName is an optional parameter, if it is not set, dump all methods in the class*/
            if (stringIsNone(args.methodName)) {
                System.out.println("methodName is not given, dump all method names.");
                dumpAllMethodNames(targetClassDef);
                System.exit(0);
            }
            else {
                Method targetMethod = null;

                // locate target method
                for (Method method : targetClassDef.getMethods()) {
                    MethodImplementation impl = method.getImplementation();
                    if (impl != null) {
                        String sig = CFG.methodSignature2Name(method);
                        if(sig.equals(args.methodName)){
                            targetMethod = method;
                            break;
                        }
                    }
                }
                // method not located
                if(targetMethod == null){
                    System.err.println("methodName is not given, dump all method names.");
                    dumpAllMethodNames(targetClassDef);
                    System.exit(-1);
                }

                //draw the CFG
                drawCFG4Method(targetMethod, args.methodName);
            }

        }
    }

    private static String getDotFileNameFromDate() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return format.format(date)+".dot";
    }

    private static void dumpAllClassNames(File dexFile) {
        try {
            Set<? extends DexBackedClassDef> classDefSet = DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault()).getClasses();

            for (ClassDef classDef : classDefSet)
                System.out.println(CFG.classType2Name(classDef.getType()));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void dumpAllMethodNames(ClassDef classDef) {
        for (Method method : classDef.getMethods())
            if (method.getImplementation() != null)
                System.out.println(CFG.methodSignature2Name(method));
    }

    private static void drawCFG4Method(Method method, String methodName) throws IOException {
        CFG cfg = CFG.createCFG(method);
        String dotFileName = getDotFileNameFromDate();

        System.out.println("Create CFG for method: " + methodName + " into dot file: " + dotFileName);
        Visualization.visualizeCFG(cfg, new PrintWriter("./" + dotFileName));

        String svgFileName = dotFileName.replace(".dot", ".svg");
        System.out.println("Create CFG for method: " + methodName + " into svg file: " + svgFileName);
        InputStream dot = new FileInputStream(new File("./" + dotFileName));
        MutableGraph g = new Parser().read(dot);
        Graphviz.fromGraph(g).render(Format.SVG).toFile(new File("./" + svgFileName));
    }
}
