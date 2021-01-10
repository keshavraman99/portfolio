package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Keshav Sharma
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine mach = readConfig();
        if (!_input.hasNext("\\*")) {
            throw EnigmaException.error(
                    "Setting line formatted "
                            + "incorrectly or not present");
        }
        while (_input.hasNext()) {
            String line = _input.nextLine();
            if ((!(line.length() == 0)) && line.charAt(0) == '*') {
                setInitialSetting(mach, line);
            } else if (line.matches("\\s*")) {
                _output.println();
            } else {
                readMessage(mach, line);
            }
        }

    }

    /** Helper function for PROCESS that takes in the setting
     * line of input and sets up machine MACH based on INITIALSETTING.
     * @param mach Machine being set up
     * @param initialSetting Input setting line
     */
    private void setInitialSetting(Machine mach, String initialSetting) {
        String[] initialSetList = initialSetting.split("\\s");
        _cycles = new String();
        _setting = new String();
        List<String> rotorNamesList = new ArrayList<String>();
        readInitialSetting(
                initialSetList, mach, _cycles, _setting, rotorNamesList);
        String[] rotorNames = new String[rotorNamesList.size()];
        for (int i = 0; i < rotorNamesList.size(); i++) {
            rotorNames[i] = rotorNamesList.get(i);
        }
        mach.insertRotors(rotorNames);
        if (!_setting.isEmpty()) {
            setUp(mach, _setting);
        }
        if (!_cycles.isEmpty()) {
            mach.setPlugboard(new Permutation(_cycles, _alphabet));
        }
    }

    /** Helper function to SETINITIALSETTING that reads
     * through setting line and assigns values to the input
     * parameters that correspond with
     * the input settings need for MACH.
     * @param initialSetList List of String items
     *                       in input setting line
     * @param mach Machine being used and set up
     * @param cycles Cycles for plugboard of MACH to use
     * @param setting Setting of MACH from input setting line
     * @param rotorNamesList Names of rotors to be used
     *                       in MACH from input setting line
     */
    private void readInitialSetting(String[] initialSetList,
                                    Machine mach, String cycles,
                                    String setting,
                                    List<String> rotorNamesList) {
        int rotorSlots = mach.numRotors();
        int settingCount = 1;
        for (int i = 1; i < initialSetList.length; i++) {
            if (mach.allRotorNames().contains(initialSetList[i])) {
                rotorNamesList.add(initialSetList[i]);
                rotorSlots--;
                if (rotorSlots < 0) {
                    throw EnigmaException.error(
                            "More rotors in _input than "
                                    + "available slots in machine");
                }
            }
            if (initialSetList[i].charAt(0) == '(') {
                if (i == initialSetList.length - 1) {
                    _cycles += initialSetList[i];
                } else {
                    _cycles += initialSetList[i] + " ";

                }
            }
            if (initialSetList[i].length() == mach.numRotors() - 1
                    && (!mach.allRotorNames().contains(initialSetList[i]))
                    && (!(initialSetList[i].charAt(0) == '('))) {
                _setting = initialSetList[i];
                settingCount--;
                if (settingCount < 0) {
                    throw EnigmaException.error(
                            "multiple settings for machine not possible");
                }
            }
        }
    }

    /**Helper function to PROCESS that reads the message
     * line(s) of input and formats them for OUTPUT MESSAGE.
     * @param mach Machine that is reading MESSAGE
     * @param message Input Message Line
     */
    private void readMessage(Machine mach, String message) {
        String[] messageList = message.split("\\s");
        String formattedMessage = new String();
        for (int i = 0; i < messageList.length; i++) {
            formattedMessage += messageList[i];
        }
        outputMessage(mach, formattedMessage);
    }

    /** Helper function to READMESSAGE that converts
     *  the message from input message line, after
     *  formatted by READMESSAGE.
     * and prints the proper output of the Enigma Machine
     * @param mach Machine encryping INPUT
     * @param input formatted message from READMESSAGE
     */
    private void outputMessage(Machine mach, String input) {
        String initialOutput = mach.convert(input);
        _output.println(initialOutput);
    }


    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next());
            if (!_config.hasNext("[0-9]")) {
                throw EnigmaException.error(
                        "number of rotor slots not specified "
                                + "or config file format incorrect");
            }
            int numRotors = Integer.parseInt(_config.next());
            if (!_config.hasNext("[0-9]")) {
                throw EnigmaException.error(
                        "number of pawls not specified "
                                + "or config file format incorrect");
            }
            int pawls = Integer.parseInt(_config.next());
            ArrayList<Rotor> allRotors = new ArrayList<Rotor>();
            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            String movingAndNotch = _config.next();
            Character typeOfRotor = movingAndNotch.charAt(0);
            String notches = new String();
            if (movingAndNotch.length() > 1) {
                for (int i = 1; i < movingAndNotch.length(); i++) {
                    Character notch = movingAndNotch.charAt(i);
                    notches += notch.toString();
                }
            }
            String cycles = _config.next();
            while (_config.hasNext("\\(.*\\)")) {
                cycles += " " + _config.next();
            }
            Permutation perm = new Permutation(cycles, _alphabet);
            if (typeOfRotor == 'M') {
                return new MovingRotor(name, perm, notches);
            } else if (typeOfRotor == 'N') {
                return new FixedRotor(name, perm);
            } else {
                return new Reflector(name, perm);
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        M.setRotors(settings);
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
    /** Cycles to be incorporated for plugboard of machine.
     */
    private String _cycles;
    /** Setting of machine.
     */
    private String _setting;

}

