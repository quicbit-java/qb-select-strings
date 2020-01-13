package com.quicbit.select_strings;

import org.junit.Assert;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;

public class TestKit {
    PrintStream out = System.out;
    int num_tests = 0;
    int num_ok = 0;

    @FunctionalInterface
    public interface RowFn {
        Object apply(Row r);
    }

    static class Options {
        int max_tests = 0;
    }

    static class Row {
        Object[] values;
        Table table;

        public Row(Table table, Object[] vals) {
            this.table = table;
            this.values = vals;
        }

        public String str(String n) {
            return (String) obj(n);
        }

        public int ival(String n) {
            return (int) obj(n);
        }

        public Object obj(String n) {
            return values[table.indexOf(n)];
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

        public int[] intarr(String n) {
            Object[] a = (Object[]) values[table.indexOf(n)];
            int[] ret = new int[a.length];
            for (var i=0; i<a.length; i++) {
                ret[i] = (int) a[i];
            }
            return ret;
        }

        public List<Integer> intlist(String n) {
            Object[] a = (Object[]) values[table.indexOf(n)];
            List<Integer> ret = new ArrayList<>(a.length);
            for (Object v : a) { ret.add((Integer) v); }
            return ret;
        }

        public List<Object> list (String n) {
            Object[] a = (Object[]) values[table.indexOf(n)];
            List<Object> ret = new ArrayList<>(a.length);
            ret.addAll(Arrays.asList(a));
            return ret;
        }
    }

    static class Table {
        TestKit context;
        String[] header;
        Row[] rows;
        Map<String, Integer> colsByName;

        public Table(TestKit context, Object[]... all_rows) {
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

        public void test1 (String name, RowFn fn) {
            Options opt = new Options();
            opt.max_tests = 1;
            _test(name, fn, opt);
        }

        public void test (String name, RowFn fn) {
            _test(name, fn, new Options());
        }

        public void _test(String name, RowFn fn, Options opt) {
            context.out.println("# " + name);
            boolean ok = true;
            for (Row row : this.rows) {
                Object actual = fn.apply(row);
                Object expected = row.expected();
                String msg = format(row.inputs()) + " -expect-> " + format(actual);
                context.num_tests++;
                if(opt.max_tests != 0 && context.num_tests >= opt.max_tests) {
                    continue;
                }
                try {
                    if (expected != null && expected.getClass().isArray()) {
                        Object[] reta = arrayOf(actual);
                        Object[] expa = arrayOf(expected);
                        Assert.assertArrayEquals(expa, reta);
                    } else {
                        Assert.assertEquals(expected, actual);
                    }
                    context.num_ok++;
                    context.out.println("ok " + context.num_tests + " : " + msg);
                } catch (Exception e) {
                    ok = false;
                    context.out.println("not ok " + context.num_tests + " : " + msg);
                    context.out.println("  ---");
                    context.out.println("    expected: " + format(expected));
                    context.out.println("    actual:   " + format(actual));
                    context.out.println("    " + e.getMessage());
                    e.printStackTrace(context.out);
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

    public static Table table(Object[]... rows) { return new Table(new TestKit(), rows); }
    public static Object[] a (Object... a) { return a; }
    public static String[] sa (Object... a) { return Arrays.copyOf(a, a.length, String[].class); }
}
