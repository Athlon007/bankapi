package nl.inholland.bank.models;

import org.apache.commons.validator.routines.IBANValidator;
import org.iban4j.*;

public class IBANGenerator {
    /**
     * Generates a random IBAN.
     * @param countryCode Nullable. The country code to use.
     * @param bankCode Nullable. The bankcode to use. Can only be used in combination with countryCode.
     * @return Returns a randomly generated IBAN.
     */
    public static Iban generateIban(CountryCode countryCode, String bankCode)
    {
        if (countryCode != null && bankCode != null)
        {
            return new Iban.Builder().countryCode(countryCode).bankCode(bankCode).buildRandom();
        } else if (countryCode != null) {
            return Iban.random(countryCode);
        } else {
            return Iban.random();
        }
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
