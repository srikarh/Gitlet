package gitlet;

import java.io.File;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Srikar Hanumanula
 */
public class Main {
    /** Gitlet Directory. */
    private static final File GITLED_DIR = new File(".gitlet");
    /** Gitlet Object. */
    private static final File GITLET_OBJ = new File(".gitlet/gitlet");

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String command = args[0];
        Gitlet gitlet = null;
        if (GITLED_DIR.exists()) {
            if (command.equals("init")) {
                System.out.println("A Gitlet version-control system "
                        + "already exists in the current directory.");
                System.exit(0);
            }
            gitlet = Utils.readObject(GITLET_OBJ, Gitlet.class);
        } else if (!command.equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        } else if (command.equals("init")) {
            GITLED_DIR.mkdir();
        }
        int numArgs = args.length;
        if (command.equals("init")) {
            checkArgs(1, numArgs);
            gitlet = new Gitlet();
        } else {
            cases(command, numArgs, gitlet, args);
        }
        Utils.writeObject(GITLET_OBJ, gitlet);
    }

    public static void cases(String command, int numArgs,
                             Gitlet gitlet, String[] args) {
        switch (command) {
        case "add":
            checkArgs(2, numArgs);
            gitlet.add(args[1]);
            break;
        case "commit":
            checkArgs(2, numArgs);
            gitlet.commit(args[1]);
            break;
        case "rm":
            checkArgs(2, numArgs);
            gitlet.rm(args[1]);
            break;
        case "log":
            checkArgs(1, numArgs);
            gitlet.log();
            break;
        case "checkout":
            if (args.length > 2) {
                gitlet.checkoutFile(args);
            } else {
                checkArgs(2, numArgs);
                gitlet.checkoutBranch(args[1]);
            }
            break;
        case "global-log":
            checkArgs(1, numArgs);
            gitlet.globalLog();
            break;
        case "find":
            checkArgs(2, numArgs);
            gitlet.find(args[1]);
            break;
        case "status":
            checkArgs(1, numArgs);
            gitlet.status();
            break;
        case "branch":
            checkArgs(2, numArgs);
            gitlet.branch(args[1]);
            break;
        case "rm-branch":
            checkArgs(2, numArgs);
            gitlet.rmBranch(args[1]);
            break;
        case "reset":
            checkArgs(2, numArgs);
            gitlet.reset(args[1]);
            break;
        case "merge":
            checkArgs(2, numArgs);
            gitlet.merge(args[1]);
            break;
        default:
            more(command, numArgs, gitlet, args);
            break;
        }
    }

    public static void more(String command, int numArgs,
                            Gitlet gitlet, String[] args) {
        switch (command) {
        case "add-remote":
            gitlet.addRemote(args[1], args[2]);
            break;
        case "rm-remote":
            gitlet.rmRemote(args[1]);
            break;
        case "push":
            gitlet.push(args[1], args[2]);
            break;
        case "fetch":
            gitlet.fetch(args[1], args[2]);
            break;
        case "pull":
            gitlet.pull(args[1], args[2]);
            break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }

    }

    public static void checkArgs(int n, int s) {
        if (n != s) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
