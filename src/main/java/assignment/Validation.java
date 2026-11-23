package assignment;

// Input checks shared by the registration and edit forms.
public final class Validation {
    private Validation() {}

    // Data files are pipe-delimited, one record per line, so a '|' or line break inside a value would
    // split it into extra columns (corrupting the record or injecting fields such as a different role).
    public static boolean hasDelimiterChars(String... values) {
        for (String v : values) {
            if (v != null && (v.indexOf('|') >= 0 || v.indexOf('\n') >= 0 || v.indexOf('\r') >= 0)) return true;
        }
        return false;
    }

    public static final String DELIMITER_MESSAGE = "The characters '|' and line breaks are not allowed in any field.";
}
