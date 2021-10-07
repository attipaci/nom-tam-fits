package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.LongStringsNotEnabledException;
import nom.tam.fits.LongValueException;
import nom.tam.fits.TruncatedFileException;
import nom.tam.fits.UnclosedQuoteException;
import nom.tam.fits.header.hierarch.BlanksDotHierarchKeyFormatter;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.ComplexValue;

public class HeaderCardTest {


    @Before
    public void before() {
        FitsFactory.setDefaults();
    }

    @After
    public void after() {
        FitsFactory.setDefaults();
    }

//    @Rule
//    public TestWatcher watcher = new TestWatcher() {
//        @Override
//        protected void starting(Description description) {
//            System.out.println("Starting test: " + description.getMethodName());
//        }
//    };

    @Test
    public void test1() throws Exception {

        HeaderCard p;
        p = HeaderCard.create("SIMPLE  =                     T");

        assertEquals("t1", "SIMPLE", p.getKey());
        assertEquals("t2", "T", p.getValue());
        assertNull("t3", p.getComment());

        p = HeaderCard.create("VALUE   =                   123");
        assertEquals("t4", "VALUE", p.getKey());
        assertEquals("t5", "123", p.getValue());
        assertNull("t3", p.getComment());

        p = HeaderCard.create("VALUE   =    1.23698789798798E23 / Comment ");
        assertEquals("t6", "VALUE", p.getKey());
        assertEquals("t7", "1.23698789798798E23", p.getValue());
        assertEquals("t8", "Comment", p.getComment());

        String lng = "111111111111111111111111111111111111111111111111111111111111111111111111";
        p = HeaderCard.create("COMMENT " + lng);
        assertEquals("t9", "COMMENT", p.getKey());
        assertNull("t10", p.getValue());
        assertEquals("t11", lng, p.getComment());
        FitsFactory.setAllowHeaderRepairs(false);
        boolean thrown = false;
        try {
            //
            p = HeaderCard.create("VALUE   = '   ");
        } catch (Exception e) {
            thrown = true;
        }
        assertEquals("t12", true, thrown);

        p = HeaderCard.create("COMMENT " + lng + lng);
        assertEquals("t13", lng, p.getComment());

        HeaderCard z = new HeaderCard("TTTT", 1.234567891234567891234567e101, "a comment");
        assertTrue("t14", z.toString().indexOf("E") > 0);        
    }

    @Test
    public void test3() throws Exception {

        HeaderCard p = new HeaderCard("KEY", "VALUE", "COMMENT");
        assertEquals("x1", "KEY     = 'VALUE   '           / COMMENT                                        ", p.toString());

        p = new HeaderCard("KEY", 123, "COMMENT");
        assertEquals("x2", "KEY     =                  123 / COMMENT                                        ", p.toString());
        p = new HeaderCard("KEY", 1.23, "COMMENT");
        assertEquals("x3", "KEY     =                 1.23 / COMMENT                                        ", p.toString());
        p = new HeaderCard("KEY", true, "COMMENT");
        assertEquals("x4", "KEY     =                    T / COMMENT                                        ", p.toString());

        boolean thrown = false;
        try {
            p = new HeaderCard("LONGKEYWORD", 123, "COMMENT");
        } catch (Exception e) {
            thrown = true;
        }
        assertEquals("x5", true, thrown);

        thrown = false;
        String lng = "00000000001111111111222222222233333333334444444444555555555566666666667777777777";
        try {
            Header.setLongStringsEnabled(false);
            p = new HeaderCard("KEY", lng, "COMMENT");
        } catch (Exception e) {
            thrown = true;
        }
        assertEquals("x6", true, thrown);

        // Only trailing spaces are stripped.
        p = new HeaderCard("STRING", "VALUE", null);
        assertEquals("x6", "VALUE", p.getValue());

        p = new HeaderCard("STRING", "VALUE ", null);
        assertEquals("x7", "VALUE", p.getValue());

        p = new HeaderCard("STRING", " VALUE", null);
        assertEquals("x8", " VALUE", p.getValue());

        p = new HeaderCard("STRING", " VALUE ", null);
        assertEquals("x9", " VALUE", p.getValue());

        p = new HeaderCard("QUOTES", "ABC'DEF", null);
        assertEquals("x10", "ABC'DEF", p.getValue());
        assertEquals("x10b", p.toString().indexOf("''") > 0, true);

        p = new HeaderCard("QUOTES", "ABC''DEF", null);
        assertEquals("x11", "ABC''DEF", p.getValue());
        assertEquals("x10b", p.toString().indexOf("''''") > 0, true);
    }

    @Test
    public void testDefault() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", (String) null, "dummy");
        assertEquals(Integer.valueOf(5), hc.getValue(int.class, 5));
    }
    
    @Test
    public void testHeaderBlanks() throws Exception {
        
        
        HeaderCard hc = HeaderCard.create("               ");
        assertEquals("", hc.getKey());
        assertNull(hc.getValue());
        assertNull(hc.getComment());
        
        hc = HeaderCard.create("=          ");
        assertEquals("", hc.getKey());
        assertNull(hc.getValue());
        assertEquals("=", hc.getComment());

        hc = HeaderCard.create("  =          ");
        assertEquals("", hc.getKey());
        assertNull(hc.getValue());
        assertEquals("=", hc.getComment());
        
        hc = HeaderCard.create("CARD       /          ");
        assertEquals("CARD", hc.getKey());
        assertNull(hc.getValue());
        assertEquals('/', hc.getComment().charAt(0));
        
        hc = HeaderCard.create("CARD = 123 /          ");
        assertEquals("CARD", hc.getKey());
        assertEquals("123", hc.getValue());
        assertEquals("", hc.getComment());
        
        hc = HeaderCard.create("CARD = 123 /          ");
        assertEquals("CARD", hc.getKey());
        assertEquals("123", hc.getValue());
        assertEquals("", hc.getComment());
        
        
        hc = HeaderCard.create("CONTINUE   /   ");
        assertEquals("CONTINUE", hc.getKey());
        assertEquals("", hc.getValue());
        assertEquals("", hc.getComment());
        
        hc = HeaderCard.create("CONTINUE 123  /   ");
        assertEquals("CONTINUE", hc.getKey());
        assertEquals("123", hc.getValue());
        assertEquals("", hc.getComment());
        
        hc = HeaderCard.create("CARD");
        assertEquals("CARD", hc.getKey());
        assertNull(hc.getValue());
        assertNull(hc.getComment());
        
        hc = HeaderCard.create("  = '         ");
        assertEquals("", hc.getKey());
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());
    }
    
    
    @Test
    public void testMissingEndQuotes() throws Exception {
        boolean thrown = false;
        HeaderCard hc = null;
        
        FitsFactory.setAllowHeaderRepairs(false);
        
        try {
            thrown = false;
            hc = HeaderCard.create("");
        } catch(IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
                 
        try {
            thrown = false;
            hc = HeaderCard.create("CONTINUE '         ");
        } catch(IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        try {
            thrown = false;
            hc = HeaderCard.create("CARD = '         ");
        } catch(IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        FitsFactory.setAllowHeaderRepairs(true);
              
        hc = HeaderCard.create("CONTINUE '         ");
        assertNotNull(hc.getValue());
        
        hc = HeaderCard.create("CONTINUE '      /   ");
        assertNotNull(hc.getValue());
        assertNull(hc.getComment());
        
        hc = HeaderCard.create("CARD = '         ");
        assertNotNull(hc.getValue());
        
        hc = HeaderCard.create("CARD = '       /  ");
        assertNotNull(hc.getValue());
        assertNull(hc.getComment());
    }
    
    @Test
    public void testMidQuotes() throws Exception {
        HeaderCard hc = HeaderCard.create("CARD = abc'def' /         ");
        assertEquals("abc'def'", hc.getValue());
        
        hc = HeaderCard.create("CONTINUE  abc'def' /         ");
        assertEquals("abc'def'", hc.getValue());
    }
    
    @Test
    public void testParseCornerCases() throws Exception {
        HeaderCard hc = HeaderCard.create("CARD = ''");
        assertEquals("", hc.getValue());
        
        // Last char is start of comment /
        byte[] bytes = hc.toString().getBytes();
        bytes[bytes.length- 1] = '/';
        hc = HeaderCard.create(new String(bytes));
        assertEquals("", hc.getComment());
    }
    
    public void testMisplacedEqual() throws Exception {
        FitsFactory.setUseHierarch(false);
        
        // Not a value because = is not in the first 9 chars...
        HeaderCard hc = HeaderCard.create("CARD       = 'value'");
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());
        
        // Hierarch without hierarchy, with equal in the wrong place...
        hc = HeaderCard.create("HIERARCH       = 'value'");
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());
        
        // Not a value because we aren't supporting hierarch convention
        hc = HeaderCard.create("HIERARCH TEST = 'value'");
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());
        
        FitsFactory.setUseHierarch(true);
        
        // Not a value because = is not in the first 9 chars, and it's not a HIERARCH card...
        hc = HeaderCard.create("CARD       = 'value'");
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());
        
        // Hierarch without hierarchy.
        hc = HeaderCard.create("HIERARCH       = 'value'");
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());
        
        // Proper hierarch
        hc = HeaderCard.create("HIERARCH TEST= 'value'");
        assertNotNull(hc.getValue());
        assertNull(hc.getComment());
    }
        
    @Test
    public void testBigDecimal1() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("12345678901234567890123456789012345678901234567890123456789012345678901234567.890"), "dummy");
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals("E76", hc.toString().substring(77));
        assertEquals(new BigInteger("12345678901234567890123456789012345678901234567890123456789012345700000000000"), hc.getValue(BigInteger.class, null));
        assertTrue(hc.toString().length() == 80);
    }

    @Test
    public void testBigDecimal2() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("123.66666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666"), "dummy");
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals('7', hc.toString().charAt(79));
        assertEquals(new BigDecimal("123.666666666666666666666666666666666666666666666666666666666666666667"), hc.getValue(BigDecimal.class, null));
        assertEquals(new Double("123.6666666666666667"), hc.getValue(Double.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testBigDecimal3() throws Exception {
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567.890123456789012345678901234567890"),
                "dummy");
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals("E96", hc.toString().substring(77));
        assertEquals(new BigInteger("1234567890123456789012345678901234567890123456789012345678901234570000000000000000000000000000000"), hc.getValue(BigInteger.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testBigDecimal4() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("123.0"), "dummy");
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals(new BigDecimal("123.0"), hc.getValue(BigDecimal.class, null));
        assertEquals(new Double("123.0"), hc.getValue(Double.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOther() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigDecimal("123.0"), "dummy");
        hc.getValue(HeaderCardTest.class, null);
    }

    @Test
    public void testBigInteger() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", new BigInteger("1234567890123456789012345678901234567890123456789012345678901234567890"), "dummy");
        assertEquals(BigInteger.class, hc.valueType());
        assertEquals("1234567890123456789012345678901234567890123456789012345678901234567890", hc.getValue());
        assertEquals(80, hc.toString().length());
        
        hc = new HeaderCard("TEST", new BigInteger("12345678901234567890123456789012345678901234567890123456789012345678901234567890"), "dummy");
        assertEquals(BigInteger.class, hc.valueType());
        assertEquals("1.23456789012345678901234567890123456789012345678901234567890123457E79", hc.getValue());
        assertEquals(new BigInteger("12345678901234567890123456789012345678901234567890123456789012345700000000000000"), hc.getValue(BigInteger.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testBoolean() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", true, "dummy");
        assertEquals(Boolean.class, hc.valueType());
        assertEquals(Boolean.TRUE, hc.getValue(Boolean.class, null));
        hc = new HeaderCard("TEST", false, "dummy");
        assertEquals(Boolean.class, hc.valueType());
        assertEquals(Boolean.FALSE, hc.getValue(Boolean.class, null));
        hc = new HeaderCard("TEST", 99, "dummy");
        assertEquals(Boolean.FALSE, hc.getValue(Boolean.class, Boolean.FALSE));
        assertEquals(Boolean.TRUE, hc.getValue(Boolean.class, Boolean.TRUE));
    }

    @Test
    public void testCardCopy() throws Exception {
        HeaderCard hc1 = new HeaderCard("TEST", 123.0, "dummy");
        HeaderCard hc2 = hc1.copy();
        
        assertEquals(hc2.getKey(), hc1.getKey());
        assertEquals(hc2.getValue(), hc1.getValue());
        assertEquals(hc2.getComment(), hc1.getComment());
        assertEquals(hc2.valueType(), hc1.valueType());
    }

    
    @Test
    public void testCardReread() throws Exception {
        HeaderCard hc1 = new HeaderCard("TEST", 123.0F, "dummy");
        HeaderCard hc2 = HeaderCard.create(hc1.toString());
        
        assertEquals(hc2.getKey(), hc1.getKey());
        assertEquals(hc2.getValue(), hc1.getValue());
        assertEquals(hc2.getComment(), hc1.getComment());
        assertEquals(hc2.valueType(), hc1.valueType());
    }

    @Test
    public void testHierarchFormatting() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard hc;
        hc = new HeaderCard("HIERARCH.TEST1.INT", "xx", "Comment");
        assertTrue(hc.toString().startsWith("HIERARCH TEST1 INT = 'xx' "));
        hc = new HeaderCard("HIERARCH.TEST1.TEST2.INT", "xx", "Comment");
        assertTrue(hc.toString().startsWith("HIERARCH TEST1 TEST2 INT = 'xx' "));
        hc = new HeaderCard("HIERARCH.TEST1.TEST3.B", "xx", "Comment");
        assertTrue(hc.toString().startsWith("HIERARCH TEST1 TEST3 B = 'xx' "));
    }

    @Test
    public void testHierarchTolerant() throws Exception {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        HeaderCard hc = HeaderCard.create("HIERARCH [xxx].@{ping}= xx / Comment");
        assertEquals("HIERARCH.[XXX].@{PING}", hc.getKey());
        assertEquals("xx", hc.getValue());
    }

    @Test
    public void testSimpleHierarch() throws Exception {
        FitsFactory.setUseHierarch(false);
        HeaderCard hc = HeaderCard.create("HIERARCH= 0.123 / Comment");
        assertEquals("HIERARCH", hc.getKey());
        assertEquals("0.123", hc.getValue());
        assertEquals("Comment", hc.getComment());
        assertEquals("HIERARCH=                0.123 / Comment", hc.toString().trim());
    }

    @Test
    public void testHierarch() throws Exception {

        HeaderCard hc;
        String key = "HIERARCH.TEST1.TEST2.INT";
        boolean thrown = false;
        
        FitsFactory.setUseHierarch(false);
        try {
            hc = new HeaderCard(key, 123, "Comment");
        } catch (Exception e) {
            thrown = true;
        }
        assertEquals("h1", true, thrown);

        String card = "HIERARCH TEST1 TEST2 INT=           123 / Comment                               ";
        hc = HeaderCard.create(card);
        assertEquals("h2", "HIERARCH", hc.getKey());
        assertNull("h3", hc.getValue());
        // its wrong because setUseHierarch -> false
        assertEquals("h4", "TEST1 TEST2 INT=           123 / Comment", hc.getComment());

        FitsFactory.setUseHierarch(true);

        hc = new HeaderCard(key, 123, "Comment");

        assertEquals("HIERARCH TEST1 TEST2 INT = 123 / Comment", hc.toString().trim());

        assertEquals("h5", key, hc.getKey());
        assertEquals("h6", "123", hc.getValue());
        assertEquals("h7", "Comment", hc.getComment());

        hc = HeaderCard.create(card);
        assertEquals("h8", key, hc.getKey());
        assertEquals("h9", "123", hc.getValue());
        assertEquals("h10", "Comment", hc.getComment());

        hc = HeaderCard.create("KEYWORD sderrfgre");
        assertNull("no-equals", hc.getValue());
        
        // now test a longString
        FitsFactory.setLongStringsEnabled(true);

        hc = new HeaderCard(key, "a verly long value that must be splitted over multiple lines to fit the card", "the comment is also not the smallest");

        assertEquals("HIERARCH TEST1 TEST2 INT = 'a verly long value that must be splitted over mult&'" + //
                "CONTINUE  'iple lines to fit the card' /the comment is also not the smallest    ", hc.toString());

    }
    
    @Test
    public void testHierarchMixedCase() throws Exception { 
        // The default is to use upper-case only for HIERARCH
        assertEquals(false, FitsFactory.getHierarchFormater().isCaseSensitive());
        
        int l = "HIERARCH abc DEF HiJ".length();
        
        HeaderCard hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        assertEquals("HIERARCH.ABC.DEF.HIJ", hc.getKey());
        assertEquals("HIERARCH ABC DEF HIJ", hc.toString().substring(0, l));
        
        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        assertEquals("HIERARCH ABC DEF HIJ", hc.toString().substring(0, l));
        
        FitsFactory.getHierarchFormater().setCaseSensitive(true);
        assertEquals(true, FitsFactory.getHierarchFormater().isCaseSensitive());
        
        hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        assertEquals("HIERARCH abc DEF HiJ", hc.toString().substring(0, l));
        
        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());    
        assertEquals("HIERARCH abc DEF HiJ", hc.toString().substring(0, l));
    }

    @Test
    public void testBlacksHierarchMixedCase() throws Exception { 
        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));
        
        // The default is to use upper-case only for HIERARCH
        assertEquals(false, FitsFactory.getHierarchFormater().isCaseSensitive());

        int l = "HIERARCH  abc.DEF.HiJ".length();
        
        HeaderCard hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        assertEquals("HIERARCH.ABC.DEF.HIJ", hc.getKey());
        assertEquals("HIERARCH  ABC.DEF.HIJ", hc.toString().substring(0, l));
        
        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        assertEquals("HIERARCH  ABC.DEF.HIJ", hc.toString().substring(0, l));
        
        FitsFactory.getHierarchFormater().setCaseSensitive(true);
        assertEquals(true, FitsFactory.getHierarchFormater().isCaseSensitive());
        
        hc = HeaderCard.create("HIERARCH abc DEF HiJ= 'something'");
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());
        assertEquals("HIERARCH  abc.DEF.HiJ", hc.toString().substring(0, l));
        
        hc = new HeaderCard("HIERARCH.abc.DEF.HiJ", "something", null);
        assertEquals("HIERARCH.abc.DEF.HiJ", hc.getKey());   
        assertEquals("HIERARCH  abc.DEF.HiJ", hc.toString().substring(0, l));
    }
    
    @Test
    public void testLongStringWithSkippedBlank() throws Exception {
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setSkipBlankAfterAssign(true);
        String key = "HIERARCH.TEST1.TEST2.INT";

        HeaderCard hc = new HeaderCard(key, "a verly long value that must be splitted over multiple lines to fit the card", "the comment is also not the smallest");

        assertEquals("HIERARCH TEST1 TEST2 INT ='a verly long value that must be splitted over multi&'" + //
                "CONTINUE  'ple lines to fit the card' /the comment is also not the smallest     ", hc.toString());

    }
    
    @Test
    public void testLongComment() throws Exception {
        String value = "value";
        String start = "This is a long comment with";
        String comment = start + "                                                                               wrapped spaces.";

        FitsFactory.setLongStringsEnabled(false);
        HeaderCard hc = new HeaderCard("TEST", value, comment);
        assertEquals(comment, hc.getComment());
      
        hc = HeaderCard.create(hc.toString());
        assertEquals(start, hc.getComment());
        
        FitsFactory.setLongStringsEnabled(true);
        hc = new HeaderCard("TEST", value, comment);
        assertEquals(comment, hc.getComment());
        hc = HeaderCard.create(hc.toString());
        assertEquals(comment, hc.getComment());
    }
    
    @Test
    public void testFakeLongCards() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        
        // Continue not with a string value...
        HeaderCard hc = HeaderCard.create("TEST   = '                                                                    &'" 
                    + "CONTINUE  not a string / whatever                                               ");
        assertEquals(1, hc.cardSize());
        
        // Continue, but no ending &
        hc = HeaderCard.create("TEST   = '                                                                     '" 
                + "CONTINUE  'a string' / whatever                                               ");
        assertEquals(1, hc.cardSize());
        
        // Ending &, but no CONTINUE
        hc = HeaderCard.create("TEST   = '                                                                     '" 
                + "COMMENT   'a string' / whatever                                               ");
        assertEquals(1, hc.cardSize());
    }
    
    public void testFakeHierarch()  throws Exception {
        FitsFactory.setUseHierarch(true);
        
        // Just a regular card.
        HeaderCard hc = HeaderCard.create("HIERARCH= 'value'");
        assertEquals("HIERARCH", hc.getKey());
        assertEquals("value", hc.getValue());
        assertNull(hc.getComment());
        
        // '=' in the wrong place
        hc = HeaderCard.create("HIERARCH = 'value'");
        assertEquals("HIERARCH", hc.getKey());
        assertNull(hc.getValue());
        assertNotNull(hc.getComment());
    }
    
    public void testChangeKey() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "value", "comment");
        HeaderCard hc2 = new HeaderCard("TEST", "long value ---------------------------------------------------------");
        HeaderCard hc3 = new HeaderCard("TEST", new BigInteger("1234567890123456789012345678901234567890123456789012345678901234567890"));
        
        FitsFactory.setUseHierarch(true);
        FitsFactory.setLongStringsEnabled(false);
        
        hc.changeKey("TEST1");
        assertEquals("TEST1", hc.getKey());
        
        boolean thrown = false;
        try {
            hc2.changeKey("HIERARCH.ZZZ");
        } catch(LongStringsNotEnabledException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            hc3.changeKey("HIERARCH.ZZZ");
        } catch(LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        FitsFactory.setLongStringsEnabled(true);
        
        hc.changeKey("TEST2");
        assertEquals("TEST2", hc.getKey());
        
        hc.changeKey("HIERARCH.ZZZ"); 
        assertTrue(hc.hasHierarchKey());
        assertEquals("HIERARCH.ZZZ", hc.getKey());
        
    }
        
    @Test
    public void testSanitize() throws Exception {
        String card = "CARD = 'abc\t\r\n\bdef'";
        String sanitized = "CARD    = 'abc????def'";
        HeaderCard hc = HeaderCard.create(card);
        assertEquals(sanitized, hc.toString().substring(0, sanitized.length()));
    }

    @Test
    public void testInt() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 9999);
        assertEquals(Integer.class, hc.valueType());
        assertEquals(Integer.valueOf(9999), hc.getValue(Integer.class, null));
        hc.setValue(9999);
        assertEquals(Integer.class, hc.valueType());
        assertEquals(Integer.valueOf(9999), hc.getValue(Integer.class, null));
        hc.setValue(-9999);
        assertEquals(Integer.class, hc.valueType());
        assertEquals(Integer.valueOf(-9999), hc.getValue(Integer.class, null));
    }

    @Test
    public void testLong() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", 999999999999999999L);
        assertEquals(Long.class, hc.valueType());
        assertEquals(Long.valueOf(999999999999999999L), hc.getValue(Long.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testLongDoubles() throws Exception {
        // Check to see if we make long double values
        // fit in the recommended space.
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890")
        );
        String val = hc.getValue();
        assertEquals("tld1", val.length(), 70);
        assertEquals(BigDecimal.class, hc.valueType());
        assertEquals(new BigInteger("123456789012345678901234567890123456789012345678901234567"), hc.getValue(BigDecimal.class, null).toBigInteger());
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_1() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", -123456.78905D, 6, "dummy");
        assertEquals(-1.234568E5, hc.getValue(Double.class, 0.0), 1.1);
        assertEquals(-123456.78905D, hc.getValue(Double.class, null), 0.11);
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_2() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", 123456.78905D, 2, "dummy");
        String val = hc.getValue();
        assertEquals("1.23E5", val);
        assertTrue(hc.toString().contains("E5"));
        assertEquals(123456.78905D, hc.getValue(Double.class, null), 1.1e4);
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_3() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", -0.000012345678905D, 6, "dummy");
        String val = hc.getValue();
        assertEquals("-1.234568E-5", val);
        assertTrue(hc.toString().contains("E-5"));
        assertEquals(-0.000012345678905D, hc.getValue(Double.class, null), 1.1e-11);
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificDoubles_4() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST", 0.000012345678905D, 6, "dummy");
        assertEquals("1.234568E-5", hc.getValue());
        assertTrue(hc.toString().contains("E-5"));
        assertEquals(0.000012345678905D, hc.getValue(Double.class, null), 1.1e-11);
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_1() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"),
                9,"dummy");  
        String val = hc.getValue();
        assertEquals("1.23456789D56", val);
        assertTrue(hc.toString().contains("D56"));
        assertEquals(new BigDecimal("1.23456789E56"), hc.getValue(BigDecimal.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_2() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("-123456789012345678901234567890123456789012345678901234567.8901234567890123456789012345678901234567890123456789012345678901234567890"),
                9,"dummy");
        String val = hc.getValue();
        assertEquals("-1.23456789D56", val);
        assertTrue(hc.toString().contains("D56"));
        assertEquals(new BigDecimal("-1.23456789E56"), hc.getValue(BigDecimal.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_3() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("0.000000000000000000000000001234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123"),
                9,"dummy");
        String val = hc.getValue();
        assertEquals("1.23456789D-27", val);
        assertTrue(hc.toString().contains("D-27"));
        assertEquals(new BigDecimal("1.23456789E-27"), hc.getValue(BigDecimal.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testScientificLongDoubles_4() throws Exception {
        FitsFactory.setUseExponentD(true);
        HeaderCard hc = new HeaderCard("TEST",
                new BigDecimal("-0.000000000000000000000000001234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123"),
                9,"dummy");
        String val = hc.getValue();
        
        assertEquals("-1.23456789D-27", val);
        assertTrue(hc.toString().contains("D-27"));
        assertEquals(new BigDecimal("-1.23456789E-27"), hc.getValue(BigDecimal.class, null));
        assertEquals(80, hc.toString().length());
    }

    @Test
    public void testString() throws Exception {
        HeaderCard hc = new HeaderCard("TEST", "bla bla", "dummy");
        assertEquals(String.class, hc.valueType());
        assertEquals("bla bla", hc.getValue(String.class, null));
    }

    @Test
    public void testCommentLine() throws Exception {
        HeaderCard hc = new HeaderCard("", "dummyafsdfasdfasfasdf", false);
        assertEquals(null, hc.valueType());
        // AK: empty spaces are not allowd in bytes 0-7 by the FITS standard. It was wrong we
        // allowed them. Instead, cards created with a null keyword should have COMMENT as the key
        //Assert.assertTrue(hc.toString().startsWith("        "));
        Assert.assertTrue(hc.toString().startsWith("        "));
    }

    @Test
    public void testStringQuotes() throws Exception {
        // Regular string value in FITS header
        HeaderCard hc = HeaderCard.create("TEST    = 'bla bla' / dummy");
        assertEquals(String.class, hc.valueType());
        assertEquals("bla bla", hc.getValue(String.class, null));

        // Quoted string in FITS with ''
        hc = HeaderCard.create("TEST    = '''bla'' bla' / dummy");
        assertEquals(String.class, hc.valueType());
        assertEquals("'bla' bla", hc.getValue(String.class, null));
        
        // Quotes in constructed value
        hc = new HeaderCard("TEST", "'bla' bla", "dummy");
        assertEquals("'bla' bla", hc.getValue(String.class, null));
       
        // Quotes in comment
        hc = HeaderCard.create("TEST    = / 'bla bla' dummy");
        assertEquals("", hc.getValue(String.class, null));
        
        // Unfinished quotes
        Exception ex = null;
        try {
            hc = HeaderCard.create("TEST    = 'bla bla / dummy");
        } catch (IllegalArgumentException e) {
            ex = e;
        }
        assertNotNull(ex);
        
        FitsFactory.setAllowHeaderRepairs(true);
        hc = HeaderCard.create("TEST    = 'bla bla / dummy");
        assertEquals("bla bla / dummy", hc.getValue(String.class, null));
    }

    @Test
    public void testCardSize() throws Exception {

        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setUseHierarch(true);

        HeaderCard hc = new HeaderCard("HIERARCH.TEST.TEST.TEST.TEST.TEST.TEST", //
                "bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla ",
                "dummy");
        assertEquals(4, hc.cardSize());
    }

    @Test
    public void testHierarchCard() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setUseHierarch(true);

        HeaderCard hc = new HeaderCard("HIERARCH.TEST.TEST.TEST.TEST.TEST.TEST", //
                "bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla ",
                " dummy");
        BufferedDataInputStream data = headerCardToStream(hc);
        HeaderCard headerCard = new HeaderCard(data);
        assertEquals(hc.getKey(), headerCard.getKey());
        assertEquals(hc.getValue(), headerCard.getValue());

    }

    protected BufferedDataInputStream headerCardToStream(HeaderCard hc) throws Exception {
        BufferedDataInputStream data = new BufferedDataInputStream(new ByteArrayInputStream(AsciiFuncs.getBytes(hc.toString())));
        return data;
    }

    @Test
    public void testHierarchAlternatives() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard headerCard = new HeaderCard("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", "xy", null);
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1 TEST2 TEST3 TEST4 TEST5 TEST6 = 'xy'                             ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 = 'xy'                             ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH  TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 = 'xy'                            ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

    }

    @Test
    public void testKeyWordNullability() throws Exception {
        assertEquals("TEST    = 'VALUE   '           / COMMENT                                        ", new HeaderCard("TEST", "VALUE", "COMMENT", true).toString());
        assertEquals("TEST    = 'VALUE   '           / COMMENT                                        ", new HeaderCard("TEST", "VALUE", "COMMENT", false).toString());
        assertEquals("TEST    =                      / COMMENT                                        ", new HeaderCard("TEST", null, "COMMENT", true).toString());
        // AK: Fixed because comment can start at or after byte 11 only!
        assertEquals("TEST      COMMENT                                                               ", new HeaderCard("TEST", null, "COMMENT", false).toString());
        HeaderCardException actual = null;
        try {
            new HeaderCard(null, "VALUE", "COMMENT", true);
        } catch (HeaderCardException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        assertEquals(true, new HeaderCard("TEST", "VALUE", "COMMENT", true).isKeyValuePair());
        assertEquals(true, new HeaderCard("TEST", "VALUE", "COMMENT", false).isKeyValuePair());
        assertEquals(false, new HeaderCard("TEST", null, "COMMENT", true).isKeyValuePair());
        assertEquals(false, new HeaderCard("TEST", null, "COMMENT", false).isKeyValuePair());

    }

    @Test
    public void testHierarchAlternativesWithSkippedBlank() throws Exception {
        FitsFactory.setSkipBlankAfterAssign(true);
        FitsFactory.setUseHierarch(true);
        HeaderCard headerCard = new HeaderCard("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", "xy", null);
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1 TEST2 TEST3 TEST4 TEST5 TEST6 ='xy'                              ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(1));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 ='xy'                              ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", headerCard.getKey());
        assertEquals("HIERARCH  TEST1.TEST2.TEST3.TEST4.TEST5.TEST6 ='xy'                             ", headerCard.toString());
        assertEquals("HIERARCH.TEST1.TEST2.TEST3.TEST4.TEST5.TEST6", new HeaderCard(headerCardToStream(headerCard)).getKey());

    }

    @Test
    public void testKeyWordNullabilityWithSkippedBlank() throws Exception {
        FitsFactory.setSkipBlankAfterAssign(true);
        assertEquals("TEST    ='VALUE    '           / COMMENT                                        ", new HeaderCard("TEST", "VALUE", "COMMENT", true).toString());
        assertEquals("TEST    ='VALUE    '           / COMMENT                                        ", new HeaderCard("TEST", "VALUE", "COMMENT", false).toString());
        assertEquals("TEST    =                      / COMMENT                                        ", new HeaderCard("TEST", null, "COMMENT", true).toString());
        // AK: Fixed because comment can start at or after byte 11 only!
        assertEquals("TEST      COMMENT                                                               ", new HeaderCard("TEST", null, "COMMENT", false).toString());
        HeaderCardException actual = null;
        try {
            new HeaderCard(null, "VALUE", "COMMENT", true);
        } catch (HeaderCardException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        assertEquals(true, new HeaderCard("TEST", "VALUE", "COMMENT", true).isKeyValuePair());
        assertEquals(true, new HeaderCard("TEST", "VALUE", "COMMENT", false).isKeyValuePair());
        assertEquals(false, new HeaderCard("TEST", null, "COMMENT", true).isKeyValuePair());
        assertEquals(false, new HeaderCard("TEST", null, "COMMENT", false).isKeyValuePair());

    }

    @Test(expected = TruncatedFileException.class)
    public void testTruncatedLine() throws Exception {
        new HeaderCard(new BufferedDataInputStream(new ByteArrayInputStream("TO_SHORT    ".getBytes())) {

            @Override
            public int read(byte[] obuf, int offset, int length) throws IOException {
                try {
                    return super.read(obuf, offset, length);
                } catch (Exception e) {
                    return 0;
                }
            }
        });
    }

    @Test
    public void testKeyWordCommentedValue() throws Exception {
        // the important thing is that the equals sign my not be at the 9
        // position
        String cardString = new HeaderCard("XX", null, "= COMMENT", false).toString();
        assertNotEquals('=', cardString.charAt(8));
        Assert.assertTrue(cardString.indexOf('=') > 8);
        HeaderCard card = HeaderCard.create(cardString);
        assertEquals("XX", card.getKey());
        assertNull(card.getValue());
        assertEquals("= COMMENT", card.getComment());
    }
    
    @Test
    public void testBigDecimalValueType() throws Exception {
        HeaderCard headerCard = new HeaderCard("XX", 1, null);
        headerCard.setValue(new BigDecimal("55555555555555555555.555555555555555"));
        Class<?> type = headerCard.valueType();
        assertEquals(BigDecimal.class, type);
        headerCard.setValue(55.55);
        type = headerCard.valueType();
        assertEquals(Double.class, type);
    }

    @Test
    public void testBigDIntegerValueType() throws Exception {
        HeaderCard headerCard = new HeaderCard("XX", 1, null);
        headerCard.setValue(new BigInteger("55555555555555555555555555555555555"));
        Class<?> type = headerCard.valueType();
        assertEquals(BigInteger.class, type);
        headerCard.setValue(5555);
        type = headerCard.valueType();
        assertEquals(Integer.class, type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeaderCardCreate() throws Exception {
        HeaderCard.create("");
    }

    public void testSimpleConstructors() throws Exception {        
        HeaderCard hc = new HeaderCard("TEST", true);
        assertEquals("TEST", hc.getKey());
        assertEquals("T", hc.getValue());
        assertEquals(true, hc.getValue(Boolean.class, false));
        assertNull(hc.getComment());
        
        hc = new HeaderCard("TEST", false);
        assertEquals("TEST", hc.getKey());
        assertEquals("T", hc.getValue());
        assertEquals(false, hc.getValue(Boolean.class, true));
        assertNull(hc.getComment());
        
        hc = new HeaderCard("TEST", 101);
        assertEquals("TEST", hc.getKey());
        assertTrue(hc.isIntegerType());
        assertFalse(hc.isDecimalType());
        assertEquals(Integer.class, hc.valueType());
        assertEquals(101, hc.getValue());
        assertEquals(101, hc.getValue(Integer.class, 0).intValue());
        assertEquals(101, hc.getValue(Long.class, 0L).intValue());
        assertEquals(101, hc.getValue(Short.class, (short) 0).intValue());
        assertEquals(101, hc.getValue(Byte.class, (byte) 0).intValue());
        assertEquals(101, hc.getValue(BigInteger.class, BigInteger.ZERO).intValue());
        assertNull(hc.getComment());
        
        hc = new HeaderCard("TEST", Math.PI);
        assertEquals("TEST", hc.getKey());
        assertEquals(Double.class, hc.valueType());
        assertFalse(hc.isIntegerType());
        assertTrue(hc.isDecimalType());
        assertEquals(Math.PI, hc.getValue());
        assertEquals(Math.PI, hc.getValue(Double.class, 0.0).doubleValue(), 1e-12);
        assertEquals(Math.PI, hc.getValue(Float.class, 0.0F).doubleValue(), 1e-6);
        assertEquals(Math.PI, hc.getValue(BigDecimal.class, BigDecimal.ZERO).doubleValue(), 1e-12);
        assertNull(hc.getComment());
       
        
        hc = new HeaderCard("TEST", new ComplexValue(1.0, -2.0));
        assertEquals("TEST", hc.getKey());
        assertEquals(ComplexValue.class, hc.valueType());
        assertFalse(hc.isIntegerType());
        assertFalse(hc.isDecimalType());        
        assertNull(hc.getComment());
        
        hc = new HeaderCard("TEST", "string value");
        assertEquals("TEST", hc.getKey());
        assertEquals(String.class, hc.valueType());
        assertEquals("string value", hc.getValue());
        assertFalse(hc.isIntegerType());
        assertFalse(hc.isDecimalType()); 
        assertTrue(hc.isStringValue());
        assertNull(hc.getComment());
    }
    
    
    public void testSetValue() throws Exception {    
        HeaderCard hc = new HeaderCard("TEST", "value");
        
        int i = 20211006;
        hc.setHexValue(i);
        assertEquals(Integer.class, hc.valueType());
        assertEquals(i, hc.getValue(Integer.class, 0).intValue());
        
        long l = 202110062256L;
        hc.setHexValue(l);
        assertEquals(Long.class, hc.valueType());
        assertEquals(l, hc.getValue(Long.class, 0L).longValue());
        
        BigInteger big = new BigInteger("12345678901234567890");
        hc.setValue(big);
        assertEquals(BigInteger.class, hc.valueType());
        assertEquals(big, hc.getValue(BigInteger.class, BigInteger.ZERO));
    }
    
    public void testSetValueExcept() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard hc = new HeaderCard("HIERARCH.ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", 0);
        
        boolean thrown = false;

        try {
            int i = 20211006;
            hc.setHexValue(i);
        } catch(LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            long l = 202110062256L;
            hc.setHexValue(l);
        } catch(LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            BigInteger big = new BigInteger("12345678901234567890");
            hc.setValue(big);
        } catch(LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            BigInteger big = new BigInteger("12345678901234567890", 1);
            hc.setValue(big);
        } catch(LongValueException e) {
            thrown = true;
        }
        assertFalse(thrown);
    }
    
        
    @Test()
    public void testParseDExponent() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST   = 1.53E4");
        assertEquals(Float.class, hc.valueType());
        
        hc = HeaderCard.create("TEST   = 1.53D4");
        assertEquals(Double.class, hc.valueType());
    }
    
    @Test
    public void testEmptyNonString() throws Exception {
        HeaderCard hc = HeaderCard.create("TEST=     / comment");
        assertEquals("", hc.getValue());
        assertNotNull(hc.getComment());
    }
    
    @Test
    public void testJunkAfterStringValue() throws Exception {
        FitsFactory.setAllowHeaderRepairs(false);
        HeaderCard hc = HeaderCard.create("TEST= 'value' junk    / comment");
        assertNull(hc.getComment());
        
        FitsFactory.setAllowHeaderRepairs(true);
        hc = HeaderCard.create("TEST= 'value' junk    / comment");
        assertTrue(hc.getComment().startsWith("junk"));
    }
    
    @Test()
    public void testHeaderCardFormat() throws Exception {
        HeaderCard card = HeaderCard.create("TIMESYS = 'UTC ' / All dates are in UTC time");
        FitsFactory.setSkipBlankAfterAssign(true);
        assertEquals("UTC", card.getValue());
        assertEquals("All dates are in UTC time", card.getComment());
        assertEquals("TIMESYS", card.getKey());
        assertEquals("TIMESYS ='UTC      '           / All dates are in UTC time                      ", card.toString());

        card = HeaderCard.create("TIMESYS ='UTC ' / All dates are in UTC time");
        assertEquals("UTC", card.getValue());
        assertEquals("All dates are in UTC time", card.getComment());
        assertEquals("TIMESYS", card.getKey());
        assertEquals("TIMESYS ='UTC      '           / All dates are in UTC time                      ", card.toString());
    }

    @Test()
    public void testHeaderCardFormatHierarch() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard card = HeaderCard.create("HIERARCH TIMESYS.BBBB.CCCC = 'UTC ' / All dates are in UTC time");
        FitsFactory.setSkipBlankAfterAssign(true);
        assertEquals("UTC", card.getValue());
        assertEquals("All dates are in UTC time", card.getComment());
        assertEquals("HIERARCH.TIMESYS.BBBB.CCCC", card.getKey());
        assertEquals("HIERARCH TIMESYS BBBB CCCC ='UTC' / All dates are in UTC time                   ", card.toString());

        card = HeaderCard.create("HIERARCH TIMESYS.BBBB.CCCC ='UTC ' / All dates are in UTC time");
        assertEquals("UTC", card.getValue());
        assertEquals("All dates are in UTC time", card.getComment());
        assertEquals("HIERARCH.TIMESYS.BBBB.CCCC", card.getKey());
        assertEquals("HIERARCH TIMESYS BBBB CCCC ='UTC' / All dates are in UTC time                   ", card.toString());
    }
    
    
    
}
