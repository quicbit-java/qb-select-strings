package com.quicbit.select_strings;

import org.junit.Test;

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
        ).test("regex",
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
        ).test("select zero or one",
            (r) -> select(r.strarr("expressions"), r.strarr("strings"))
        );
    }

    @Test
    public void testSelectMany () {
        table(
            a( "expressions",              "strings",                                                                "expect" ),
            a( a( "a", "b" ),              a( "a", "b" ),                                                            a( "a", "b" ) ),
            a( a( "*" ),                   a( "a", "b" ),                                                            a( "a", "b" ) ),
            a( a( "a", "c" ),              a( "a", "b", "c" ),                                                       a( "a", "c" ) ),
            a( a( "c", "*" ),              a( "a", "b", "c" ),                                                       a( "c", "a", "b" ) ),
            a( a( "*", "c" ),              a( "a", "b", "c" ),                                                       a( "a", "b", "c" ) ),
            a( a( "*", "c", "a" ),         a( "a", "b", "c" ),                                                       a( "b", "c", "a" ) ),
            a( a( "*", "c", "a", "*" ),    a( "a", "b", "c" ),                                                       a( "b", "c", "a" ) ),
            a( a( "c", "*" ),              a( "a", "", "c" ),                                                        a( "c", "a", "" ) ),
            a( a( "c", "*" ),              a( "a", null, "c" ),                                                 a( "c", "a", null ) ),
            a(
                a( "fr*", "t*", "/((at)|(as)).*/", "*" ),
                a( "from", "tangy", "title", "fribble", "fact", "slipper", "at", "ask" ),
                a( "from", "fribble", "tangy", "title", "at", "ask", "fact", "slipper" )
            )
        ).test(
            "select many",
            (r) -> select(r.strarr("expressions"), r.strarr("strings"))
        );
    }

    @Test
    public void testInject () {
        String s = null;
        table(
            a( "a",          "off", "insert", "exp" ),
            a( a(),          0,     a(),      a() ),
            a( a( 3 ),       0,     a( 3 ),   a( 3 ) ),
            a( a(  3 ),      1,     a( 3 ),   a(  3 ) ),
            a( a( 3, 1, 2 ), 0,     a( 3 ),   a( 3, 1, 2 ) ),
            a( a( 3, 1 ),    0,     a( 3 ),   a( 3, 1 ) )
        ).test(
            "inject",
            (r) -> {
                List<Integer> ret = r.intlist("a");
                inject(r.intlist("a"), r.ival("off"), r.intlist("insert"));
                return ret.toArray();
            }
        );
    }
}
