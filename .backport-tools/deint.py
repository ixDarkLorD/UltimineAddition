#!/usr/bin/env python3
import re, sys, os
base = os.path.dirname(__file__)
member = {}
with open(os.path.join(base,"int_member.tsv"), encoding="utf-8") as f:
    for line in f:
        p=line.rstrip("\n").split("\t")
        if len(p)==2: member[p[0]]=p[1]
simple={}; fqcn={}
with open(os.path.join(base,"int_class.tsv"), encoding="utf-8") as f:
    for line in f:
        p=line.rstrip("\n").split("\t")
        if len(p)==3:
            simple[p[0]]=p[1]; fqcn[p[0]]=p[2]
def conv(text):
    un=set()
    # 1) imports of net.minecraft.class_NNNN  (possibly nested with $)
    def imp(mo):
        cls=mo.group(1)
        if cls in fqcn: return "import "+fqcn[cls]+";"
        un.add(cls); return mo.group(0)
    text=re.sub(r'import\s+net\.minecraft\.(class_\d+);', imp, text)
    # 2) members
    def mem(mo):
        s=mo.group(0)
        if s in member: return member[s]
        un.add(s); return s
    text=re.sub(r'\b(?:method|field)_\d+\b', mem, text)
    # 3) class simple names
    def cls(mo):
        s=mo.group(0)
        if s in simple: return simple[s]
        un.add(s); return s
    text=re.sub(r'\bclass_\d+\b', cls, text)
    return text, un
if __name__=="__main__":
    src=sys.argv[1]; text=open(src,encoding="utf-8").read()
    out,un=conv(text)
    if len(sys.argv)>2: open(sys.argv[2],"w",encoding="utf-8",newline="\n").write(out)
    else: sys.stdout.write(out)
    if un: sys.stderr.write("UNMAPPED("+src+"): "+",".join(sorted(un))+"\n")
