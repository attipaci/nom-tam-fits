package nom.tam.fits.utilities;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
 * %%
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * #L%
 */

import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;

import static nom.tam.fits.header.NonStandard.CONTINUE;
import static nom.tam.fits.header.NonStandard.HIERARCH;

/**
 * A helper utility class to parse header cards for there value (especially
 * strings) and comments.
 *
 * @author Attila Kovacs
 * @author Richard van Nieuwenhoven
 */
public class FitsHeaderLineParser {

    /** For logging */
    private static final Logger LOG = Logger.getLogger(Header.class.getName());

    /** The header line (usually 80-character width), which to parse. */
    private String line;
    
    /**
     * the value of the card. (trimmed and standardized with . in HIERARCH)
     */
    private String key = null;

    /**
     * the value of the card. (trimmed)
     */
    private String value = null;

    /**
     * the comment specified with the value.
     */
    private String comment = null;

    /**
     * was the value quoted?
     */
    private boolean isString = false;

    /**
     * The position in the string that right after the last character processed by this parser
     */
    private int parsePos = 0;

    /**
     * Instantiates a new parser for a FITS header line.
     * 
     * @param line  a line in the FITS header, normally exactly 80-characters wide (but need not be).'
     * 
     * @see #getKey()
     * @see #getValue()
     * @see #getComment()
     * @see #isString()
     * 
     * @throws IllegalArgumentException     if there is a missing end-quote and header repairs aren't allowed.
     * 
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     */
    public FitsHeaderLineParser(String line) throws IllegalArgumentException {
        this.line = line;
        parseKey();
        parseValue();
        parseComment();
    }

    /**
     * Returns the keyword component of the parsed header line. If the processing of HIERARCH
     * keywords is enabled, it may be a `HIERARCH` style long key with the components separated
     * by dots (e.g. `HIERARCH.ORG.SYSTEM.SUBSYS.ELEMENT`). Otherwise, it will be a standard
     * 0--8 character standard uppercase FITS keyword (including simply `HIERARCH` if 
     * {@link FitsFactory#setUseHierarch(boolean)} was set <code>false</code>).
     * 
     * @return the FITS header keyword for the line.
     * 
     * @see FitsFactory#setUseHierarch(boolean)
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Returns the value component of the parsed header line.
     * 
     * @return the value part of the line or <code>null</code> if the line contained no value.
     * 
     *  @see FitsFactory#setUseHierarch(boolean)
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the comment component of the parsed header line.
     * 
     * @return the comment part of the line or <code>null</code> if the line contained no comment.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Returns whether the line contained a quoted string value. By default, strings with missing end
     * quotes are no considered string values, but rather as comments. To allow processing lines
     * with missing quotes as string values, you must set {@link FitsFactory#setAllowHeaderRepairs(boolean)}
     * to <code>true</code> prior to parsing a header line with the missing end quote.
     * 
     * @return true if the value was quoted.
     * 
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     */
    public boolean isString() {
        return this.isString;
    }



    /**
     * Parses a fits keyword from a card and standardizes it (trim, uppercase, and hierarch with dots).
     *
     */
    private void parseKey() {
        /*
         * AK: The parsing of headers should never be stricter that the writing,
         * such that any header written by this library can be parsed back
         * without errors. (And, if anything, the parsing should be more
         * permissive to allow reading FITS produced by other libraries, which
         * may be less stringent in their rules). The original implementation
         * strongly enforced the ESO HIERARCH convention when reading, but not
         * at all for writing. Here is a tolerant hierarch parser that will
         * read back any hierarch key that was written by this library. The input 
         * FITS can use any space or even '.' to separate the hierarchies, and 
         * the hierarchical elements may contain any ASCII characters other than
         * those used for separating. It is more in line with what we do with 
         * standard keys too.
         */

        // Find the '=' in the line, if any...
        int iEq = line.indexOf('=');

        // The stem is in the first 8 characters or what precedes an '=' character
        // before that.
        int endStem = (iEq >= 0 && iEq <= HeaderCard.MAX_KEYWORD_LENGTH) ? iEq : HeaderCard.MAX_KEYWORD_LENGTH;

        // Find the key stem of the long key.
        String stem = line.substring(0, endStem).trim().toUpperCase(Locale.US);

        key = stem;
        parsePos = endStem;

        // If not using HIERARCH, then be very resilient, and return whatever key the first 8 chars make...
        if (!FitsFactory.getUseHierarch()) {
            return;
        }

        // If the line does not have an '=', can only be a simple key
        if (iEq < 0) {
            return;
        }

        // If it's not a HIERARCH keyword, then return an empty key.
        if (!stem.equals(HIERARCH.key())) {
            return;
        }

        // Compose the hierarchical key...
        StringTokenizer tokens = new StringTokenizer(line.substring(stem.length(), iEq), " \t\r\n.");
        StringBuilder builder = new StringBuilder(stem);

        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();

            parsePos = line.indexOf(token, parsePos) + token.length();

            // Add a . to separate hierarchies
            builder.append('.');
            builder.append(token);
        }
        
        key = builder.toString();
        
        if (!FitsFactory.getHierarchFormater().isCaseSensitive()) {
            key = key.toUpperCase(Locale.US);
        }
    }

    /**
     * Advances the parse position to skip any spaces at the current parse position, and returns whether there
     * is anything left in the line after the spaces...
     * 
     * @return      <code>true</code> if there is more non-space characters in the string, otherwise <code>false</code>
     */
    private boolean skipSpaces() {
        for (; parsePos < line.length(); parsePos++) {
            if (!Character.isSpaceChar(line.charAt(parsePos))) {
                // Line has non-space characters left to parse...
                return true;
            }
        }
        // nothing left to parse.
        return false;
    }

    /**
     * Parses the comment components starting for the current parse position. After this call the parse position
     * is set to the end of the string. The leading '/' (if found) is not included in the comment.
     * 
     */
    private void parseComment() {
        if (!skipSpaces()) {
            // nothing left to parse.
            return;
        }

        if (line.charAt(parsePos) == '/') {
            if (++parsePos >= line.length()) {
                // empty comment
                comment = "";
                return;
            }
        }

        comment = line.substring(parsePos).trim();        
        parsePos = line.length();
    }



    /**
     * Parses the value component from the current parse position. The parse position is advanced to
     * the first character after the value specification in the line. If the header line does
     * not contain a value component, then the value field of this object is set to <code>null</code>.
     * 
     * @throws IllegalArgumentException     if there is a missing end-quote and header repairs aren't allowed.
     * 
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     */
    private void parseValue() {
        if (key.isEmpty()) {
            // the entire line is a comment.
            return;
        }

        if (!skipSpaces()) {
            // nothing left to parse.
            return;
        }

        if (CONTINUE.key().equals(key)) {
            parseValueBody();
        } else if (line.charAt(parsePos) == '=') {
            if (parsePos > HeaderCard.MAX_KEYWORD_LENGTH) {
                // equal sign = after the 9th char -- only supported with hierarch keys...
                if (!key.startsWith(HIERARCH.key())) {
                    // It's not a HIERARCH key
                    return;
                }
                if (HIERARCH.key().equals(key)) {
                    // The key is only HIERARCH, without a hierarchical keyword after it...
                    return;
                }
            }

            parsePos++;
            parseValueBody();
        }
    }

    /**
     * Parses the value body from the current parse position. The parse position is advanced to
     * the first character after the value specification in the line. If the header line does
     * not contain a value component, then the value field of this object is set to <code>null</code>.
     * 
     * @throws IllegalArgumentException     if there is a missing end-quote and header repairs aren't allowed.
     * 
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     */
    private void parseValueBody() throws IllegalArgumentException {
        if (!skipSpaces()) {
            // nothing left to parse.
            return;
        }
       
        if (isNextQuote()) {
            // Parse as a string value, or else throw an exception.
            parseStringValue();
        } else {
            int end = line.indexOf('/', parsePos);
            if (end < 0) {
                end = line.length();     
            }
            value = line.substring(parsePos, end).trim();
            parsePos = end;
        }

    }

    /**
     * Checks if the next character, at the current parse position, is a single quote.
     * 
     * @return  <code>true</code> if the next character on the line exists and is a single quote, otherwise <code>false</code>.
     */
    private boolean isNextQuote() {
        if (parsePos >= line.length()) {
            // nothing left to parse.
            return false;
        }
        return line.charAt(parsePos) == '\'';
    }

    /**
     * Returns the string fom a parsed string value component, with trailing spaces removed. It preserves
     * leading spaces.
     * 
     * @param buf   the parsed string value.
     * @return      the string value with trailing spaces removed.
     */
    private String getString(StringBuilder buf) {  
        int to = buf.length();

        // Remove trailing spaces only!
        while (--to >= 0) {
            if (!Character.isSpaceChar(buf.charAt(to))) {
                break;
            }
        }

        isString = true;
        return to < 0 ? "" : buf.substring(0, to + 1);
    }

    /**
     * Parses a quoted string value starting at the current parse position. If successful, the parse
     * position is updated to after the string. Otherwise, the parse position is advanced only to skip 
     * leading spaces starting from the input position.
     * 
     * @throws IllegalArgumentException     if there is a missing end-quote and header repairs aren't allowed.
     * 
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     */
    private void parseStringValue() throws IllegalArgumentException {
        // In case the string parsing fails, we'll reset the parse position to where we
        // started.
        int from = parsePos++;

        StringBuilder buf = new StringBuilder(HeaderCard.MAX_VALUE_LENGTH);

        // Build the string value, up to the end quote and paying attention to double
        // quotes inside the string, which are translated to single quotes within
        // the string value itself.
        for (; parsePos < line.length(); parsePos++) {
            if (isNextQuote()) {
                parsePos++;
                
                if (isNextQuote()) {
                    // Quoted quote...
                    buf.append('\'');
                } else {
                    // Closing single quote.
                    value = getString(buf);
                    return;
                }
            } else {
                buf.append(line.charAt(parsePos));
            }
        }

        // String with missing end quote
        if (FitsFactory.isAllowHeaderRepairs()) {
            LOG.warning("Ignored missing end quote in " + getKey() + "!");
            value = getString(buf);
        } else {
            value = null;
            parsePos = from;
            throw new IllegalArgumentException("Missing or unexpected single quotes in value");
        }
    }


}
