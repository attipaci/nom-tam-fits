package nom.tam.util;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.LongValueException;
import nom.tam.fits.HeaderCardException;

import java.math.BigInteger;
import java.util.Random;

public class ComplexValueTest {
    
    @Before
    public void before() {
        FitsFactory.setDefaults();
    }

    @After
    public void after() {
        FitsFactory.setDefaults();
    }
    
    @Test
    public void testComplex() throws Exception {
        Random random = new Random();
        FlexFormat f = new FlexFormat();
        ComplexValue z;
        
        for(int i=0; i<100; i++) {
            double re = random.nextDouble();
            double im = random.nextDouble();
            int decimals = random.nextInt(FlexFormat.DOUBLE_DECIMALS);
            
            z = new ComplexValue(re, im);
            assertEquals("re", re, z.re(), 1e-12 * re);
            assertEquals("im", im, z.im(), 1e-12 * im);
        
            assertTrue("isFinite", z.isFinite());
            
            String s = "(" + re + "," + im + ")";
            assertEquals("toString", s, z.toString());
           
            s = z.toBoundedString(25);
            assertNotNull("toBoundedString1", s);
            assertTrue(!s.isEmpty());
            assertTrue(s.length() <= 25);
            
            f.setPrecision(decimals);
            s = "(" + f.format(re) + "," + f.format(im) + ")";
            assertEquals("toString(" + decimals + ")", s, z.toString(decimals));
        }
        
        
        z = new ComplexValue(Double.NaN, -1.0);
        assertFalse("NaN1", z.isFinite());
        
        z = new ComplexValue(Math.PI, Double.NaN);
        assertFalse("NaN2", z.isFinite());
        
        z = new ComplexValue(Double.POSITIVE_INFINITY, -1.0);
        assertFalse("Inf1", z.isFinite());
        
        z = new ComplexValue(Math.PI, Double.NEGATIVE_INFINITY);
        assertFalse("Inf2", z.isFinite());
        
        z = new ComplexValue(Double.NaN, Double.NEGATIVE_INFINITY);
        assertFalse("NaN/Inf", z.isFinite());
        
        boolean thrown = false;
        
        try {
            z = new ComplexValue(1.0, -2.0);
            // No ComplexValue should be printable in 4 characters...
            z.toBoundedString(4);
        } catch (LongValueException e) {
            thrown = true;
        }
        assertTrue("toBoundedString1", thrown);
        
        thrown = false;
        try {
            z = new ComplexValue(1.1, 2.1);
            // Should be printable in 9 characters "(1.1,2.1)
            z.toBoundedString(9);
        } catch (LongValueException e) {
            thrown = true;
        }
        assertFalse("toBoundedString2", thrown);
        
        thrown = false;
        try {
            z = new ComplexValue(1.1, -2.1);
            // Should require at least 10 characters "(1.1,-2.1)
            z.toBoundedString(9);
            thrown = true;
        } catch (LongValueException e) {
            thrown = true;
        }
        assertTrue("toBoundedString3", thrown);
    }
    
    public void testComplexFromString() throws Exception {
        // Missing brackets
        ComplexValue z = new ComplexValue("(5566.2,-1123.1)");
        assertEquals(5566.2, z.re(), 1-10);
        assertEquals(-1123.1, z.im(), 1-10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBadComplex1() throws Exception {
        // Missing brackets
        new ComplexValue("5566.2,-1123.1"); 
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBadComplex2() throws Exception {
        // Missing closing bracket
        new ComplexValue("(5566.2,-1123.1"); 
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBadComplex3() throws Exception {
        // Missing closing bracket
        new ComplexValue("(5566.2,-112#.1)"); 
    }
    
    
    @Test
    public void testComplexCard() throws Exception {
        HeaderCard hc = new HeaderCard("COMPLEX", new ComplexValue(1.0, -2.0));

        assertEquals(ComplexValue.class, hc.valueType());
        
        ComplexValue z = hc.getValue(ComplexValue.class, null);
        assertNotNull(z);
        
        assertEquals(1.0, z.re(), 1e-12);
        assertEquals(-2.0, z.im(), 1e-12);
    }
    
    @Test
    public void testComplexCard2() throws Exception {
        HeaderCard hc = HeaderCard.create("COMPLEX=   (1.0, -2.0)");

        assertEquals(ComplexValue.class, hc.valueType());
        
        ComplexValue z = hc.getValue(ComplexValue.class, null);
        assertNotNull(z);
        
        assertEquals(1.0, z.re(), 1e-12);
        assertEquals(-2.0, z.im(), 1e-12);
    }
    
    @Test(expected = HeaderCardException.class)
    public void testComplexNaNCard() throws Exception {
        new HeaderCard("COMPLEX", new ComplexValue(Double.NaN, -2.0));
    }
    
    public void testComplexNaNSetCard() throws Exception {
        HeaderCard hc = new HeaderCard("COMPLEX", new ComplexValue(1.0, -2.0));
        
        boolean thrown = false;
        try {
            hc.setValue(new ComplexValue(Double.NaN, -2.0));
        } catch (NumberFormatException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test(expected = HeaderCardException.class)
    public void testNoSpaceComplexCard() throws Exception {
        FitsFactory.setUseHierarch(true);
        new HeaderCard("HIERARCH.SOME.VERY.LONG.COMPLEX.KEYWORD.TAKING.UP.THE.SPACE", 
                new ComplexValue(Math.PI, -Math.PI), 16, "comment");
    }
    
    public void testNoSpaceComplexValue() throws Exception {
        FitsFactory.setUseHierarch(true);
        HeaderCard hc = null;
        
        try {
            hc = new HeaderCard("HIERARCH.SOME.VERY.LONG.COMPLEX.KEYWORD.TAKING.UP.THE.SPACE", true, "comment");
            hc.setValue(new ComplexValue(0.0, 0.0));
        } catch(HeaderCardException e) {
            
        }
        
        assertNotNull(hc);
        
        boolean thrown = false;
        
        try {
            // This should throw an expcetion as there is no space for the value...
            hc.setValue(new ComplexValue(Math.PI, -Math.PI), 16);
        } catch(LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }
    

    public void flexFormatTest() throws Exception {
        FlexFormat f = new FlexFormat();
        
        int i = 1001;
        BigInteger bigi = new BigInteger("12345678901234567890");
        
        f.setPrecision(FlexFormat.DOUBLE_DECIMALS);
        assertEquals(FlexFormat.DOUBLE_DECIMALS, f.getPrecision());
        
        f.setPrecision(FlexFormat.FLOAT_DECIMALS);
        assertEquals(FlexFormat.FLOAT_DECIMALS, f.getPrecision());
        
        f.flexPrecision();
        assertEquals(FlexFormat.FLEX_PRECISION, f.getPrecision());
        
        f.setPrecision(-11233);
        assertEquals(FlexFormat.FLEX_PRECISION, f.getPrecision());
        
        f.setWidth(80);
        assertEquals(80, f.getWidth());
        
        String s = f.format(Math.PI);
        assertEquals(Math.PI, Double.parseDouble(s), 1e-12);
  
        s = f.format(1e12 * Math.PI);
        assertEquals(1e12 * Math.PI, Double.parseDouble(s), 1.0);
        
        s = f.format(1e-12 * Math.PI);
        assertEquals(1e-12 * Math.PI, Double.parseDouble(s), 1e-24);
        
        s = f.format(i);
        assertEquals(i, Integer.parseInt(s));
        
        s = f.format(bigi);
        assertEquals(bigi, new BigInteger(s));
        
        f.setWidth(10);
        assertEquals(10, f.getWidth());
        
        
        s = f.format(Math.PI);
        assertEquals(Math.PI, Double.parseDouble(s), 1e-6);
        
        s = f.format(1e12 * Math.PI);
        assertEquals(1e12 * Math.PI, Double.parseDouble(s), 1e6);
        
        s = f.format(1e-12 * Math.PI);
        assertEquals(1e-12 * Math.PI, Double.parseDouble(s), 1e-16);
        
        s = f.format(i);
        assertEquals(i, Integer.parseInt(s));
        
        s = f.format(bigi);
        assertEquals(bigi.doubleValue(), Double.parseDouble(s), 1e15);
        
        f.setWidth(-100);
        assertEquals(0, f.getWidth());
        
        boolean thrown = false;
        try {
            s = f.format(Math.PI);
            assertEquals(Math.PI, Double.parseDouble(s), 1e-6);
        } catch(LongValueException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
   
    }

}
