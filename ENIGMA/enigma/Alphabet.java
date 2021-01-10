package enigma;
import java.util.ArrayList;
import java.util.List;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Keshav Sharma
 */
class Alphabet {
    /**List for storing alphabet.
     */
    private List<Character> source;
    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        source = new ArrayList<>();
        for (int i = 0; i < chars.length(); i++) {
            if (source.contains(chars.charAt(i))) {
                throw EnigmaException.error(
                        "Multiple instances of same character in alphabet");
            }
            source.add(i, chars.charAt(i));

        }

    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return source.size();
    }

    /** Returns true if preprocess(CH) is in this alphabet. */
    boolean contains(char ch) {
        return source.contains(ch);
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return source.get(index);
    }

    /** Returns the index of character preprocess(CH), which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        return source.indexOf(ch);
    }

}
