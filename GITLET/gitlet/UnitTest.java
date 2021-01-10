package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    /** A dummy test to avoid complaint. */
    @Test
    @SuppressWarnings("unchecked")
    public void testAdd() throws IOException {
        String[] command1 = new String[1];
        command1[0] = "init";
        Main.main(command1);
        File hello = new File("hello.txt");
        hello.createNewFile();
        String[] command2 = new String[2];
        Main.main(convertToStringArray("add hello.txt"));
        HashMap<String, String> testF2SHA1
                = Utils.readObject(Main.fSHA1(), HashMap.class);
        assertTrue(testF2SHA1.containsKey("hello.txt"));
        HashMap<String, String> testFileSource
                = Utils.readObject(Main.fS(), HashMap.class);
        assertEquals(Utils.readContentsAsString(hello),
                testFileSource.get(testF2SHA1.get("hello.txt")));
        Utils.writeObject(Main.fSHA1(), testF2SHA1);
        Utils.writeObject(Main.fS(), testFileSource);
        Utils.restrictedDelete(hello);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRM() throws IOException {
        testAdd();
        String[] command3 = new String[2];
        command3[0] = "rm";
        command3[1] = "hello.txt";
        Main.main(command3);
        ArrayList<String> toRemove
                = Utils.readObject(Main.tR(), ArrayList.class);
        assertTrue(toRemove.contains(command3[1]));
        HashMap<String, String> f2SHA1
                = Utils.readObject(Main.fSHA1(), HashMap.class);
        assertFalse(f2SHA1.containsKey(command3[1]));
        Utils.writeObject(Main.tR(), toRemove);
        Utils.writeObject(Main.fSHA1(), f2SHA1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddtoRemovefromRemove() throws IOException {
        Main.main("init");
        File world = new File("world.txt");
        world.createNewFile();
        String[] command2 = new String[2];
        command2[0] = "add"; command2[1] = "world.txt";
        Main.main(command2);
        String[] command3 = new String[2];
        command3[0] = "rm"; command3[1] = "world.txt";
        Main.main(command3);
        String[] command4 = new String[2];
        command4[0] = "add"; command4[1] = "world.txt";
        Main.main(command4);
        ArrayList<String> toRemove
                = Utils.readObject(Main.tR(), ArrayList.class);
        HashMap<String, String> f2SHA1
                = Utils.readObject(Main.fSHA1(), HashMap.class);
        assertFalse(toRemove.contains(command3[1]));
        assertTrue(f2SHA1.containsKey("world.txt"));
        Utils.writeObject(Main.tR(), toRemove);
        Utils.writeObject(Main.fSHA1(), f2SHA1);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testCommit() throws IOException {
        Main.main("init");
        File hello = new File("hello.txt");
        hello.createNewFile();
        File world = new File("world.txt");
        world.createNewFile();
        String[] command1 = new String[2];
        command1[0] = "add";
        command1[1] = "hello.txt";
        Main.main(command1);
        String[] command2 = new String[2];
        command2[0] = "add";
        command2[1] = "world.txt";
        Main.main(command2);
        String[] commitCommand1 = new String[4];
        commitCommand1[0] = "commit";
        commitCommand1[1] = "hello";
        commitCommand1[2] = "old";
        commitCommand1[3] = "friend";
        Main.main(commitCommand1);
        HashMap<String, Commit> commitSource
                = Utils.readObject(Main.cS(), HashMap.class);
        HashMap<String, String> pointerSource
                = Utils.readObject(Main.pS(), HashMap.class);
        String currentPointer = Utils.readObject(Main.cP(), String.class);
        Commit current = commitSource.get(pointerSource.get(currentPointer));
        assertTrue(current.filesinCommit().containsKey("hello.txt"));
        assertTrue(current.filesinCommit().containsKey("world.txt"));
        assertFalse(current.commitMessage().equals("initial commit"));
        assertTrue(commitSource.keySet().size() == (2));
        Utils.writeObject(Main.cS(), commitSource);
        Utils.writeObject(Main.pS(), pointerSource);
        Utils.writeObject(Main.cP(), currentPointer);
        String[] command3 = new String[2];
        Main.main(convertToStringArray("rm world.txt"));
        String[] commitCommand2 = new String[3];
        commitCommand2[0] = "commit";
        commitCommand2[1] = "Round";
        commitCommand2[2] = "Two";
        Main.main(commitCommand2);
        HashMap<String, Commit> commitSource2
                = Utils.readObject(Main.cS(), HashMap.class);
        HashMap<String, String> pointerSource2
                = Utils.readObject(Main.pS(), HashMap.class);
        String currentPointer2 = Utils.readObject(Main.cP(), String.class);
        Commit curr2 = commitSource2.get(pointerSource2.get(currentPointer2));
        assertFalse(curr2.filesinCommit().containsKey("world.txt"));
        assertTrue(curr2.filesinCommit().containsKey("hello.txt"));
        assertTrue(commitSource2.keySet().size() == 3);
        assertTrue(curr2.commitMessage().equals("Round Two"));
        Main.main("log");
        Utils.writeObject(Main.cS(), commitSource2);
        Utils.writeObject(Main.pS(), pointerSource2);
        Utils.writeObject(Main.cP(), currentPointer2);
        String[] findCommand1 = new String[4];
        findCommand1[0] = "find"; findCommand1[1] = "hello";
        findCommand1[2] = "old"; findCommand1[3] = "friend";
        Main.main(findCommand1);
    }

    /** Only run after test commit and modify
     * hello.txt to contain String "hello chicken". */
    @Test
    @SuppressWarnings("unchecked")
    public void testCheckout() throws IOException {
        String[] command1 = new String[3];
        command1[0] = "checkout";
        command1[1] = "--";
        command1[2] = "hello.txt";
        Main.main(command1);
        File reference
                = Utils.join(System.getProperty("user.dir"), "hello.txt");
        String contents
                = Utils.readContentsAsString(reference);
        HashMap<String, Commit> commitSource
                = Utils.readObject(Main.cS(), HashMap.class);
        HashMap<String, String> fileSource
                = Utils.readObject(Main.fS(), HashMap.class);
        String currentPointer = Utils.readObject(Main.cP(), String.class);
        HashMap<String, String> pointerSource
                = Utils.readObject(Main.pS(), HashMap.class);
        assertTrue(contents.equals(fileSource.get(
                commitSource.get(pointerSource.get(
                        currentPointer)).filesinCommit().get("hello.txt"))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCheckoutFiles() throws IOException {
        Main.main("init");
        File hello = new File("hello.txt");
        hello.createNewFile();
        String[] command1 = new String[2];
        command1[0] = "add";
        command1[1] = "hello.txt";
        Main.main(command1);
        String[] command2 = new String[3];
        command2[0] = "commit";
        command2[1] = "Round";
        command2[2] = "One";
        Main.main(command2);
        File world = new File("world.txt");
        world.createNewFile();
        String[] command3 = new String[2];
        command3[0] = "add";
        command3[1] = "world.txt";
        Main.main(command3);
        String toAdd = "Hola Juan";
        Utils.writeObject(hello, toAdd);
        String mhello = Utils.readContentsAsString(hello);
        String[] command4 = new String[2];
        command4[0] = "add";
        command4[1] = "hello.txt";
        Main.main(command4);
        Main.main(convertToStringArray("commit Round Two"));
        String toAdd1 = "hello John";
        Utils.writeObject(hello, toAdd1);
        assertTrue(!mhello.equals(Utils.readContentsAsString(hello)));
        Main.main(convertToStringArray("add hello.txt"));
        Main.main(convertToStringArray("commit Round Three"));
        HashMap<String, Commit> commitSource
                = Utils.readObject(Main.cS(), HashMap.class);
        String key = new String();
        for (String s : commitSource.keySet()) {
            if (commitSource.get(s).commitMessage().equals("Round Two")) {
                key = s;
            }
        }
        Utils.writeObject(Main.cS(), commitSource);
        Main.main(convertToStringArray("checkout " + key + " -- hello.txt"));
        HashMap<String, Commit> commitSource2
                = Utils.readObject(Main.cS(), HashMap.class);
        HashMap<String, String> pointerSource
                = Utils.readObject(Main.pS(), HashMap.class);
        String currentPointer = Utils.readObject(Main.cP(), String.class);
        HashMap<String, String> fileSource
                = Utils.readObject(Main.fS(), HashMap.class);
        String compare = Utils.readContentsAsString(hello);
        assertEquals(mhello, compare);
        Utils.writeObject(Main.cS(), commitSource2);
        Utils.writeObject(Main.pS(), pointerSource);
        Utils.writeObject(Main.fS(), fileSource);
        Utils.restrictedDelete("hello.txt");
        Utils.restrictedDelete("world.txt");
    }

    @Test
    public void testStatusEmpty() throws IOException {
        Main.main("init");
        Main.main("status");
    }

    @Test
    public void testStatusWithStage() throws IOException {
        Main.main("init");
        File hello = new File("g.txt");
        File world = new File("f.txt");
        hello.createNewFile();
        world.createNewFile();
        Main.main(convertToStringArray("add g.txt"));
        Main.main(convertToStringArray("add f.txt"));
        Main.main("status");
        Main.main(convertToStringArray("rm f.txt"));
        Main.main("status");
        Utils.restrictedDelete(hello);
        Utils.restrictedDelete(world);

    }

    public String[] convertToStringArray(String s) {
        return s.split(" ");
    }

}


