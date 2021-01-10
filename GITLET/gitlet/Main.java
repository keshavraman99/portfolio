package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashMap;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Keshav Sharma
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length < 1) {
            System.out.println("Please enter a command.");
            System.exit(0);
        } else if (args[0].equals("init")) {
            init();
        } else if (args[0].equals("add")) {
            incorrectOperands(args);
            add(args[1]);
        } else if (args[0].equals("commit")) {
            incorrectOperands(args);
            String cM = new String();
            for (int i = 1; i < args.length - 1; i++) {
                cM += args[i] + " ";
            }
            cM += args[args.length - 1];
            commit(cM);
        } else if (args[0].equals("rm")) {
            incorrectOperands(args);
            rm(args[1]);
        } else if (args[0].equals("log")) {
            log();
        } else if (args[0].equals("global-log")) {
            globalLog();
        } else if (args[0].equals("find")) {
            incorrectOperands(args);
            String cM = new String();
            for (int i = 1; i < args.length - 1; i++) {
                cM += args[i] + " ";
            }
            cM += args[args.length - 1];
            find(cM);
        } else if (args[0].equals("status")) {
            status();
        } else if (args[0].equals("checkout")) {
            if (args.length == 2) {
                checkoutBranchHeadFile(args[1]);
            } else if (args[1].equals("--")) {
                checkoutFile(args[2]);
            } else if (args[2].equals("--")) {
                checkoutCommitFile(args[1], args[3]);
            } else {
                System.out.println("Incorrect operands.");
            }
        } else if (args[0].equals("branch")) {
            branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            removeBranch(args[1]);
        } else if (args[0].equals("reset")) {
            reset(args[1]);
        } else if (args[0].equals("merge")) {
            merge(args[1]);
        } else {
            System.out.println("No command with that name exists");
            System.exit(0);
        }
    }

    /**Marks if incorrect operands.
     * @param args : Args to be tested.
     * */
    private static void incorrectOperands(String[] args) {
        if (args.length == 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Initializes Gitlet version-control system. */
    private static void init() throws IOException {
        if (f.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }
        f = Utils.join(System.getProperty("user.dir"), ".gitlet");
        f.mkdir();
        s = Utils.join(f, ".staging");
        s.mkdir();
        c = Utils.join(f, ".commits");
        c.mkdir();
        _currentPointer = "master";
        Commit initial = new Commit("initial commit", null, null, null);
        _pointerSource.put("master", initial.sHA1());
        _commitSource.put(initial.sHA1(), initial);
        cS = Utils.join(c, ".commitSource.txt");
        cS.createNewFile();
        pS = Utils.join(c, ".pointerSource.txt");
        pS.createNewFile();
        cP = Utils.join(c, ".currentPointer.txt");
        cP.createNewFile();
        fS = Utils.join(s, ".fileSource.txt");
        fS.createNewFile();
        fSHA1 = Utils.join(s, ".fileNametoSHA1.txt");
        fSHA1.createNewFile();
        tR = Utils.join(s, ".toRemove.txt");
        tR.createNewFile();
        Utils.writeObject(cS, _commitSource);
        Utils.writeObject(pS, _pointerSource);
        Utils.writeObject(cP, _currentPointer);
        Utils.writeObject(fS, _fileSource);
        Utils.writeObject(fSHA1, _fileNametoSHA1);
        Utils.writeObject(tR, _toRemove);
    }

    /** Add file to staging area.
     * @param fileName : file name to be staged for addition.
     * */
    @SuppressWarnings("unchecked")
    private static void add(String fileName) {
        File toAdd = Utils.join(System.getProperty("user.dir"), fileName);
        if (!toAdd.exists()) {
            System.out.print("File does not exist.");
            System.exit(0);
        }
        String willAdd = Utils.readContentsAsString(toAdd);
        String filesha1 = Utils.sha1(willAdd);
        _fileSource = Utils.readObject(fS, HashMap.class);
        _fileNametoSHA1 = Utils.readObject(fSHA1, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        _pointerSource = Utils.readObject(pS, HashMap.class);
        _commitSource =
                (HashMap<String, Commit>) Utils.readObject(cS, HashMap.class);
        _toRemove = Utils.readObject(tR, ArrayList.class);
        Commit curr = _commitSource.get(_pointerSource.get(_currentPointer));
        if (_toRemove.contains(fileName)) {
            _toRemove.remove(fileName);
            Utils.writeObject(tR, _toRemove);
            return;
        }
        if (!curr.commitMessage().equals("initial commit")) {
            HashMap<String, String> commitFiles = curr.filesinCommit();
            if (commitFiles.containsKey(fileName)
                    && commitFiles.get(fileName).equals(filesha1)) {
                return;
            }
        }
        _fileSource.put(filesha1, willAdd);
        _fileNametoSHA1.put(fileName, filesha1);
        Utils.writeObject(fS, _fileSource);
        Utils.writeObject(fSHA1, _fileNametoSHA1);
        Utils.writeObject(pS, _pointerSource);
        Utils.writeObject(cP, _currentPointer);
        Utils.writeObject(tR, _toRemove);
        Utils.writeObject(cS, _commitSource);

    }

    /** Creates commit with given message.
     * @param cM : commit message.
     * */
    @SuppressWarnings("unchecked")
    private static void commit(String cM) {
        if (cM.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        _fileNametoSHA1 = Utils.readObject(fSHA1, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        _pointerSource = Utils.readObject(pS, HashMap.class);
        _commitSource = Utils.readObject(cS, HashMap.class);
        _toRemove = Utils.readObject(tR, ArrayList.class);
        if (_fileNametoSHA1.isEmpty() && _toRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit parent = _commitSource.get(_pointerSource.get(_currentPointer));
        Commit toCreate = new Commit(cM, parent, _fileNametoSHA1, _toRemove);
        parent.addChildrenSHA1(toCreate);
        _pointerSource.put(_currentPointer, toCreate.sHA1());
        _commitSource.put(parent.sHA1(), parent);
        _commitSource.put(toCreate.sHA1(), toCreate);
        _fileNametoSHA1 = new HashMap<String, String>();
        _toRemove = new ArrayList<String>();
        Utils.writeObject(fSHA1, _fileNametoSHA1);
        Utils.writeObject(tR, _toRemove);
        Utils.writeObject(pS, _pointerSource);
        Utils.writeObject(cS, _commitSource);
    }

    /** Removes a file from staging area and
     * marked for removal if in latest commit.
     * @param fileName : Name of file marked for removal and to be unstaged.
     * */
    @SuppressWarnings("unchecked")
    public static void rm(String fileName) {
        _fileNametoSHA1 = Utils.readObject(fSHA1, HashMap.class);
        _commitSource = Utils.readObject(cS, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        _pointerSource = Utils.readObject(pS, HashMap.class);
        _toRemove = Utils.readObject(tR, ArrayList.class);
        boolean notHasFiles =
                _commitSource.get(_pointerSource.get(
                        _currentPointer)).filesinCommit().isEmpty();
        HashMap<String, String> filesTrackedInCommit =
                _commitSource.get(_pointerSource.get(
                        _currentPointer)).filesinCommit();
        if (notHasFiles) {
            if (!_fileNametoSHA1.containsKey(fileName)) {
                System.out.println("No reason to remove the file.");
                System.exit(0);
            }
        } else {
            if ((!filesTrackedInCommit.containsKey(fileName))
                    && (!_fileNametoSHA1.containsKey(fileName))) {
                System.out.println("No reason to remove the file.");
                System.exit(0);
            }
            if (_commitSource.get(_pointerSource.get(
                    _currentPointer)).filesinCommit().containsKey(fileName)) {
                Utils.restrictedDelete(Utils.join(
                        System.getProperty("user.dir"), fileName));
                _toRemove.add(fileName);
            }
        }
        if (_fileNametoSHA1.containsKey(fileName)) {
            _fileNametoSHA1.remove(fileName);
        }
        Utils.writeObject(fSHA1, _fileNametoSHA1);
        Utils.writeObject(tR, _toRemove);
        Utils.writeObject(cS, _commitSource);
        Utils.writeObject(cP, _currentPointer);
        Utils.writeObject(pS, _pointerSource);
    }


    /** Prints history of commits of given branch. */
    @SuppressWarnings("unchecked")
    private static void log() {
        _commitSource = Utils.readObject(cS, HashMap.class);
        _pointerSource = Utils.readObject(pS, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        Commit curr = _commitSource.get(_pointerSource.get(_currentPointer));
        while (!curr.equals(null)) {
            curr.logOutput();
            if (curr.commitMessage().equals("initial commit")) {
                break;
            }
            curr = _commitSource.get(curr.firstParent());
        }
        Utils.writeObject(cS, _commitSource);
        Utils.writeObject(pS, _pointerSource);
        Utils.writeObject(cP, _currentPointer);
    }

    /** Prints entire history of commits. */
    @SuppressWarnings("unchecked")
    private static void globalLog() {
        _commitSource = Utils.readObject(cS, HashMap.class);
        Iterator<String> source =  _commitSource.keySet().iterator();
        while (source.hasNext()) {
            String currSHA1 = source.next();
            _commitSource.get(currSHA1).logOutput();
        }
        Utils.writeObject(cS, _commitSource);
    }

    /** Find the commit id of commit with the give commit message.
     * @param cM : Commit message to look for.
     */
    @SuppressWarnings("unchecked")
    private static void find(String cM) {
        _commitSource = Utils.readObject(cS, HashMap.class);
        Iterator<String> source = _commitSource.keySet().iterator();
        int count = 0;
        while (source.hasNext()) {
            String currSHA1 = source.next();
            if (_commitSource.get(currSHA1).commitMessage().equals(cM)) {
                System.out.println(currSHA1);
                count++;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** Prints out status message. */
    @SuppressWarnings("unchecked")
    private static void status() {
        _pointerSource = Utils.readObject(pS, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        _fileNametoSHA1 = Utils.readObject(fSHA1, HashMap.class);
        _toRemove = Utils.readObject(tR, ArrayList.class);
        _commitSource = Utils.readObject(cS, HashMap.class);
        System.out.println("=== Branches ===");
        ArrayList<String> branches = new ArrayList<String>();
        branches.addAll(_pointerSource.keySet());
        Collections.sort(branches);
        for (String sol : branches) {
            if (sol.equals(_currentPointer)) {
                System.out.println("*" + _currentPointer);
            } else {
                System.out.println(sol);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        ArrayList<String> stagedFiles = new ArrayList<String>();
        stagedFiles.addAll(_fileNametoSHA1.keySet());
        Collections.sort(stagedFiles);
        for (String see: stagedFiles) {
            System.out.println(see);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        ArrayList<String> removedFiles = new ArrayList<String>();
        removedFiles.addAll(_toRemove);
        Collections.sort(removedFiles);
        for (String say : removedFiles) {
            System.out.println(say);
        }
        System.out.println();
        Commit curr = _commitSource.get(_pointerSource.get(_currentPointer));
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Checks out file from current head.
     * @param fileName : Name of file being checked out.
     * */
    @SuppressWarnings("unchecked")
    private static void checkoutFile(String fileName) {
        _commitSource = Utils.readObject(cS, HashMap.class);
        _pointerSource = Utils.readObject(pS, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        _fileSource = Utils.readObject(fS, HashMap.class);
        Commit head = _commitSource.get(_pointerSource.get(_currentPointer));
        if (!head.filesinCommit().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String contents = _fileSource.get(head.filesinCommit().get(fileName));
        File toOverwrite = Utils.join(System.getProperty("user.dir"), fileName);
        Utils.writeContents(toOverwrite, contents);
        Utils.writeObject(cS, _commitSource);
        Utils.writeObject(pS, _pointerSource);
        Utils.writeObject(cP, _currentPointer);
        Utils.writeObject(fS, _fileSource);
    }

    /** Checks out file from given commit.
     * @param cID : Commit SHA1 to check out to.
     * @param fileName : File to check out.
     * */
    @SuppressWarnings("unchecked")
    private static void checkoutCommitFile(String cID, String fileName) {
        _commitSource = Utils.readObject(cS, HashMap.class);
        _fileSource = Utils.readObject(fS, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        Commit source = _commitSource.get(_pointerSource.get(_currentPointer));
        boolean commitExists = false;
        if (_commitSource.containsKey(cID)) {
            commitExists = true;
            source = _commitSource.get(cID);
        } else {
            for (String sethi : _commitSource.keySet()) {
                if (sethi.startsWith(cID)) {
                    source = _commitSource.get(sethi);
                    commitExists = true;
                    break;
                }
            }
        }
        if (!commitExists) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        if (!source.filesinCommit().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String contents = _fileSource.get(source.filesinCommit().get(fileName));
        File toOverwrite = Utils.join(System.getProperty("user.dir"), fileName);
        Utils.writeContents(toOverwrite, contents);
        Utils.writeObject(cS, _commitSource);
        Utils.writeObject(fS, _fileSource);
        Utils.writeObject(cP, _currentPointer);
    }

    /** Resets to the commit refered to be cID,
     * and checks out all files from this commit.
     * @param cID : Commit SHA1 to reset to.
     * */
    @SuppressWarnings("unchecked")
    private static void reset(String cID) {
        _commitSource = Utils.readObject(cS, HashMap.class);
        _fileSource = Utils.readObject(fS, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        _pointerSource = Utils.readObject(pS, HashMap.class);
        _fileNametoSHA1 = Utils.readObject(fSHA1, HashMap.class);
        Commit source = _commitSource.get(_pointerSource.get(_currentPointer));
        boolean commitExists = false;
        if (_commitSource.containsKey(cID)) {
            commitExists = true;
            source = _commitSource.get(cID);
        } else {
            for (String sam : _commitSource.keySet()) {
                if (sam.startsWith(cID)) {
                    source = _commitSource.get(sam);
                    commitExists = true;
                    break;
                }
            }
        }
        if (!commitExists) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else {
            Commit current =
                    _commitSource.get(_pointerSource.get(_currentPointer));
            for (String input : source.filesinCommit().keySet()) {
                File check = Utils.join(System.getProperty("user.dir"), input);
                if (check.exists()) {
                    if (!current.filesinCommit().containsKey(input)) {
                        System.out.println("There is an "
                                + "untracked file in the way; "
                                + "delete it or add it first.");
                        System.exit(0);
                    } else {
                        Utils.writeContents(check,
                                _fileSource.get(
                                        source.filesinCommit().get(input)));
                    }
                } else {
                    Utils.writeContents(check,
                            _fileSource.get(source.filesinCommit().get(input)));
                }
            }
            for (String se : current.filesinCommit().keySet()) {
                File check2 = Utils.join(System.getProperty("user.dir"), se);
                if (!source.filesinCommit().containsKey(se)) {
                    Utils.restrictedDelete(check2);
                }
            }
            _pointerSource.put(_currentPointer, source.sHA1());
            _fileNametoSHA1 = new HashMap<String, String>();
            Utils.writeObject(pS, _pointerSource);
            Utils.writeObject(fSHA1, _fileNametoSHA1);
        }
    }


    /** Checks out commit that is head of given branch.
     * @param branchName : checkout to branchName.
     * */
    @SuppressWarnings("unchecked")
    private static void checkoutBranchHeadFile(String branchName) {
        _commitSource = Utils.readObject(cS, HashMap.class);
        _pointerSource = Utils.readObject(pS, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        _fileSource = Utils.readObject(fS, HashMap.class);
        _fileNametoSHA1 = Utils.readObject(fSHA1, HashMap.class);
        if (!_pointerSource.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (_currentPointer.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit refer = _commitSource.get(_pointerSource.get(branchName));
        Commit curr = _commitSource.get(_pointerSource.get(_currentPointer));
        for (String sa : refer.filesinCommit().keySet()) {
            File testy = Utils.join(System.getProperty("user.dir"), sa);
            if (testy.exists()) {
                if (!curr.filesinCommit().containsKey(sa)) {
                    System.out.println("There is an untracked file in "
                            + "the way; delete it or add it first.");
                    System.exit(0);
                } else {
                    Utils.writeContents(testy,
                            _fileSource.get(refer.filesinCommit().get(sa)));
                }

            } else {
                Utils.writeContents(testy,
                        _fileSource.get(refer.filesinCommit().get(sa)));
            }
        }
        for (String sit : curr.filesinCommit().keySet()) {
            File testy2 = Utils.join(System.getProperty("user.dir"), sit);
            if (!refer.filesinCommit().containsKey(sit)) {
                Utils.restrictedDelete(testy2);
            }
        }
        _toRemove = new ArrayList<String>();
        _currentPointer = branchName;
        _fileNametoSHA1 = new HashMap<String, String>();
        Utils.writeObject(cS, _commitSource);
        Utils.writeObject(pS, _pointerSource);
        Utils.writeObject(cP, _currentPointer);
        Utils.writeObject(fS, _fileSource);
        Utils.writeObject(fSHA1, _fileNametoSHA1);
    }

    /** Creates new pointer.
     * @param branchName : Name of a branch to create.
     * */
    @SuppressWarnings("unchecked")
    private static void branch(String branchName) {
        _pointerSource = Utils.readObject(pS, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        _commitSource = Utils.readObject(cS, HashMap.class);
        if (_pointerSource.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Commit curr = _commitSource.get(_pointerSource.get(_currentPointer));
        _pointerSource.put(branchName, _pointerSource.get(_currentPointer));
        Utils.writeObject(pS,  _pointerSource);
        Utils.writeObject(cP, _currentPointer);
        Utils.writeObject(cS, _commitSource);
    }

    /** Removes the branch reference.
     * @param branchName : Branch name to be removed.
     * */
    @SuppressWarnings("unchecked")
    private static void removeBranch(String branchName) {
        _pointerSource = Utils.readObject(pS, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        if (_currentPointer.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else if (!_pointerSource.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else {
            _pointerSource.remove(branchName);
        }
        Utils.writeObject(pS, _pointerSource);
        Utils.writeObject(cP, _currentPointer);
    }

    /** Calls merge between current branch head commit.
     * @param branchName : Branch name to be merged with current branch.
     */
    @SuppressWarnings("unchecked")
    private static void merge(String branchName) {
        _commitSource = Utils.readObject(cS, HashMap.class);
        _pointerSource = Utils.readObject(pS, HashMap.class);
        _currentPointer = Utils.readObject(cP, String.class);
        _fileNametoSHA1 = Utils.readObject(fSHA1, HashMap.class);
        _toRemove = Utils.readObject(tR, ArrayList.class);
        _fileSource = Utils.readObject(fS, HashMap.class);
        Commit curr = _commitSource.get(_pointerSource.get(_currentPointer));
        if ((!_fileNametoSHA1.isEmpty()) || (!_toRemove.isEmpty())) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else if (!_pointerSource.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (_currentPointer.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        Commit input = _commitSource.get(_pointerSource.get(branchName));
        Commit split = findSplitPoint(input, curr);
//        ArrayList alreadyModified = new ArrayList<String>();
        for (String s : split.filesinCommit().keySet()) {
            String check = split.filesinCommit().get(s);
            if ((!input.filesinCommit().containsKey(s)) && curr.filesinCommit().containsKey(s)) {
                if (curr.filesinCommit().get(s).equals(split.filesinCommit().get(s))) {
                    rm(s);
                }
            } else if ((!curr.filesinCommit().containsKey(s)) && input.filesinCommit().containsKey(s)) {
                if (input.filesinCommit().get(s).equals(split.filesinCommit().get(s))) {
                    continue;
                }
            } else if (input.filesinCommit().containsKey(s) && curr.filesinCommit().containsKey(s)) {
                String inputFile = input.filesinCommit().get(s);
                String currFile = curr.filesinCommit().get(s);
                String splitFile = split.filesinCommit().get(s);
                if ((!inputFile.equals(splitFile)) && currFile.equals(splitFile)) {
                    checkoutCommitFile(input.sHA1(), s);
                } else if ((!currFile.equals(splitFile)) && inputFile.equals(splitFile)) {
                    continue;
                } else if ((!inputFile.equals(splitFile)) && (!currFile.equals(splitFile))
                        && inputFile.equals(currFile)) {
                    continue;
                } else if ((!inputFile.equals(splitFile)) && (!curr.equals(splitFile))
                && (!inputFile.equals(currFile))) {
                    conflictMerge(input, curr);
                }
            }
//            alreadyModified.add(s);
        }
        for (String s : input.filesinCommit().keySet()) {
            if ((!split.filesinCommit().containsKey(s))
                    && (!curr.filesinCommit().containsKey(s))) {
                checkoutCommitFile(input.sHA1(), s);
                add(s);
            }
        }
        Utils.writeObject(cS, _commitSource);
        Utils.writeObject(pS, _pointerSource);
        Utils.writeObject(cP, _currentPointer);
        Utils.writeObject(fSHA1, _fileNametoSHA1);
        Utils.writeObject(fS, _fileNametoSHA1);
        Utils.writeObject(tR, _toRemove);
    }



    /** Finds split point for merge function. */
    private static Commit findSplitPoint(Commit input, Commit curr) {
        ArrayList<String> parents = curr.parents();
        for (String pSHA1 : parents) {
            Commit parent = _commitSource.get(pSHA1);
            if (parent.childrenSHA1().size() > 1) {
                for (String cSHA1 : parent.childrenSHA1()) {
                    if (!curr.sHA1().equals(cSHA1)) {
                        if (checkChildren(parent, parent, input)) {
                            return parent;
                        }
                    }
                }
            } else {
                findSplitPoint(input, parent);
            }
        }
        return null;
    }

    /** Helper function to findSplitPoint and merge, goes through children of a commit to
     * find input
     */
    private static boolean checkChildren(Commit parent, Commit possibleSplitPoint, Commit input) {
        for (String childrenSHA1 : parent.childrenSHA1()) {
            if (childrenSHA1.equals(input.sHA1())) {
                return true;
            } else {
                checkChildren(_commitSource.get(childrenSHA1), possibleSplitPoint, input);
            }
        }
        return false;
    }


    /**Helper function to merge that deals with the conflictMerge scenario when mere is called.
     */
    private static void conflictMerge(Commit input, Commit curr) {

    }

//     /** Goes through previous commits to find merge point. */
//     private Commit backTrack (Commit start, Commit find) {
//         Commit curr = start;
//         Commit possibleSplitPoint = new Commit;
//         boolean found = false;
//         String currSHA1 = curr.sHA1();
//         while (!found) {
//             for (String pSHA1 : curr.parents()) {
//                 curr = _commitSource.get(pSHA1);
//                 if (curr.childrenSHA1().size() > 1) {
//                     for (String sha1 : curr.childrenSHA1()) {
//                         if (!sha1.equals(currSHA1)) {
//                             if (forwardTrack(find)) {
//                                 return curr;
//                             }
//                         }
//                     }
//                 } else
//
//                 }
//             }
//         }
//     }


    /** Source of files.
     * @return HashMap of _fileSource.
     * */
    public static File fS() {
        return fS;
    }

    /** Commit HashMap.
     * @return Commit HashMap.*/
    public static File cS() {
        return cS;
    }

    /** Returns staging area.
     * @return HashMap of Files to be added.
     */
    public static File fSHA1() {
        return fSHA1;
    }

    /**Files to Remove.
     * @return _toRemove HashMap.*/
    public static File tR() {
        return tR;
    }

    /**Returns pointer Source. */
    public static File pS() {
        return pS;
    }

    /** Returns current Pointer. */
    public static File cP() {
        return cP;
    }

    /** Main .gitlet directory. */
    private static File f = new
            File(System.getProperty("user.dir") + "/.gitlet");
    /** Staging folder. */
    private static File s = new File(f + "/.staging");
    /** Commits folder. */
    private static File c = new File(f + "/.commits");
    /** Commit source, or the file with the hashmap of SHA1 to commit. */
    private static File cS = new File(c + "/.commitSource.txt");
    /** Pointer source, or the file with
     * the hashmap of pointer name to commit SHA1.*/
    private static File pS = new File(c + "/.pointerSource.txt");
    /** Current Pointer, or the file with
     * the String with the name of the current pointer. */
    private static File cP = new File(c + "/.currentPointer.txt");
    /** File Source, or HashMap of file SHA1 to files. */
    private static File fS = new File(s + "/.fileSource.txt");
    /** File to SHA1 HashMap, updated with new versions
     * of files with same file name to staging area. */
    private static File fSHA1 = new File(s + "/.fileNametoSHA1.txt");
    /** Files to remove, or ArrayList of File
     * names of files to remove next commit. */
    private static File tR = new File(s + "/.toRemove.txt");
    /** Name of current pointer. */
    private static String _currentPointer;
    /** Commit Source, or hashmap of commit sha1 to commit object. */
    private static HashMap<String, Commit>
            _commitSource = new HashMap<String, Commit>();
    /** Pointer Source, or hashmap of pointer names to commit sha1's. */
    private static HashMap<String, String>
            _pointerSource = new HashMap<String, String>();
    /** ArrayList of file names to be staged for removal. */
    private static ArrayList<String> _toRemove = new ArrayList<String>();
    /** Hashmap of file SHA1 to File objects. */
    private static HashMap<String, String>
            _fileSource = new HashMap<String, String>();
    /** Hashmap of file name to file sha1, which always
     * contains the latest version of that file.
     */
    private static HashMap<String, String>
            _fileNametoSHA1 = new HashMap<String, String>();

}
