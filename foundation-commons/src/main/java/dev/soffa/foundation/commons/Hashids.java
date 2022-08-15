package dev.soffa.foundation.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"PMD.GodClass", "PMD.AvoidLiteralsInIfCondition"})
public class Hashids {
    private static final String DEFAULT_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final String DEFAULT_SEPS = "cfhistuCFHISTU";
    private static final String DEFAULT_SALT = "";

    private static final int DEFAULT_MIN_HASH_LENGTH = 0;
    private static final int MIN_ALPHABET_LENGTH = 16;
    private static final double SEP_DIV = 3.5;
    private static final int GUARD_DIV = 12;

    private final String salt;
    private final int minHashLength;
    private final String alphabet;
    private final String seps;
    private final String guards;

    public Hashids() {
        this(DEFAULT_SALT);
    }

    public Hashids(String salt) {
        this(salt, 0);
    }

    public Hashids(String salt, int minHashLength) {
        this(salt, minHashLength, DEFAULT_ALPHABET);
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public Hashids(String salt, int minHashLength, final String chars) {
        String alphabet = chars;
        this.salt = salt != null ? salt : DEFAULT_SALT;
        this.minHashLength = minHashLength > 0 ? minHashLength : DEFAULT_MIN_HASH_LENGTH;

        final StringBuilder uniqueAlphabet = new StringBuilder();
        for (int i = 0; i < alphabet.length(); i++) {
            if (uniqueAlphabet.indexOf(String.valueOf(alphabet.charAt(i))) == -1) {
                uniqueAlphabet.append(alphabet.charAt(i));
            }
        }

        alphabet = uniqueAlphabet.toString();

        if (alphabet.length() < MIN_ALPHABET_LENGTH) {
            throw new IllegalArgumentException(
                "alphabet must contain at least " + MIN_ALPHABET_LENGTH + " unique characters");
        }

        if (alphabet.contains(" ")) {
            throw new IllegalArgumentException("alphabet cannot contains spaces");
        }

        // seps should contain only characters present in alphabet;
        // alphabet should not contains seps
        String seps = DEFAULT_SEPS;
        for (int i = 0; i < seps.length(); i++) {
            final int j = alphabet.indexOf(seps.charAt(i));
            if (j == -1) {
                seps = seps.substring(0, i) + " " + seps.substring(i + 1);
            } else {
                alphabet = alphabet.substring(0, j) + " " + alphabet.substring(j + 1);
            }
        }

        alphabet = alphabet.replaceAll("\\s+", "");
        seps = seps.replaceAll("\\s+", "");
        seps = Hashids.consistentShuffle(seps, this.salt);

        if (seps.isEmpty() || ((float) alphabet.length() / seps.length()) > SEP_DIV) {
            int sepsLen = (int) Math.ceil(alphabet.length() / SEP_DIV);

            if (sepsLen == 1) {
                sepsLen++;
            }

            if (sepsLen > seps.length()) {
                final int diff = sepsLen - seps.length();
                seps += alphabet.substring(0, diff);
                alphabet = alphabet.substring(diff);
            } else {
                seps = seps.substring(0, sepsLen);
            }
        }

        alphabet = Hashids.consistentShuffle(alphabet, this.salt);
        // use double to round up
        final int guardCount = (int) Math.ceil((double) alphabet.length() / GUARD_DIV);

        String guards;
        if (alphabet.length() < 3) {
            guards = seps.substring(0, guardCount);
            seps = seps.substring(guardCount);
        } else {
            guards = alphabet.substring(0, guardCount);
            alphabet = alphabet.substring(guardCount);
        }
        this.guards = guards;
        this.alphabet = alphabet;
        this.seps = seps;
    }

    public static int checkedCast(long value) {
        final int result = (int) value;
        if (result != value) {
            // don't use checkArgument here, to avoid boxing
            throw new IllegalArgumentException("Out of range: " + value);
        }
        return result;
    }

    @SuppressWarnings({"PMD.AvoidReassigningLoopVariables", "PMD.ForLoopVariableCount"})
    private static String consistentShuffle(final String alphabet, final String salt) {
        if (salt.length() <= 0) {
            return alphabet;
        }

        int ascVal;
        int j;
        final char[] tmpArr = alphabet.toCharArray();
        for (int i = tmpArr.length - 1, v = 0, p = 0; i > 0; i--, v++) {
            v %= salt.length();
            ascVal = salt.charAt(v);
            p += ascVal;
            j = (ascVal + v + p) % i;
            final char tmp = tmpArr[j];
            tmpArr[j] = tmpArr[i];
            tmpArr[i] = tmp;
        }

        return new String(tmpArr);
    }

    private static String hash(final long value, final String alphabet) {
        StringBuilder hash = new StringBuilder();
        final int alphabetLen = alphabet.length();
        long input = value;

        do {
            final int index = (int) (input % alphabetLen);
            if (index >= 0 && index < alphabet.length()) {
                hash.insert(0, alphabet.charAt(index));
            }
            input /= alphabetLen;
        } while (input > 0);

        return hash.toString();
    }

    private static Long unhash(String input, String alphabet) {
        long number = 0;
        long pos;

        for (int i = 0; i < input.length(); i++) {
            pos = alphabet.indexOf(input.charAt(i));
            number = number * alphabet.length() + pos;
        }

        return number;
    }

    /**
     * Encrypt numbers to string
     *
     * @param numbers the numbers to encrypt
     * @return the encrypt string
     */
    public String encode(long... numbers) {
        if (numbers.length == 0) {
            return "";
        }

        for (final long number : numbers) {
            if (number < 0) {
                return "";
            }
        }
        return this.internalEncode(numbers);
    }

    /* Private methods */

    /**
     * Decrypt string to numbers
     *
     * @param hash the encrypt string
     * @return decryped numbers
     */
    public long[] decode(String hash) {
        if (hash.isEmpty()) {
            return new long[0];
        }

        String validChars = this.alphabet + this.guards + this.seps;
        for (int i = 0; i < hash.length(); i++) {
            if (validChars.indexOf(hash.charAt(i)) == -1) {
                return new long[0];
            }
        }

        return this.internalDecode(hash, this.alphabet);
    }

    /**
     * Encrypt hexa to string
     *
     * @param hexa the hexa to encrypt
     * @return the encrypt string
     */
    public String encodeHex(String hexa) {
        if (!hexa.matches("^[\\da-fA-F]+$")) {
            return "";
        }

        final List<Long> matched = new ArrayList<>();
        final Matcher matcher = Pattern.compile("[\\w\\W]{1,12}").matcher(hexa);

        while (matcher.find()) {
            matched.add(Long.parseLong("1" + matcher.group(), 16));
        }

        // conversion
        final long[] result = new long[matched.size()];
        for (int i = 0; i < matched.size(); i++) {
            result[i] = matched.get(i);
        }

        return this.encode(result);
    }

    /**
     * Decrypt string to numbers
     *
     * @param hash the encrypt string
     * @return decryped numbers
     */
    public String decodeHex(String hash) {
        final StringBuilder result = new StringBuilder();
        final long[] numbers = this.decode(hash);

        for (final long number : numbers) {
            result.append(Long.toHexString(number).substring(1));
        }

        return result.toString();
    }

    private String internalEncode(long... numbers) {
        long numberHashInt = 0;
        for (int i = 0; i < numbers.length; i++) {
            numberHashInt += numbers[i] % (i + 100);
        }
        String alphabet = this.alphabet;
        final char ret = alphabet.charAt((int) (numberHashInt % alphabet.length()));

        long num;
        long sepsIndex;
        long guardIndex;
        String buffer;
        final StringBuilder retStrB = new StringBuilder(this.minHashLength);
        retStrB.append(ret);
        char guard;

        for (int i = 0; i < numbers.length; i++) {
            num = numbers[i];
            buffer = ret + this.salt + alphabet;

            alphabet = Hashids.consistentShuffle(alphabet, buffer.substring(0, alphabet.length()));
            final String last = Hashids.hash(num, alphabet);

            retStrB.append(last);

            if (i + 1 < numbers.length) {
                if (last.length() > 0) {
                    num %= last.charAt(0) + i;
                    sepsIndex = (int) (num % this.seps.length());
                } else {
                    sepsIndex = 0;
                }
                retStrB.append(this.seps.charAt((int) sepsIndex));
            }
        }

        StringBuilder retStr = new StringBuilder(retStrB.toString());
        if (retStr.length() < this.minHashLength) {
            guardIndex = (numberHashInt + (retStr.charAt(0))) % this.guards.length();
            guard = this.guards.charAt((int) guardIndex);

            retStr.insert(0, guard);

            if (retStr.length() < this.minHashLength) {
                guardIndex = (numberHashInt + (retStr.charAt(2))) % this.guards.length();
                guard = this.guards.charAt((int) guardIndex);

                retStr.append(guard);
            }
        }

        final int halfLen = alphabet.length() / 2;
        while (retStr.length() < this.minHashLength) {
            alphabet = Hashids.consistentShuffle(alphabet, alphabet);
            retStr = new StringBuilder(alphabet.substring(halfLen) + retStr + alphabet.substring(0, halfLen));
            final int excess = retStr.length() - this.minHashLength;
            if (excess > 0) {
                final int startPos = excess / 2;
                retStr = new StringBuilder(retStr.substring(startPos, startPos + this.minHashLength));
            }
        }

        return retStr.toString();
    }

    private long[] internalDecode(final String hash, final String chars) {
        final ArrayList<Long> ret = new ArrayList<>();
        String alphabet = chars;
        int i = 0;
        final String regexp = "[" + this.guards + "]";
        String hashBreakdown = hash.replaceAll(regexp, " ");
        String[] hashArray = hashBreakdown.split(" ");

        if (hashArray.length == 3 || hashArray.length == 2) {
            i = 1;
        }

        if (hashArray.length > 0) {
            hashBreakdown = hashArray[i];
            if (!hashBreakdown.isEmpty()) {
                final char lottery = hashBreakdown.charAt(0);

                hashBreakdown = hashBreakdown.substring(1);
                hashBreakdown = hashBreakdown.replaceAll("[" + this.seps + "]", " ");
                hashArray = hashBreakdown.split(" ");

                String subHash;
                String buffer;
                for (final String aHashArray : hashArray) {
                    subHash = aHashArray;
                    buffer = lottery + this.salt + alphabet;
                    alphabet = Hashids.consistentShuffle(alphabet, buffer.substring(0, alphabet.length()));
                    ret.add(Hashids.unhash(subHash, alphabet));
                }
            }
        }

        // transform from List<Long> to long[]
        long[] arr = new long[ret.size()];
        for (int k = 0; k < arr.length; k++) {
            arr[k] = ret.get(k);
        }

        if (!this.encode(arr).equals(hash)) {
            arr = new long[0];
        }

        return arr;
    }

    /**
     * Get Hashid algorithm version.
     *
     * @return Hashids algorithm version implemented.
     */
    public String getVersion() {
        return "1.0.0";
    }
}
