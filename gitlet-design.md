# Gitlet Design Document
author: Srikar Hanumanula

## 1. Classes and Data Structures

### Main

This is the driver class for the rest of the program.
It has methods to handle persistence and deal with command line inputs to the program.

#### Fields

1. static final File CWD: A pointer to the current working directory of the program.
2. static final File GITLET_FOLDER: A pointer to the `.gitlet` directory in the current working directory.
3. static Commit HEAD: The commit representing the current HEAD.
4. static ArrayList<Commit> branches: First commits of each branch.

### Commit

Represents a single commit, with commit information and blobs.

#### Fields

1. private Commit parent: The commit representing the previous commit.
2. private Commit parent2: Other previous commit for merge requests.
3. private ArrayList<Commit> children: ArrayList representing children of commit.
4. private String message: The message commit.
5. private Hashmap blob: Mapping of file names to blob references.




## 2. Algorithms

### Main.java
1. main(String[] args): This is the entry point of the program. It first checks to make sure that the input array is not empty. Then, it calls `setupPersistence`.
2. setupPersistence(): Sets up the persistence for Gitlet by creating the needed directories.
3. init(String[] args): Sets up the repo with one commit and adding it to branches.
4. add(String[] args): It adds files to the staging directory
5. commit(String[] args):  Creates a commit object and stores corresponding blobs.
6. rm(String[] args): remove file from staging directory
7. log(String[] args): Loop through parent commits starting with HEAD
8. global-log(String[] args): Go through all of the files in the commit directory and display
9. find(String[] args): Go through all files in the commit directory and trying to find the message.
10. status(String[] args): Uses branches to find the branches, uses staged directory, and finds files that have differing hashes.
11. checkout(String[] args): Takes the same file from the Commit HEAD or with Commit ID and replaces it in cwd, or takes all of the files from branch in branches and puts it in cwd.
12. branch(String[] args): Creates a new commit and adds it to branches.
13. rm-branch(String[] args): Removes corresponding commit pointer from branches.
14. reset(String[] args): call checkout with ID of commit and changes head to the commit.
15. merge(String[] args): Follow merge logic and move needed files over to cwd.
16. exitWithError(String message): It prints out MESSAGE and exits with error code -1.
17. validateNumArgs(String cmd, String[] args, int n):  It checks the number of arguments versus the expected number and throws a RuntimeException if they do not match.

### Commit.java
1. Commit(String message, Commit parent, Commit parent2): Create a commit object with given parameters.
2. Remove(): Removes the commit and makes sure chain of commits is still intact

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* Need to record the state of branches - Serialize branches
* Current Head: Serialize pointer
* Serialize all commits
* Staged files: directory of files

* Directories: staged directory, directory for blobs

## 4. Design Diagram


