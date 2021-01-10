package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Keshav Sharma
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycles;
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        this._cycles +=  cycle;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        return _alphabet.toInt(permute(_alphabet.toChar(wrap(p))));
    }

    /** Helper function goes through cycles to see the index
     * of the letter that should be mapped to, according to
     * whether permute or invert is called.
     * @param p the index of the character
     *          that you are starting with from alphabet
     * @param increment the increment you would use to f
     *                  ind the 'next' value in the permutation,
     *                  depending on whether permute or invert
     *                  is called
     * @param blocker The end of the sequence
     *                in a set of characters in _cycles t
     *                hat indicates that the character to map
     *                to is the first in the set
     * @param reverseEndpoint The parenthesis at the
     *                         beginning of the set of characters in _cycles,
     *                         when permute is called, and at the end
     *                         when invert is called
     * @return the index at alphabet of the
     * character the permutation maps to
     */

    private char permutationHelper(char p,
                                   int increment, Character blocker,
                                   Character reverseEndpoint) {
        Character initial = p;
        if (!_alphabet.contains(p)) {
            throw EnigmaException.error(
                    "Character not found in this Alphabet");
        }
        if (_cycles.contains(initial.toString())) {
            Character next = _cycles.charAt(_cycles.indexOf(p) + increment);
            if (next == blocker) {
                next = p;
                while (_cycles.charAt(_cycles.indexOf(next) - increment)
                        != reverseEndpoint) {
                    next = _cycles.charAt(_cycles.indexOf(next) - increment);
                }
            }
            if (!_alphabet.contains(next)) {
                throw EnigmaException.error(
                        "Character not found in this Alphabet");
            }
            return next;
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return _alphabet.toInt(invert(_alphabet.toChar(wrap(c))));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return permutationHelper(p, 1, ')', '(');
    }

    /** Return the result of ap plying the inverse of this permutation to C. */
    char invert(char c) {
        return permutationHelper(c, -1, '(', ')');
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < size(); i++) {
            Character check = _alphabet.toChar(i);
            if (!_cycles.contains(check.toString())) {
                return false;
            }
        }
        return true;
    }
    /** Alphabet of this permutation.
     */
    private Alphabet _alphabet;
    /** Cycles to be used in permutation.
     */
    private String _cycles;
}
