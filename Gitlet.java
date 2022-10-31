package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;


public class Gitlet implements Serializable {
    /**
     * HEAD of current branch.
     */
    private Commit _HEAD;
    /**
     * Branches.
     */
    private TreeMap<String, String> branches;
    /**
     * Staged files.
     */
    private TreeMap<String, String> staged;
    /**
     * Current Branch.
     */
    private String currentBranch;
    /**
     * Gitlet Directory.
     */
    public static final String GITLET_DIR = ".gitlet/";
    /**
     * Remotes.
     */
    private TreeMap<String, String> remotes;


    public Gitlet() {
        branches = new TreeMap<>();
        staged = new TreeMap<>();
        remotes = new TreeMap<>();
        _HEAD = new Commit("initial commit", null);
        File file = new File(GITLET_DIR + _HEAD.getHash());
        Utils.writeObject(file, _HEAD);
        currentBranch = "master";
        branches.put(currentBranch, _HEAD.getHash());
    }


    public void add(String arg) {
        byte[] contents = null;
        try {
            contents = Utils.readContents(new File(arg));
        } catch (IllegalArgumentException e) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String hash = Utils.sha1(contents);
        if (_HEAD.getBlobs().containsKey(arg)
                && _HEAD.getBlobs().get(arg).equals(hash)) {
            staged.remove(arg);
            return;
        }
        File dest = new File(GITLET_DIR + hash);
        staged.put(arg, hash);
        Utils.writeContents(dest, contents);
    }

    public void commit(String message) {
        if (staged.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
        }
        Commit newCommit = new Commit(message, _HEAD.getHash());
        _HEAD = newCommit;
        TreeMap<String, String> newBlobs = newCommit.getBlobs();
        for (Map.Entry<String, String> entry : staged.entrySet()) {
            if (entry.getValue() == null) {
                newBlobs.remove(entry.getKey());
            } else {
                newBlobs.put(entry.getKey(), entry.getValue());
            }
        }
        staged.clear();
        branches.put(currentBranch, _HEAD.getHash());
        File file = new File(GITLET_DIR + newCommit.getHash());
        Utils.writeObject(file, newCommit);
    }

    public void commitMerge(String message, String parent2) {
        Commit newCommit = new Commit(message, _HEAD.getHash(), parent2);
        newCommit.setParent2(branches.get(parent2));
        _HEAD = newCommit;
        TreeMap<String, String> newBlobs = newCommit.getBlobs();
        for (Map.Entry<String, String> entry : staged.entrySet()) {
            if (entry.getValue() == null) {
                newBlobs.remove(entry.getKey());
            } else {
                newBlobs.put(entry.getKey(), entry.getValue());
            }
        }
        staged.clear();
        branches.put(currentBranch, _HEAD.getHash());
        File file = new File(GITLET_DIR + newCommit.getHash());
        Utils.writeObject(file, newCommit);
    }

    public void rm(String arg) {
        boolean removed = false;
        if (staged.containsKey(arg)) {
            staged.remove(arg);
            removed = true;
        }
        if (_HEAD.getBlobs().containsKey(arg)) {
            staged.put(arg, null);
            removed = true;
            Utils.restrictedDelete(arg);
        }
        if (!removed) {
            System.out.println("No reason to remove the file.");
        }
    }

    public void log() {
        Commit trace = _HEAD;
        while (trace != null) {
            System.out.println("===");
            System.out.println("commit " + trace.getHash());
            System.out.println("Date: " + trace.getTimestamp());
            System.out.println(trace.getMessage());
            System.out.println();
            trace = Commit.getCommit(trace.getParent());
        }
    }

    public void checkoutFile(String[] args) {
        String fileName;
        Commit commit;
        if (args[1].equals("--")) {
            Main.checkArgs(3, args.length);
            fileName = args[2];
            commit = _HEAD;
        } else {
            Main.checkArgs(4, args.length);
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
            }
            commit = Commit.getCommit(args[1]);
            fileName = args[3];
        }
        if (!commit.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File currentFile = new File(fileName);
        File replacement = new File(GITLET_DIR
                + commit.getBlobs().get(fileName));
        byte[] contents = Utils.readContents(replacement);
        Utils.writeContents(currentFile, contents);
    }

    public void checkoutBranch(String name) {
        if (!branches.containsKey(name)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (currentBranch.equals(name)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        currentBranch = name;
        staged.clear();
        Commit newHEAD = _HEAD;
        if (!branches.get(name).equals(_HEAD.getHash())) {
            newHEAD = Commit.getCommit(branches.get(name));
        }
        untrackedCheck(newHEAD);
        _HEAD = newHEAD;
        for (String fileName : Utils.plainFilenamesIn("./")) {
            if (!_HEAD.getBlobs().containsKey(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }
        for (String file : _HEAD.getBlobs().keySet()) {
            byte[] contents = Utils.readContents(
                    new File(GITLET_DIR + _HEAD.getBlobs().get(file)));
            Utils.writeContents(new File(file), contents);
        }
    }

    public void globalLog() {
        for (String filename : Utils.plainFilenamesIn(GITLET_DIR)) {
            Commit commit;
            try {
                commit = Commit.getCommit(filename);
            } catch (IllegalArgumentException e) {
                continue;
            }
            System.out.println("===");
            System.out.println("commit " + commit.getHash());
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    public void find(String message) {
        boolean found = false;
        for (String filename : Utils.plainFilenamesIn(GITLET_DIR)) {
            Commit commit;
            try {
                commit = Commit.getCommit(filename);
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (commit.getMessage().equals(message)) {
                System.out.println(commit.getHash());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public void status() {
        System.out.println("=== Branches ===");
        for (String branch : branches.keySet()) {
            String selector = "";
            if (currentBranch.equals(branch)) {
                selector = "*";
            }
            System.out.println(selector + branch);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String file : staged.keySet()) {
            if (staged.get(file) != null) {
                System.out.println(file);
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String file : staged.keySet()) {
            if (staged.get(file) == null) {
                System.out.println(file);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : _HEAD.getBlobs().keySet()) {
            File current = new File(fileName);
            if (current.exists()) {
                File file = new File(GITLET_DIR
                        + _HEAD.getBlobs().get(fileName));
                Diff d = new Diff();
                d.setSequences(current, file);
                if (!d.sequencesEqual() && !staged.containsKey(fileName)) {
                    System.out.println(fileName + "(modified)");
                }
            } else {
                if (!staged.containsKey(fileName)) {
                    System.out.println(fileName + "(deleted)");
                }
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String fileName : Utils.plainFilenamesIn("./")) {
            if (!_HEAD.getBlobs().containsKey(fileName)
                    && !staged.containsKey(fileName)) {
                System.out.println(fileName);
            }
        }
    }

    public void branch(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        branches.put(branchName, _HEAD.getHash());
    }

    public void rmBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (currentBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        branches.remove(branchName);
    }

    public void reset(String commitID) {
        Commit reset = Commit.getCommit(commitID);
        untrackedCheck(reset);
        staged.clear();
        for (String fileName : _HEAD.getBlobs().keySet()) {
            if (!reset.getBlobs().containsKey(fileName)) {
                Utils.restrictedDelete(new File(fileName));
            }
        }
        for (String fileName : reset.getBlobs().keySet()) {
            String[] args = {"checkout", reset.getHash(), "--", fileName};
            checkoutFile(args);
        }
        _HEAD = reset;
        branches.put(currentBranch, _HEAD.getHash());
    }

    public void mergeChecks(String given) {
        if (staged.size() > 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!branches.containsKey(given)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (currentBranch.equals(given)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    public void merge(String given) {
        mergeChecks(given);
        LinkedList<String> givenCommits = bfs(branches.get(given));
        LinkedList<String> currentCommits = bfs(branches.get(currentBranch));
        String split = null;
        for (String commit : currentCommits) {
            if (givenCommits.contains(commit)) {
                split = commit;
                break;
            }
        }
        Commit splitCommit = Commit.getCommit(split);
        Commit currentCommit = Commit.getCommit(branches.get(currentBranch));
        Commit givenCommit = Commit.getCommit(branches.get(given));
        untrackedCheck(givenCommit);
        if (split.equals(branches.get(given))) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            System.exit(0);
        }
        if (split.equals(branches.get(currentBranch))) {
            checkoutBranch(given);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        HashSet<String> files = new HashSet<>();
        String[] givenFiles = givenCommit.getBlobs().keySet().toArray(
                new String[0]);
        String[] cwdFiles = Utils.plainFilenamesIn("./").toArray(
                new String[0]);
        Collections.addAll(files, givenFiles);
        Collections.addAll(files, cwdFiles);
        boolean conflict = mergeHelper(files, currentCommit, splitCommit,
                givenCommit, given, false);
        commitMerge("Merged " + given + " into " + currentBranch + ".", given);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private boolean mergeHelper(HashSet<String> files, Commit currentCommit,
                                Commit splitCommit,
                                Commit givenCommit,
                                String given, boolean conflict) {
        for (String fileName : files) {
            String currentFileName = currentCommit.getBlobs().get(fileName);
            String splitFileName = splitCommit.getBlobs().get(fileName);
            String givenFileName = givenCommit.getBlobs().get(fileName);
            File currentFile = null;
            File givenFile = null;
            if (currentFileName != null) {
                currentFile = new File(GITLET_DIR + currentFileName);
            }
            if (givenFileName != null) {
                givenFile = new File(GITLET_DIR + givenFileName);
            }

            File splitFile = new File(GITLET_DIR + splitFileName);
            Diff givenDiff = new Diff();
            givenDiff.setSequences(givenFile, splitFile);
            Diff currentDiff = new Diff();
            currentDiff.setSequences(currentFile, splitFile);
            Diff changed = new Diff();
            changed.setSequences(currentFile, givenFile);
            if (!givenDiff.sequencesEqual() && currentDiff.sequencesEqual()) {
                if (givenFile != null) {
                    String[] args = {"checkout",
                            branches.get(given), "--", fileName};
                    checkoutFile(args);
                    add(fileName);
                } else {
                    rm(fileName);
                }
            } else if (!givenDiff.sequencesEqual()
                    && !currentDiff.sequencesEqual()
                    && !changed.sequencesEqual()) {
                conflict = true;
                String currentContents = "";
                String givenContents = "";
                if (currentFile != null) {
                    currentContents = Utils.readContentsAsString(currentFile);
                }
                if (givenFile != null) {
                    givenContents = Utils.readContentsAsString(givenFile);
                }
                String contents = "<<<<<<< HEAD\n"
                        + currentContents + "=======\n"
                        + givenContents + ">>>>>>>\n";
                Utils.writeContents(new File(fileName), contents);
                add(fileName);
            }
        }
        return conflict;
    }

    private LinkedList<String> bfs(String s) {
        HashMap<String, Boolean> visited = new HashMap<>();
        visited.put(s, true);
        LinkedList<String> queue = new LinkedList<>();
        LinkedList<String> order = new LinkedList<>();
        order.add(s);
        queue.add(s);
        while (queue.size() != 0) {
            s = queue.poll();
            order.add(s);
            String n = Commit.getCommit(s).getParent();
            if (n != null) {
                if (!visited.containsKey(n)) {
                    visited.put(n, true);
                    queue.add(n);
                }
            }
            n = Commit.getCommit(s).getParent2();
            if (n != null) {
                if (!visited.containsKey(n)) {
                    visited.put(n, true);
                    queue.add(n);
                }
            }
        }
        return order;
    }

    private void untrackedCheck(Commit reset) {
        for (String filename : Utils.plainFilenamesIn("./")) {
            String hash = Utils.sha1(Utils.readContents(new File(filename)));
            if (!_HEAD.getBlobs().containsKey(filename)
                    && !staged.containsKey(filename)
                    && reset.getBlobs().containsKey(filename)
                    && !reset.getBlobs().get(filename).equals(hash)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    public void addRemote(String name, String path) {
        if (remotes.containsKey(name)) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }
        remotes.put(name, path);
    }

    public void rmRemote(String name) {
        if (!remotes.containsKey(name)) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        remotes.remove(name);
    }

    public void push(String name, String branch) {
        File remoteDir = new File(remotes.get(name));
        if (!remoteDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        Gitlet remote = Utils.readObject(new
                File(remotes.get(name) + "/gitlet"), Gitlet.class);
        if (!remote.branches.containsKey(branch)) {
            remote.branch(branch);
        }
        String remoteBranch = remote.branches.get(branch);
        LinkedList<String> history = bfs(_HEAD.getHash());
        if (!history.contains(remoteBranch)) {
            System.out.println("Please pull down remote"
                    + " changes before pushing.");
            System.exit(0);
        }
        int start = history.indexOf(remoteBranch);
        for (; start < history.size(); start++) {
            String commit = history.get(start);
            Utils.writeContents(new File(remotes.get(name) + commit),
                    Utils.readContents(new File(GITLET_DIR + commit)));
            for (String blob : Commit.getCommit(commit).getBlobs().values()) {
                Utils.writeContents(new File(remotes.get(name) + blob),
                        Utils.readContents(new File(GITLET_DIR + blob)));
            }
        }
        remote.branches.put(branch, _HEAD.getHash());
    }

    public void fetch(String name, String branch) {
        File remoteDir = new File(remotes.get(name));
        if (!remoteDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        Gitlet remote = Utils.readObject(new File(remotes.get(name)
                + "/gitlet"), Gitlet.class);
        if (!remote.branches.containsKey(branch)) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }
        if (!branches.containsKey(name + "/" + branch)) {
            branch(name + "/" + branch);
        }

        LinkedList<String> history = remote.bfs(remote.branches.get(branch));

        int start = history.indexOf(_HEAD.getHash());
        if (start == -1) {
            start = history.size() - 1;
        }
        for (; start >= 0; start--) {
            String commit = history.get(start);
            Utils.writeContents(new File(GITLET_DIR + commit),
                    Utils.readContents(new File(remotes.get(name)
                            + "/" + commit)));
            for (String blob : Commit.getCommit(commit).getBlobs().values()) {
                Utils.writeContents(new File(GITLET_DIR + blob),
                        Utils.readContents(new File(remotes.get(name)
                                + "/" + blob)));
            }
        }
        branches.put(name + "/" + branch, history.get(history.size() - 1));
    }

    public void pull(String name, String branch) {
        fetch(name, branch);
        merge(branch);
    }
}
