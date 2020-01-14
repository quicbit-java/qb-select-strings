package com.quicbit.select_strings;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.quicbit.select_strings.TestKit.*;
import static com.quicbit.select_strings.Select.*;

public class SelectTest {
    @Test
    public void testRegex() {

        table(
            a( "s",      "exp" ),
            a( "*",      "^.*$" ),
            a( "a|*2#(", "^a\\|.*2\\#\\($" )
        ).test("regex(s)",
                (r) -> escape_re(r.str("s"))
        );
    }

    @Test
    public void testSelectZeroOrOne () {
        table(
            a( "expressions", "strings",           "expect" ),
            a( a( "b" ),      a(),                 sa() ),
            a( a( "b" ),      a( "b" ),            sa( "b" ) ),
            a( a( "b" ),      a( "a", "b" ),       sa( "b" ) ),
            a( a( "b" ),      a( "b", "c" ),       sa( "b" ) ),
            a( a( "b" ),      a( "a", "b", "c" ),  sa( "b" ) ),
            a( a( "b*" ),     a( "b" ),            sa( "b" ) ),
            a( a( "b" ),      a( "bc" ),           sa() ),
            a( a( "b" ),      a( "ab" ),           sa() ),
            a( a( "*" ),      a(),                 sa() ),
            a( a( "*" ),      a( "" ),             sa( "" ) ),
            a( a( "*" ),      a( "ab" ),           sa( "ab" ) ),
            a( a( "b*" ),     a( "bc" ),           sa( "bc" ) ),
            a( a( "*b" ),     a( "ab" ),           sa( "ab" ) ),
            a( a( "*b" ),     a( "a", "ab", "c" ), sa( "ab" ) )
        ).test("select(expressions, strings)",
            (r) -> select(r.strarr("expressions"), r.strarr("strings"))
        );
    }

    @Test
    public void testSelectMany () {
        table(
            a( "expressions",              "strings",                "expect" ),
            a( a( "a", "b" ),              a( "a", "b" ),            a( "a", "b" ) ),
            a( a( "*" ),                   a( "a", "b" ),            a( "a", "b" ) ),
            a( a( "a", "c" ),              a( "a", "b", "c" ),       a( "a", "c" ) ),
            a( a( null, "c" ),             a( "a", null, "c", null ),      a( null, null, "c" ) ),
            a( a( "c", "*" ),              a( "a", "b", "c" ),       a( "c", "a", "b" ) ),
            a( a( "*", "c" ),              a( "a", "b", "c" ),       a( "a", "b", "c" ) ),
            a( a( "*", "c", "a" ),         a( "a", "b", "c" ),       a( "b", "c", "a" ) ),
            a( a( "*", "c", "a", "*" ),    a( "a", "b", "c" ),       a( "b", "c", "a" ) ),
            a( a( "c", "*" ),              a( "a", "", "c" ),        a( "c", "a", "" ) ),
            a( a( "c", "*" ),              a( "a", null, "c" ),      a( "c", "a", null ) ),
            a(
                a( "fr*", "t*", "/((at)|(as)).*/", "*" ),
                a( "from", "tangy", "title", "fribble", "fact", "slipper", "at", "ask" ),
                a( "from", "fribble", "tangy", "title", "at", "ask", "fact", "slipper" )
            )
        ).test(
            "select(expressions, strings)",
            (r) -> select(r.strarr("expressions"), r.strarr("strings"))
        );
    }

    @Test
    public void testInject () {
        table(
            a( "input",   "off", "insert", "exp" ),
            a( a(),       0,     a(),      a() ),
            a( a(),       0,     a( 3 ),   a( 3 ) ),
//            a( a(),       1,     a( 3 ),   a( null, 3 ) ),  // this case, from the javascript tests, is not needed - we don't inject greater than length
            a( a( 1, 2 ), 0,     a( 3 ),   a( 3, 1, 2 ) ),
            a( a( 1 ),    0,     a( 3 ),   a( 3, 1 ) )
        ).test(
            "inject(input, off, insert)",
            (r) -> {
                List<Integer> ret = r.intlist("input");
                inject(ret, r.ival("off"), r.intlist("insert"));
                return ret.toArray();
            }
        );
    }

    @Test
    public void testAdd () {
        List<Integer> a = new ArrayList<>();
        a.add(3);
        a.add(0, 1);
        System.out.println(a);
        a.add(1, 2);
        System.out.println(a);

    }
}
