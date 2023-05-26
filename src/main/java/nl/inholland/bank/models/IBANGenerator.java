package nl.inholland.bank.models;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class IBANGenerator {
    /**
     * Generates a new IBAN
     * @param countryCode The country code. Example "NL"
     * @param bankCode The bank code. Example "ABNA"
     * @param accountNumber The account number. Example "12345678901234"
     * @return Returns a String with the generated IBAN.
     */
    public static String generateIBAN(String countryCode, String bankCode, String accountNumber) {
        String iban = countryCode + "00" + bankCode + accountNumber;

        // Convert iban to be fully numeric.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iban.length(); i++) {
            char c = iban.charAt(i);
            if (Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c) - 'A' + 10);
            } else {
                sb.append(c);
            }
        }

        iban = sb.toString();

        // Mod value is the modulus value in IBAN generations.
        int modValue = 97;
        BigInteger ibanNumber = new BigInteger(iban);

        // Calculate remainder.
        int remainder = ibanNumber.mod(BigInteger.valueOf(modValue)).intValue();
        // Number for validity, it's the number that makes the iban divisible by 97.
        int checkDigit = modValue - remainder;

        return countryCode + String.format("%02d", checkDigit) + bankCode + accountNumber;
    }

    public static boolean isValidIBAN(String iban)
    {
        if (iban != null) {
            // Remove whitespace and convert to uppercase
            iban = iban.replaceAll("\\s+", "").toUpperCase();

            // Check if the IBAN length is valid
            if (iban.length() < 2 || iban.length() > 34) {
                return false;
            }

            // Extract the country code and check if it is valid
            String countryCode = iban.substring(0, 2);
            if (!isValidCountryCode(countryCode)) {
                return false;
            }

            // Move the first 4 characters to the end
            iban = iban.substring(4) + iban.substring(0, 4);

            // Convert letters to digits (A = 10, B = 11, etc.)
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < iban.length(); i++) {
                char c = iban.charAt(i);
                if (Character.isLetter(c)) {
                    sb.append(Character.getNumericValue(c));
                } else {
                    sb.append(c);
                }
            }
            iban = sb.toString();

            // Perform modulus-97 operation
            BigInteger ibanNumber = new BigInteger(iban);
            return ibanNumber.mod(BigInteger.valueOf(97)).intValue() == 1;
        }
        return false;
    }

    private static boolean isValidCountryCode(String countryCode) {
        List<String> validCountryCodes = Arrays.asList("GB", "DE", "FR", "NL");

        return validCountryCodes.contains(countryCode);
    }
}
