#!/usr/bin/env python3
"""Rename obfuscated parameters (varNN/p_NNNNN_) in ABSTRACT method signatures
(declarations ending in ';'). These params are never referenced, so renaming is
zero-risk. Generics-aware comma splitting."""
import re, glob

def name_for(t):
    t = re.sub(r'<.*>', '', t).replace('[]', '').strip().split('.')[-1]
    p = {"int": "i", "long": "l", "float": "f", "double": "d", "boolean": "flag",
         "byte": "b", "short": "s", "char": "c"}
    if t in p: return p[t]
    if re.search(r'(Exception|Throwable|Error)$', t): return "e"
    return (t[0].lower() + t[1:]) if t and t[0].isalpha() else "v"

def split_params(s):
    parts = []; depth = 0; cur = ""
    for ch in s:
        if ch in "<([": depth += 1
        elif ch in ">)]": depth -= 1
        if ch == ',' and depth == 0:
            parts.append(cur); cur = ""
        else:
            cur += ch
    if cur.strip(): parts.append(cur)
    return parts

PARAM = re.compile(r'^(\s*(?:@[\w.]+(?:\([^)]*\))?\s+)*(?:final\s+)?)'
                   r'([\w.]+(?:<.*>)?(?:\[\])*)\s+((?:var\d+|p_\d+_))\s*$', re.S)

# abstract method signature: name(params) [throws ...] ;   (no body)
SIG = re.compile(r'(\b[A-Za-z_]\w*\s*\()([^()]*)(\)\s*(?:throws[\w\s,.]*?)?;)')

def fix(text):
    def repl(mo):
        head, params, tail = mo.group(1), mo.group(2), mo.group(3)
        if not re.search(r'\b(var\d+|p_\d+_)\b', params): return mo.group(0)
        used = set(); newparts = []
        for part in split_params(params):
            m = PARAM.match(part)
            if not m: newparts.append(part); continue
            base = name_for(m.group(2)); cand = base; k = 1
            while cand in used: k += 1; cand = base + str(k)
            used.add(cand)
            newparts.append(m.group(1) + m.group(2) + " " + cand)
        return head + ",".join(newparts) + tail
    return SIG.sub(repl, text)

if __name__ == "__main__":
    n = 0
    for r in ["common/src/main/java", "forge/src/main/java", "fabric/src/main/java"]:
        for p in glob.glob(r + "/**/*.java", recursive=True):
            s = open(p, encoding="utf-8").read()
            s2 = fix(s)
            if s2 != s:
                open(p, "w", encoding="utf-8", newline="\n").write(s2); n += 1
    print("files changed:", n)
