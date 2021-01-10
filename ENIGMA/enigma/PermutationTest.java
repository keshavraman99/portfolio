package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @Keshav Sharma
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void testInvert() {
        Alphabet alphabetOne = new Alphabet();
        Alphabet beta = new Alphabet("ABCMNOP");
        Permutation p = new Permutation(
                "(PNH) (ABDFIKLZYXW) (JC)", alphabetOne);
        Permutation q = new Permutation("(PNH) (ABDFIKLZYXW) (JC)", beta);
        assertEquals('N', p.invert('H'));
        assertEquals(alphabetOne.toInt('N'), p.invert(alphabetOne.toInt('H')));
        assertEquals('W', p.invert('A'));
        assertEquals(alphabetOne.toInt('W'), p.invert(alphabetOne.toInt('A')));
        assertEquals('C', p.invert('J'));
        assertEquals(alphabetOne.toInt('C'), p.invert(alphabetOne.toInt('J')));
    }

    @Test
    public void testpermute() {
        Alphabet alphabetTwo = new Alphabet();
        Permutation p = new Permutation(
                "(PNH) (ABDFIKLZYXW) (JC)", alphabetTwo);
        assertEquals('P', p.permute('H'));
        assertEquals(alphabetTwo.toInt('P'), p.permute(alphabetTwo.toInt('H')));
        assertEquals('C', p.permute('J'));
        assertEquals(alphabetTwo.toInt('C'), p.permute(alphabetTwo.toInt('J')));
        assertEquals('B', p.permute('A'));
        assertEquals(alphabetTwo.toInt('B'), p.permute(alphabetTwo.toInt('A')));

    }

    @Test
    public void testderangment() {
        Alphabet alphabetThree = new Alphabet();
        Permutation p = new Permutation("(PNH) (ABD) (JC)", alphabetThree);
        assertEquals(false, p.derangement());

    }




}
