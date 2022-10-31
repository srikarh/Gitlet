package gitlet;


import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

public class Commit implements Serializable {
    /** Commit message. */
    private String _message;

    /** Commit timestamp. */
    private String _timestamp;

    /** Commit blobs. */
    private TreeMap<String, String> blobs = new TreeMap<>();

    /** Commit parent. */
    private String _parent;

    /** Commit merge parent. */
    private String _parent2;

    public Commit(String message, String parent) {
        _message = message;
        _parent = parent;
        String format = "EEE MMM dd HH:mm:ss yyyy Z";
        SimpleDateFormat f = new SimpleDateFormat(format);
        if (_parent == null) {
            _timestamp = f.format(new Date(0));
        } else {
            _timestamp = f.format(new Date());
            File p = new File(Gitlet.GITLET_DIR + parent);
            Commit parentObj = Utils.readObject(p, Commit.class);
            this.blobs = parentObj.blobs;
        }
    }

    public Commit(String message, String parent, String parent2) {
        _message = message;
        _parent = parent;
        _parent2 = parent2;
        String format = "EEE MMM dd HH:mm:ss yyyy Z";
        SimpleDateFormat f = new SimpleDateFormat(format);
        if (_parent == null) {
            _timestamp = f.format(new Date(0));
        } else {
            _timestamp = f.format(new Date());
            File p = new File(Gitlet.GITLET_DIR + parent);
            Commit parentObj = Utils.readObject(p, Commit.class);
            this.blobs = parentObj.blobs;
        }
    }

    public static Commit getCommit(String abrev) {
        if (abrev == null) {
            return null;
        }
        String hash = null;
        for (String filename : Utils.plainFilenamesIn(Gitlet.GITLET_DIR)) {
            if (filename.indexOf(abrev) == 0) {
                hash = filename;
            }
        }
        if (hash == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        File commitFile = new File(Gitlet.GITLET_DIR + hash);
        Commit result = Utils.readObject(commitFile, Commit.class);
        return result;
    }

    public void setParent2(String hash) {
        _parent2 = hash;
    }

    public String getMessage() {
        return _message;
    }

    public String getTimestamp() {
        return _timestamp;
    }

    public String getParent() {
        return _parent;
    }

    public String getHash() {
        return Utils.sha1(Utils.serialize(this));
    }

    public TreeMap<String, String> getBlobs() {
        return this.blobs;
    }

    public String getParent2() {
        return _parent2;
    }

}
