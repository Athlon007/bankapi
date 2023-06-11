package nl.inholland.bank.models;

import org.apache.commons.validator.routines.IBANValidator;
import org.iban4j.CountryCode;
import org.iban4j.Iban;


public class IBANGenerator {
    /**
     * Generates a random IBAN with the standard (NLxxINHO0xxxxxxxxx).
     * @return Returns a randomly generated IBAN.
     */
    public static Iban generateIBAN()
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
            if (iban.equals("NL01INHO0000000001")) { // Bank IBAN
                return true;
            }

           IBANValidator ibanValidator = new IBANValidator();
           return ibanValidator.isValid(iban);
        }
        return false;
    }
}
