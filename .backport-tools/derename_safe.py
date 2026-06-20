#!/usr/bin/env python3
"""Safe flat renamer: rename varNN/p_NNNNN_ within method bodies (flat per body).
Skips TYPE-declaration blocks (class/interface/enum) to avoid mis-naming
abstract-method params. Backup exists; verified by recompile."""
import re, glob

KW = set("new this super return void int long float double boolean byte short char "
         "class if else for while do switch case break continue try catch finally throw "
         "throws instanceof null true false var final static public private protected "
         "abstract default enum interface extends implements package import synchronized".split())

def mask(s):
    out = list(s); i = 0; n = len(s)
    while i < n:
        c = s[i]; two = s[i:i+2]
        if two == '//':
            j = s.find('\n', i); j = n if j < 0 else j
            for k in range(i, j): out[k] = ' '
            i = j; continue
        if two == '/*':
            j = s.find('*/', i + 2); j = n if j < 0 else j + 2
            for k in range(i, min(j, n)): out[k] = ' '
            i = j; continue
        if c == '"' or c == "'":
            q = c; out[i] = ' '; i += 1
            while i < n:
                if s[i] == chr(92):           # backslash escape
                    out[i] = ' '
                    if i + 1 < n: out[i+1] = ' '
                    i += 2; continue
                if s[i] == q:
                    out[i] = ' '; i += 1; break
                out[i] = ' '; i += 1
            continue
        i += 1
    return ''.join(out)

def _param_start(m, brace):
    """Return index of the '(' opening the param list before a method-body '{', else brace+1."""
    j = brace - 1
    while j >= 0 and m[j] in ' \t\r\n': j -= 1
    if j >= 0 and m[j] != ')':   # skip optional "throws A, B"
        k = j
        while k >= 0 and (m[k].isalnum() or m[k] in '_,. \t\r\n'): k -= 1
        if k >= 0 and m[k] == ')': j = k
    if j >= 0 and m[j] == ')':
        d = 0; p = j
        while p >= 0:
            if m[p] == ')': d += 1
            elif m[p] == '(':
                d -= 1
                if d == 0: return p
            p -= 1
    return brace + 1

def _is_method(m, brace):
    """True if the '{' at index `brace` opens a method/constructor/lambda body."""
    j = brace - 1
    while j >= 0 and m[j] in ' \t\r\n': j -= 1
    if j < 0: return False
    if m[j] == '>' and j-1 >= 0 and m[j-1] == '-': return True  # lambda  ) -> {
    b = j
    while b >= 0 and m[b] not in ';{}': b -= 1
    header = m[b+1:brace]
    if re.search(r'\b(class|interface|enum)\b', header): return False     # type body
    if re.search(r'\bnew\b', header) and m[j] == ')': return False        # anon class body
    return m[j] == ')'                                                    # method/ctor: ...) {

def bodies(m):
    """Find method/lambda body spans (params included), recursing into TYPE bodies but
    NOT into method bodies (whose flat rename already covers nested anon/local classes)."""
    spans = []; stack = []   # each frame: True if it is a method-scope root
    in_method = False
    for i, c in enumerate(m):
        if c == '{':
            if not in_method and _is_method(m, i):
                stack.append(('M', _param_start(m, i))); in_method = True
            else:
                stack.append(('X', None))
        elif c == '}':
            if stack:
                kind, start = stack.pop()
                if kind == 'M':
                    spans.append((start, i)); in_method = False
    return spans

DECL = re.compile(
    r'(?:catch\s*\(\s*|for\s*\(\s*(?:final\s+)?|[(;{}]\s*(?:final\s+)?|^\s*(?:final\s+)?|,\s*(?:final\s+)?)'
    r'([A-Z]\w*(?:\.\w+)*(?:<[^;{}=]*?>)?(?:\[\])*|int|long|float|double|boolean|byte|short|char)'
    r'\s+((?:var\d+|p_\d+_|[A-Za-z_]\w*\$temp))\b', re.M)

def name_for(t):
    t = re.sub(r'<.*>', '', t).replace('[]', '').strip().split('.')[-1]
    p = {"int": "i", "long": "l", "float": "f", "double": "d", "boolean": "flag",
         "byte": "b", "short": "s", "char": "c"}
    if t in p: return p[t]
    if re.search(r'(Exception|Throwable|Error)$', t): return "e"
    return (t[0].lower() + t[1:]) if t and t[0].isalpha() else "v"

def process(path):
    s = open(path, encoding="utf-8").read()
    if not re.search(r'\b(var\d+|p_\d+_)\b|[A-Za-z_]\w*\$temp', s): return False
    m = mask(s); out = []; last = 0; changed = False
    for a, b in bodies(m):
        out.append(s[last:a]); seg = s[a:b]; segm = m[a:b]
        decls = {}
        for mo in DECL.finditer(segm): decls.setdefault(mo.group(2), mo.group(1))
        if decls:
            used = set(re.findall(r'\b[A-Za-z_]\w*\b', segm)) - set(decls)
            mp = {}
            for v, t in decls.items():
                base = name_for(t); cand = base; k = 1
                while cand in used or cand in mp.values() or cand in KW:
                    k += 1; cand = base + str(k)
                mp[v] = cand
            seg = re.sub(r'\bvar\d+\b|\bp_\d+_\b|[A-Za-z_]\w*\$temp\b', lambda mo: mp.get(mo.group(0), mo.group(0)), seg)
            changed = True
        out.append(seg); last = b
    out.append(s[last:])
    if changed: open(path, "w", encoding="utf-8", newline="\n").write("".join(out))
    return changed

if __name__ == "__main__":
    n = 0
    for r in ["common/src/main/java", "forge/src/main/java", "fabric/src/main/java"]:
        for p in glob.glob(r + "/**/*.java", recursive=True):
            if process(p): n += 1
    print("files changed:", n)
    resid = 0
    for r in ["common/src/main/java", "forge/src/main/java", "fabric/src/main/java"]:
        for p in glob.glob(r + "/**/*.java", recursive=True):
            if re.search(r'\b(var\d+|p_\d+_)\b', open(p, encoding="utf-8").read()): resid += 1
    print("residual var/p_ files (nested-class locals + abstract params):", resid)
