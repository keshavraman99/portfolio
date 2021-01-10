package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Keshav Sharma
 */
class MovingRotor extends Rotor {

    /** _NOTCHES indicates the letters at which the moving rotor
     * has a notch.
     */
    private String _notches;

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
    }

    @Override
    void advance() {
        set(setting() + 1);
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        Character currentNotch = permutation().alphabet().toChar(setting());
        return _notches.contains(currentNotch.toString());
    }

}
