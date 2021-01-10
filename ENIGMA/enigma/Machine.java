package enigma;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Keshav Sharma
 */
class Machine {


    /** Number of rotor slots.
     */
    private int _numRotors;
    /** Number of pawls.
     */
    private int _pawls;
    /** Number of moving rotors.
     */
    private int _numMovingRotors;
    /** Current rotors of machine.
     */
    private ArrayList<Rotor> _currentRotors = new ArrayList<Rotor>();
    /** Rotors that will advance due to enigma configuration.
     */
    private ArrayList<Rotor> _rotorsToAdvance = new ArrayList<Rotor>();
    /** All rotors available to be used.
     */
    private Collection<Rotor> _allRotors;
    /** Hashmap to access a rotor from with rotor name.
     */
    private HashMap<String, Rotor> _rotorSource = new HashMap<String, Rotor>();
    /** Plugboard permutation of machine.
     */
    private Permutation _plugboard;
    /** List of all rotor names.
     */
    private  List<String> _allRotorNames = new ArrayList<String>();

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        if (!((_pawls >= 0) && (_pawls < _numRotors))) {
            throw EnigmaException.error("impossible pawl number");
        }
        if (!(_numRotors > 1)) {
            throw EnigmaException.error("impossible rotor slot number");
        }
        _allRotors = allRotors;
        Iterator<Rotor> allRotorsIterator = _allRotors.iterator();
        for (int i = 0; i < _allRotors.size(); i++) {
            Rotor current = allRotorsIterator.next();
            _allRotorNames.add(current.name());
            _rotorSource.put(current.name(), current);
        }

    }


    /**  Returns the list of all rotor names.
     * @return list of all rotor names.
     */
    public List allRotorNames() {
        return _allRotorNames;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _currentRotors = new ArrayList<Rotor>();
        _numMovingRotors = 0;
        if (rotors.length != numRotors()) {
            throw EnigmaException.error(
                    "mismatch between rotors to insert "
                            + "and available slots in machine");
        }
        for (int i = 0; i < rotors.length; i++) {
            if (!_rotorSource.containsKey(rotors[i])) {
                throw EnigmaException.error(
                        "Rotor slot cannot be filled "
                                + "by rotor not present in allRotors");
            } else if (_currentRotors.contains(_rotorSource.get(rotors[i]))) {
                throw EnigmaException.error(
                        "Rotor can't be used in multiple positions");
            }
            if (_rotorSource.get(rotors[i]).rotates()) {
                _numMovingRotors++;
            }
            _currentRotors.add(i, _rotorSource.get(rotors[i]));

        }
        if (_numMovingRotors > numRotors()) {
            throw EnigmaException.error(
                    "More moving rotors than pawls available");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != numRotors() - 1) {
            throw EnigmaException.error(
                    "Incorrect amount of settings"
                            + " considering amount of rotors");
        }
        for (int i = 0; i < setting.length(); i++) {
            Character rotorSetting = setting.charAt(i);
            if (!_alphabet.contains(rotorSetting)) {
                throw EnigmaException.error(
                        "Incorrect character for rotor setting");
            }
            _currentRotors.get(i + 1).set(setting.charAt(i));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;

    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing

     *  the machine. */
    int convert(int c) {
        int output = c;
        advanceMachine();
        if (!(_plugboard == null)) {
            output = _plugboard.permute(output);
        }
        for (int i = _currentRotors.size() - 1; i >= 0; i--) {
            output = _currentRotors.get(i).convertForward(output);
        }
        for (int i = 1; i < _currentRotors.size(); i++) {
            output = _currentRotors.get(i).convertBackward(output);
        }
        if (!(_plugboard == null)) {
            output = _plugboard.permute(output);
        }
        return output;

    }

    /** Advances machine and moves rotor according to Enigma specifications.
     */
    void advanceMachine() {
        Rotor fastRotor = _currentRotors.get(_currentRotors.size() - 1);
        _rotorsToAdvance = new ArrayList<>();
        for (int i = 0; i < _currentRotors.size() - 1; i++) {
            Rotor current = _currentRotors.get(i);
            Rotor next = _currentRotors.get(i + 1);
            if (next.atNotch() && current.rotates()) {
                if (current.atNotch()) {
                    _rotorsToAdvance.add(next);
                } else {
                    _rotorsToAdvance.add(current);
                    _rotorsToAdvance.add(next);
                }
            }
        }
        for (int i = 0; i < _rotorsToAdvance.size(); i++) {
            _rotorsToAdvance.get(i).advance();
        }
        if (!_rotorsToAdvance.contains(fastRotor)) {
            fastRotor.advance();
        }
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String encoded = new String();
        int charatCount = 0;
        for (int i = 0; i < msg.length(); i++) {
            if (msg.charAt(i) != ' ') {
                Character orig = msg.charAt(i);
                encoded += _alphabet.toChar(convert(_alphabet.toInt(orig)));
                charatCount++;
                if (charatCount % 5 == 0 && (i != msg.length() - 1)) {
                    encoded += " ";
                }
            }
        }
        return encoded;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

}
