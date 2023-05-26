package nl.inholland.bank.models;

import java.math.BigInteger;

public class IBANGenerator {
    public static void main(String[] args) {
        String countryCode = "NL";
        String bankCode = "ABNA";
        String accountNumber = "12345678901234";

        String generatedIBAN = generateIBAN(countryCode, bankCode, accountNumber);
        System.out.println("Generated IBAN: " + generatedIBAN);
    }

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
}
