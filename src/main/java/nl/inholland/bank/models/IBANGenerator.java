package nl.inholland.bank.models;

import org.apache.commons.validator.routines.IBANValidator;
import org.iban4j.*;


public class IBANGenerator {
    /**
     * Generates a random IBAN with the standard (NLxxINHO0xxxxxxxxx).
     * @return Returns a randomly generated IBAN.
     */
    public static Iban generateIban()
    {
        CountryCode countryCode = CountryCode.NL;
        String bankCode = "INHO";
        return new Iban.Builder().countryCode(countryCode).bankCode(bankCode).buildRandom();
    }

    /**
     * Checks if the given IBAN is valid.
     * @param iban The IBAN to check.
     * @return Returns a true or false statement.
     */
    public static boolean isValidIBAN(String iban)
    {
        if (iban != null) {
           IBANValidator ibanValidator = new IBANValidator();
           return ibanValidator.isValid(iban);
        }
        return false;
    }
}
