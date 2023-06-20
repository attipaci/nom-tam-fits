package nom.tam.fits;

/*-
 * #%L
 * nom.tam.fits
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class BinaryTableNewTest {

    @Test
    public void testSetNumberByteColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new byte[] {1, 2, 3});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Byte.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberShortColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new short[] {1, 2, 3});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Short.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberIntColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1, 2, 3});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Integer.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberLongColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new long[] {1, 2, 3});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Long.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberFloatColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new float[] {1, 2, 3});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Float.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberDoubleColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new double[] {1, 2, 3});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
        Assert.assertEquals(-1.0, tab.getDouble(0, 0), 1e-12);
        Assert.assertEquals(Double.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetNumberBooleanColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new Boolean[] {false, true, false});
        tab.set(0, 0, -1);
        tab.set(1, 0, 0);
        tab.set(2, 0, Double.NaN);
        Assert.assertTrue(tab.getLogical(0, 0));
        Assert.assertFalse(tab.getLogical(1, 0));
        Assert.assertNull(tab.getLogical(2, 0));
    }

    @Test
    public void testSetNumberStringColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new String[] {"abcdef", "abcdef", "abcdef"});
        tab.set(0, 0, -1);
        Assert.assertEquals(-1L, tab.getLong(0, 0));
    }

    @Test
    public void testSetLogicalByteColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new byte[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(Byte.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalShortColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new short[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(Short.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalIntColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(Integer.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalLongColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new long[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(Long.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalFloatColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new float[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(null, tab.getLogical(2, 0));
        Assert.assertEquals(Float.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalDoubleColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new double[] {1, 2, 3});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(null, tab.getLogical(2, 0));
        Assert.assertEquals(Double.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalBooleanColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new Boolean[] {false, true, false});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(null, tab.getLogical(2, 0));
        Assert.assertEquals(Boolean.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalCharColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new char[] {'a', 'b', 'c'});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(null, tab.getLogical(2, 0));
        Assert.assertEquals(Character.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetLogicalStringColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new String[] {"abcdef", "abcdef", "abcdef"});
        tab.set(0, 0, false);
        tab.set(1, 0, true);
        tab.set(2, 0, null);
        Assert.assertEquals(false, tab.getLogical(0, 0));
        Assert.assertEquals(true, tab.getLogical(1, 0));
        Assert.assertEquals(null, tab.getLogical(2, 0));
        Assert.assertEquals(String.class, tab.get(0, 0).getClass());
    }

    @Test
    public void testSetStringByteColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new byte[] {1, 2, 3});
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringLogicalColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new boolean[] {true, false, true});
        tab.set(0, 0, "true");
        tab.set(1, 0, "false");
        tab.set(2, 0, "null");
        Assert.assertEquals("true", tab.getString(0, 0));
        Assert.assertEquals("false", tab.getString(1, 0));
        Assert.assertEquals("null", tab.getString(2, 0));
    }

    @Test
    public void testSetStringShortColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new short[] {1, 2, 3});
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringIntColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new int[] {1, 2, 3});
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringLongColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new long[] {1, 2, 3});
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringFloatColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new float[] {1, 2, 3});
        tab.set(0, 0, "-1");
        tab.set(1, 0, Float.NaN);
        tab.set(2, 0, null);
        Assert.assertEquals("-1.0", tab.getString(0, 0));
        Assert.assertEquals("NaN", tab.getString(1, 0));
        Assert.assertEquals("NaN", tab.getString(2, 0));
    }

    @Test
    public void testSetStringDoubleColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new double[] {1, 2, 3});
        tab.set(0, 0, "-1");
        tab.set(1, 0, Double.NaN);
        tab.set(2, 0, null);
        Assert.assertEquals("-1.0", tab.getString(0, 0));
        Assert.assertEquals("NaN", tab.getString(1, 0));
        Assert.assertEquals("NaN", tab.getString(2, 0));
    }

    @Test
    public void testSetStringStringColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new String[] {"abc", "def", "ghi"});
        tab.set(0, 0, "-1");
        tab.set(1, 0, null);
        Assert.assertEquals("-1", tab.getString(0, 0));
        Assert.assertEquals("", tab.getString(1, 0));
    }

    @Test
    public void testSetStringBytesColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new byte[3][10]);
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringCharsColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new char[3][10]);
        tab.set(0, 0, "-1");
        Assert.assertEquals("-1", tab.getString(0, 0));
    }

    @Test
    public void testSetStringCharColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new char[3]);
        tab.set(0, 0, "1");
        tab.set(1, 0, "a");
        tab.set(2, 0, "A");
        Assert.assertEquals("1", tab.getString(0, 0));
        Assert.assertEquals("a", tab.getString(1, 0));
        Assert.assertEquals("A", tab.getString(2, 0));
    }

    @Test
    public void testSetCharByteColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new byte[] {1, 2, 3});
        tab.set(0, 0, 'a');
        Assert.assertEquals((byte) 'a', tab.get(0, 0));
    }

    @Test
    public void testSetCharLogicalColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new boolean[] {false, true, false, true, false, true, true});
        tab.set(0, 0, 'T');
        tab.set(1, 0, 'F');
        tab.set(2, 0, 't');
        tab.set(3, 0, 'f');
        tab.set(4, 0, '1');
        tab.set(5, 0, '0');
        tab.set(6, 0, '\0');
        Assert.assertEquals(true, tab.get(0, 0));
        Assert.assertEquals(false, tab.get(1, 0));
        Assert.assertEquals(true, tab.get(2, 0));
        Assert.assertEquals(false, tab.get(3, 0));
        Assert.assertEquals(true, tab.get(4, 0));
        Assert.assertEquals(false, tab.get(5, 0));
        Assert.assertNull(tab.get(6, 0));
    }

    @Test
    public void testSetCharStringColumn() throws Exception {
        BinaryTable tab = new BinaryTable();
        tab.addColumn(new String[] {"abc", "def", "ghi"});
        tab.set(0, 0, '1');
        tab.set(1, 0, 'a');
        tab.set(2, 0, 'A');
        Assert.assertEquals("1", tab.getString(0, 0));
        Assert.assertEquals("a", tab.getString(1, 0));
        Assert.assertEquals("A", tab.getString(2, 0));
    }

}
