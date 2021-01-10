package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;

/** Class that describes Commit.
 * @author Keshav Sharma
 * */

public class Commit implements Serializable {

    /** Commit constructor.
     * @param commitMessage : Commit message of commit.
     * @param parent : parent Commit of current commit.
     * @param toAddF2SHA1 : Files staged to be added.
     * @param toRemove : Files staged to be removed.
     * */
    Commit(String commitMessage, Commit parent,
           HashMap<String, String> toAddF2SHA1,
           ArrayList<String> toRemove) {
        _childrenSHA1 = new ArrayList<String>();
        if (commitMessage.equals("initial commit")) {
            _fileNames2SHA1 = new HashMap<String, String>();
            _parentSHA1 = new ArrayList<String>();
            _commitTime = "Wed Dec 31 16:00:00 1969 -0800";
            _count = 0;
        } else {
            if (parent.commitMessage().equals("initial commit")) {
                _fileNames2SHA1 = updateBlobs(toAddF2SHA1, toRemove,
                        new HashMap<String, String>(), true);
            } else {
                _fileNames2SHA1 = updateBlobs(toAddF2SHA1, toRemove,
                        parent._fileNames2SHA1, false);
            }
            _parentSHA1 = new ArrayList<String>();
            _count = parent._count++;
            _parentSHA1.add(parent.sHA1());
            _commitTime = currentTime();
        }
        _commitMessage = commitMessage;
        if (commitMessage.equals("initial commit")) {
            _sHA1 = Utils.sha1(_commitMessage, _commitTime);
        } else {
            _sHA1 = Utils.sha1(_fileNames2SHA1.keySet().toString(),
                    _parentSHA1.toString(), _commitMessage, _commitTime);
        }
    }

    /** Retroactively modify childrenSHA1 attribute of commit.
     * @param child : Commit to have children SHA1 references
     *              to be added.
     * */
    public void addChildrenSHA1(Commit child) {
        this._childrenSHA1.add(child.sHA1());
    }

    /** Public access to commit number. */
    public int count() {
        return _count;
    }
    /** Returns the SHA1 values of commit children. */
    public ArrayList<String> childrenSHA1() {
        return _childrenSHA1;
    }

    /** Returns file names in commit to file sha1 values. */
    public HashMap<String, String> filesinCommit() {
        return _fileNames2SHA1;
    }

    /** Returns commit message. */
    public String commitMessage() {
        return _commitMessage;
    }

    /** Returns first parent SHA1. */
    public String firstParent() {
        return _parentSHA1.get(0);
    }

    /**Return ArrayList of parent SHA1's. */
    public ArrayList<String> parents() {
        return _parentSHA1;
    }

    /** Output of the log function for each commit. */
    public void logOutput() {
        System.out.println("===");
        System.out.println("commit " + this._sHA1);
        if (!_commitMessage.equals("initial commit")) {
            if (_parentSHA1.size() == 2) {
                System.out.println("Merge: "
                        + _parentSHA1.get(0).substring(0, 6) + " "
                        + _parentSHA1.get(1).substring(0, 6));
            }
        }
        System.out.println("Date: " + this._commitTime);
        System.out.println(this._commitMessage);
        System.out.println();
    }


    /** Function updates the blob references for the commit when constructed.
     * @param f2SHA1Staging : Files in staging area for addition.
     * @param parentF2SHA1 : Files from parent commit.
     * @param removal : Files staged for removal.
     * @param isSecondCommit : True if second commit.
     * @return Files in the current commit.
     * */
    public HashMap<String, String> updateBlobs(
            HashMap<String, String> f2SHA1Staging,
            ArrayList<String> removal,
            HashMap<String, String>
                    parentF2SHA1,
            boolean isSecondCommit) {
        if (isSecondCommit) {
            return f2SHA1Staging;
        } else {
            HashMap<String, String> copyParent = new HashMap<String, String>();
            copyParent.putAll(parentF2SHA1);
            for (String s : f2SHA1Staging.keySet()) {
                if (copyParent.containsKey(s)
                        && (!copyParent.get(s).equals(f2SHA1Staging.get(s)))) {
                    copyParent.put(s, f2SHA1Staging.get(s));
                } else if (copyParent.containsKey(s)
                        && copyParent.get(s).equals(f2SHA1Staging.get(s))) {
                    continue;
                } else {
                    copyParent.put(s, f2SHA1Staging.get(s));
                }
            }
            for (String s : removal) {
                if (copyParent.containsKey(s)) {
                    copyParent.remove(s);
                }
            }
            return copyParent;
        }
    }

    /** Returns the current time. */
    public String currentTime() {
        Date curr = new Date();
        return DATE_FORMAT.format(curr);
    }

    /** Returns commit's SHA1 value. */
    public String sHA1() {
        return _sHA1;
    }

    /**Accessor for serialVersion UID.
     * @return The serial version UID.
     * */
    public long uID() {
        return serialVersionUID;
    }


    /** Commit message of the commit. */
    private String _commitMessage;

    /** Time of commit. */
    private String _commitTime;

    /**Commit number. */
    private int _count;

    /** Arraylist of SHA1 values of children. */
    private ArrayList<String> _childrenSHA1;

    /** SHA1 of commit. */
    private String _sHA1;

    /** Arraylist of parent commit SHA1 values. */
    private ArrayList<String> _parentSHA1;

    /** Date formatter. */
    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");


    private final long serialVersionUID =  6849558214771468762L;

    /** File name to file SHA1 values hashmap in commit. */
    private HashMap<String, String> _fileNames2SHA1;



}
