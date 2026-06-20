#!/usr/bin/env python3
import re, sys, os
DICT = os.path.join(os.path.dirname(__file__), "srg2named.tsv")
m = {}
with open(DICT, encoding="utf-8") as f:
    for line in f:
        p = line.rstrip("\n").split("\t")
        if len(p) == 2:
            m[p[0]] = p[1]
tok = re.compile(r'\b([mf]_\d+_)\b')
def conv(text):
    unmapped = set()
    def repl(mo):
        s = mo.group(1)
        if s in m: return m[s]
        unmapped.add(s); return s
    return tok.sub(repl, text), unmapped
if __name__ == "__main__":
    src = sys.argv[1]
    text = open(src, encoding="utf-8").read()
    out, un = conv(text)
    if len(sys.argv) > 2:
        open(sys.argv[2], "w", encoding="utf-8", newline="\n").write(out)
    else:
        sys.stdout.write(out)
    if un:
        sys.stderr.write("UNMAPPED: " + ",".join(sorted(un)) + "\n")
