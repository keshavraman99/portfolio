package enigma;


import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static enigma.TestUtils.*;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class MachineTests {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Machine _mach1;
    private String alpha = UPPER_STRING;
    private ArrayList<Rotor> rotors;
    private Alphabet alphabet = new Alphabet(alpha);
    private Permutation permRotorI =
            new Permutation("(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)",
                    alphabet);
    private Permutation permRotorII =
            new Permutation("(FIXVYOMW) (CDKLHUP) (ESZ) (BJ) (GR) (NT) (A) (Q)",
                    alphabet);
    private Permutation permRotorIII =
            new Permutation("(ABDHPEJT) (CFLVMZOYQIRWUKXSG) (N)",
                    alphabet);
    private Permutation permRotorIV =
            new Permutation("(AEPLIYWCOXMRFZBSTGJQNH) (DV) (KU)",
                    alphabet);
    private Permutation permRotorBeta =
            new Permutation("(ALBEVFCYODJWUGNMQTZSKPR) (HIX)",
                    alphabet);
    private Permutation permReflectorB =
            new Permutation(
                    "(AE) (BN) (CK) (DQ) (FU) (GY) "
                            + "(HW) (IJ) (LO) (MP) (RX) (SZ) (TV)",
                    alphabet);
    private MovingRotor rotorI =
            new MovingRotor("Rotor I", permRotorI, "Q");
    private MovingRotor rotorII =
            new MovingRotor("Rotor II", permRotorII, "E");
    private MovingRotor rotorIII =
            new MovingRotor("Rotor III", permRotorIII, "V");
    private MovingRotor rotorIV =
            new MovingRotor("Rotor IV", permRotorIV, "J");
    private FixedRotor rotorBeta =
            new FixedRotor("Rotor Beta", permRotorBeta);
    private Reflector reflectorB =
            new Reflector("Reflector B", permReflectorB);


    @Test
    public void testConvert() {
        rotors = new ArrayList<Rotor>();
        rotors.add(0, reflectorB);
        rotors.add(1, rotorBeta);
        rotors.add(2, rotorIII);
        rotors.add(3, rotorII);
        rotors.add(4, rotorI);
        _mach1 = new Machine(alphabet, 5, 3, rotors);
        String[] rotorString = new String[rotors.size()];
        for (int i = 0; i < rotors.size(); i++) {
            rotorString[i] = rotors.get(i).name();
        }
        _mach1.insertRotors(rotorString);
        _mach1.setPlugboard(new Permutation("(AH) (ZY) (DX)", alphabet));
        assertEquals(alphabet.toInt('M'), _mach1.convert(alphabet.toInt('A')));
        assertEquals(alphabet.toInt('Q'), _mach1.convert(alphabet.toInt('I')));
        assertEquals(alphabet.toInt('U'), _mach1.convert(alphabet.toInt('D')));
        _mach1 = new Machine(alphabet, 5, 3, rotors);
        _mach1.insertRotors(rotorString);
        _mach1.setRotors("AUDO");
        _mach1.setPlugboard(new Permutation("(LX) (JV) (JG)", alphabet));
        assertEquals(alphabet.toInt('C'), _mach1.convert(alphabet.toInt('L')));
        assertEquals(alphabet.toInt('I'), _mach1.convert(alphabet.toInt('A')));
        assertEquals(alphabet.toInt('L'), _mach1.convert(alphabet.toInt('G')));
        assertEquals(alphabet.toInt('D'), _mach1.convert(alphabet.toInt('O')));
        assertEquals(alphabet.toInt('Z'), _mach1.convert(alphabet.toInt('S')));
    }

    @Test
    public void checkConvertString() {
        rotors = new ArrayList<Rotor>();
        rotors.add(0, reflectorB);
        rotors.add(1, rotorBeta);
        rotors.add(2, rotorIII);
        rotors.add(3, rotorIV);
        rotors.add(4, rotorI);
        _mach1 = new Machine(alphabet, 5, 3, rotors);
        String[] rotorString = new String[rotors.size()];
        for (int i = 0; i < rotors.size(); i++) {
            rotorString[i] = rotors.get(i).name();
        }
        _mach1.insertRotors(rotorString);
        _mach1.setRotors("AXLE");
        _mach1.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)",
                alphabet));
        String input = "FROM HIS SHOULDER HIAWATHA "
                + "TOOK THE CAMERA OF ROSEWOOD "
                + "MADE OF SLIDING FOLDING ROSEWOOD "
                + "NEATLY PUT IT ALL TOGETHER IN ITS CASE "
                + "IT LAY COMPACTLY FOLDED INTO NEARLY "
                + "NOTHING BUT HE OPENED OUT THE HINGES "
                + "PUSHED AND PULLED THE JOINTS AND HINGES"
                + " TILL IT LOOKED ALL SQUARES AND OBLONGS "
                + "LIKE A COMPLICATED FIGURE IN THE SECOND "
                + "BOOK OF EUCLID";
        String expectedOutput = "QVPQS OKOIL PUBKJ ZPISF "
                + "XDWBH CNSCX NUOAA "
                + "TZXSR CFYDG UFLPN XGXIX TYJUJ RCAUG "
                + "EUNCF MKUFW JFGKC IIRGX ODJGV "
                + "CGPQO HALWE BUHTZ MOXII VXUEF PRPRK CGVPF "
                + "PYKIK ITLBU RVGTS FUSMB "
                + "NKFRI IMPDO FJVTT UGRZM UVCYL FDZPG IBXRE "
                + "WXUEB ZQJOY MHIPG RREGO HETUX"
                + " DTWLC MMWAV NVJVH OUFAN TQACK KTOZZ RDABQ "
                + "NNVPO IEFQA FSVVI "
                + "CVUDU EREYN PFFMN BJVGQ";
    }

}
