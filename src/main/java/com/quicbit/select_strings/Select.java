package com.quicbit.select_strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Select {

    interface Match {
        boolean matches (String s);
    }

    static class MatchRE implements Match {
        Pattern p;
        MatchRE (String s)                { this.p = Pattern.compile(s); }
        public boolean matches (String s) { return s != null && p.matcher(s).matches(); }
    }

    static class MatchStr implements Match {
        private String s;
        MatchStr (String s)               { this.s = s; }
        public boolean matches (String s) { return s != null && s.equals(this.s); }
    }

    static class MatchNull implements Match {
        MatchNull() {}
        public boolean matches (String s) { return s == null; }
    }

    final static Match MATCH_REMAINING = s -> true;  // not actually called - handled specially to collect all remaining

    public static Match rex (String expr) {
        if (expr == null) {
            return new MatchNull();
        } else if(expr.startsWith("/") && expr.endsWith("/")) {
            return new MatchRE(expr.substring(1, expr.length()-1));
        } else if(!expr.contains("*")) {
            return new MatchStr(expr);
        } else if(expr.trim().equals("*")) {
            return MATCH_REMAINING;  // special expression.  non-greedy. collects all unmatched.
        } else {
            return new MatchRE(escape_re(expr));
        }
    }

    static String escape_re (String s) {
        s = s.replaceAll("([-{}\\[\\]()+?.,\\\\^$|#\\s])", "\\\\$1");   // escape all special except '*'
        return '^' + s.replaceAll("[*]", ".*") + '$';              // xyz*123 -> ^xyz.*123$
    }

    // expressions may be strings with simple wild-card "*" or "/.../" regular expressions
    public static String[] select (String[] expressions, String[] strings) {
        return _select(Arrays.stream(expressions).map(Select::rex).toArray(Match[]::new), strings);
    }

// insert all items from array b into array a at offset off
//   inject([0,3], 1, [1,2])  gives:  [0,1,2,3]
    static <T> void inject (List<T> a, int off, List<T> b) {
        int blen = b.size();
        for (int i=0; i<blen; i++) { a.add(off + i, b.get(i)); }
    }

    static String[] _select (Match[] expressions, String[] strings) {
        List<String> rem = new ArrayList<>(Arrays.asList(strings));
        List<String> ret = new ArrayList<>();
        int rem_index = -1;           // offset at which to insert all remaining ('*') items
        for (Match expr: expressions) {
            if (rem.size() == 0) {
                break;
            }
            if(expr == MATCH_REMAINING) {
                if (rem_index == -1) {     // ignore subsequent match-all (just as overlapping expressions are greedy)
                    rem_index = ret.size();
                }
            } else {
                for(int i=0; i < rem.size(); i++) {
                    String s = rem.get(i);
                    if(expr.matches(s)) {
                        ret.add(s);
                        rem.remove(i);
                        i--; // compensate for splice
                    }
                }
            }
        }
        if(rem_index != -1) {
            inject(ret, rem_index, rem);
        }
        return ret.toArray(String[]::new);
    }


}