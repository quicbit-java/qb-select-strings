package com.quicbit.select_strings;

import org.junit.Assert;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestContext {
    PrintStream out = System.out;

    @FunctionalInterface
    public interface RowFn {
        Object apply(Row r);
    }

    static class Row {
        Object[] values;
        Table table;

        public Row(Table table, Object[] vals) {
            this.table = table;
            this.values = vals;
        }

        public String str(String n) {
            return (String) values[table.indexOf(n)];
        }

        public Object expected () {
            return values[values.length-1];
        }

        public Object[] inputs() {
            return Arrays.copyOfRange(values, 0, values.length-1);
        }

        public String[] strarr(String n) {
            Object[] a = (Object[]) values[table.indexOf(n)];
            return Arrays.copyOf(a, a.length, String[].class);
        }
    }

    static class Table {
        TestContext context;
        String[] header;
        Row[] rows;
        Map<String, Integer> colsByName;

        public Table(TestContext context, Object[]... all_rows) {
            this.context = context;
            String[] header = Arrays.copyOf(all_rows[0], all_rows[0].length, String[].class);
            colsByName = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                colsByName.put(header[i], i);
            }
            this.header = header;

            Row[] rows = new Row[all_rows.length-1];
            for (int i=0; i<all_rows.length-1; i++) {
                rows[i] = new Row(this, all_rows[i+1]);
            }
            this.rows = rows;
        }

        int indexOf(String n) {
            Integer idx = colsByName.get(n);
            if (idx == null) {
                throw new IllegalArgumentException("unknown column: " + n);
            }
            return idx;
        }

        public void test(RowFn fn) {
            boolean ok = true;
            for (Row row : this.rows) {
                Object actual = fn.apply(row);
                Object expected = row.expected();
                String msg = format(row.inputs()) + " -expect-> " + format(actual);
                try {
                    if (expected != null && expected.getClass().isArray()) {
                        Object[] reta = arrayOf(actual);
                        Object[] expa = arrayOf(expected);
                        Assert.assertArrayEquals(expa, reta);
                    } else {
                        Assert.assertEquals(expected, actual);
                    }
                    context.out.println("ok : " + msg);
                } catch (AssertionError e) {
                    ok = false;
                    context.out.println("not ok : " + msg);
                    context.out.println("  ---");
                    context.out.println("    expected: " + format(expected));
                    context.out.println("    actual:   " + format(actual));
                }
            }
            if (!ok) {
                throw new AssertionError("one or more assertion failures");
            }
        }
    }

    // format() was copied from org.junit.Assert to replicate look and feel of assertion messages
    static String format(Object obj) {
        if (obj != null && obj.getClass().isArray()) {
            return Arrays.deepToString(arrayOf(obj));
        } else {
            return String.valueOf(obj);
        }
    }

    public static Object[] arrayOf (Object a) {
        int len = Array.getLength(a);
        Object[] ret = new Object[len];
        for (int i=0; i<len; i++) {
            ret[i] = Array.get(a, i);
        }
        return ret;
    }

    public static Table table(Object[]... rows) { return new Table(new TestContext(), rows); }
    public static Object[] a (Object... a) { return a; }
    public static String[] sa (Object... a) { return Arrays.copyOf(a, a.length, String[].class); }
}
