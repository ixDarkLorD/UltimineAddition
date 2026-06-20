#!/usr/bin/env python3
"""Rename decompiler-obfuscated locals/params (varNN, p_NNNNN_) to type-based
names, with correct recursive scoping:
  SCOPE  = method/constructor/lambda/initializer body -> locals renamed here
  TYPE   = class/interface/enum/anon-class body       -> recurse, no direct locals
  CONTROL= if/for/while/try/catch/.. block            -> part of enclosing SCOPE
A var's rename is applied to its SCOPE's own text (excluding nested SCOPE/TYPE
regions). Collisions within a scope get a numeric suffix. Verified by recompile.
"""
import re, glob

KEYWORDS = set("new this super return void int long float double boolean byte short char "
               "class if else for while do switch case break continue try catch finally throw "
               "throws instanceof null true false var final static public private protected "
               "abstract default enum interface extends implements package import synchronized "
               "volatile transient native strictfp assert goto const".split())
CONTROL_KW = {"if", "for", "while", "switch", "synchronized", "catch"}

def mask(s):
    """Replace string/char/comment contents with spaces (same length) for structural scanning."""
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
                if s[i] == '\\':
                    out[i] = ' ';  i += 1
                    if i < n: out[i] = ' '; i += 1
                    continue
                if s[i] == q: out[i] = ' '; i += 1; break
                out[i] = ' '; i += 1
            continue
        i += 1
    return ''.join(out)

def classify(m, open_idx):
    j = open_idx - 1
    while j >= 0 and m[j].isspace(): j -= 1
    if j < 0: return 'OTHER'
    # lambda:  ... -> {
    if m[j] == '>' and j-1 >= 0 and m[j-1] == '-': return 'SCOPE'
    if m[j] == ')':
        depth = 0; k = j
        while k >= 0:
            if m[k] == ')': depth += 1
            elif m[k] == '(':
                depth -= 1
                if depth == 0: break
            k -= 1
        p = k - 1
        while p >= 0 and m[p].isspace(): p -= 1
        e = p
        while p >= 0 and (m[p].isalnum() or m[p] == '_'): p -= 1
        ident = m[p+1:e+1]
        if ident in CONTROL_KW: return 'CONTROL'
        q = p
        while q >= 0 and m[q].isspace(): q -= 1
        e2 = q
        while q >= 0 and (m[q].isalnum() or m[q] == '_'): q -= 1
        if m[q+1:e2+1] == 'new': return 'TYPE'
        return 'SCOPE'
    # word before {
    e = j; q = j
    while q >= 0 and (m[q].isalnum() or m[q] == '_'): q -= 1
    word = m[q+1:e+1]
    if word in ('try', 'finally', 'do', 'else'): return 'CONTROL'
    # statement header back to boundary
    b = q
    while b >= 0 and m[b] not in ';{}': b -= 1
    header = m[b+1:open_idx]
    if re.search(r'\b(class|interface|enum)\b', header) or '@interface' in header:
        return 'TYPE'
    if word == 'static' or header.strip() == '':
        return 'SCOPE'   # static / instance initializer
    return 'CONTROL'

def build_tree(m):
    """Return list of top-level nodes; node = dict(start,end,kind,children)."""
    stack = []; roots = []
    for i, c in enumerate(m):
        if c == '{':
            node = {'open': i, 'kind': None, 'children': []}
            stack.append(node)
        elif c == '}':
            if not stack: continue
            node = stack.pop()
            node['close'] = i
            node['kind'] = classify(m, node['open'])
            if stack: stack[-1]['children'].append(node)
            else: roots.append(node)
    return roots

DECL = re.compile(
    r'(?:catch\s*\(\s*|for\s*\(\s*(?:final\s+)?|[(;{}]\s*(?:final\s+)?|^\s*(?:final\s+)?|,\s*(?:final\s+)?)'
    r'([A-Z][A-Za-z0-9_]*(?:\.[A-Za-z0-9_]+)*(?:<[^;{}=]*?>)?(?:\[\])*|int|long|float|double|boolean|byte|short|char)'
    r'\s+((?:var\d+|p_\d+_))\b', re.M)

def simple_type(t):
    t = re.sub(r'<.*>', '', t).replace('[]', '').strip().split('.')[-1]
    return t

def name_for(t):
    s = simple_type(t)
    prims = {"int": "i", "long": "l", "float": "f", "double": "d", "boolean": "flag",
             "byte": "b", "short": "s", "char": "c"}
    if s in prims: return prims[s]
    if re.search(r'(Exception|Throwable|Error)$', s): return "e"
    if not s or not s[0].isalpha(): return "v"
    return s[0].lower() + s[1:]

def own_text_regions(node):
    """Char ranges directly owned by this SCOPE node (excluding nested SCOPE/TYPE bodies,
    but INCLUDING nested CONTROL blocks recursively)."""
    regions = []
    cur = node['open'] + 1
    def walk(n):
        nonlocal cur
        for ch in n['children']:
            if ch['kind'] in ('SCOPE', 'TYPE'):
                regions.append((cur, ch['open']))   # text up to nested scope's '{' (keeps signature/header)
                cur = ch['close'] + 1
            else:  # CONTROL / OTHER -> part of this scope; descend
                walk(ch)
    walk(node)
    regions.append((cur, node['close']))
    return [(a, b) for a, b in regions if b > a]

def collect_scopes(roots):
    out = []
    def rec(n):
        if n['kind'] == 'SCOPE': out.append(n)
        for ch in n['children']: rec(ch)
    for r in roots: rec(r)
    return out

def process(path):
    s = open(path, encoding="utf-8").read()
    if not re.search(r'\b(var\d+|p_\d+_)\b', s): return False
    m = mask(s)
    roots = build_tree(m)
    edits = []  # (start, end, oldtok->newtok map) ; we apply by rebuilding
    repl = {}   # global position-independent: map (scope_region)-> {tok:new}
    # gather rename ops as (lo, hi, mapping)
    ops = []
    for node in collect_scopes(roots):
        regions = own_text_regions(node)
        # find decls within own regions (use masked code for matching, capture from real text)
        decls = {}
        owntext_masked = "".join(m[a:b] for a, b in regions)
        for mo in DECL.finditer(owntext_masked):
            decls.setdefault(mo.group(2), mo.group(1))
        if not decls: continue
        used = set(re.findall(r'\b[A-Za-z_][A-Za-z0-9_]*\b', owntext_masked)) - set(decls)
        mapping = {}
        for var, typ in decls.items():
            base = name_for(typ); cand = base; k = 1
            while cand in used or cand in mapping.values() or cand in KEYWORDS:
                k += 1; cand = base + str(k)
            mapping[var] = cand
        for a, b in regions:
            ops.append((a, b, mapping))
    if not ops: return False
    # apply edits on real text within each region
    ops.sort(key=lambda x: x[0])
    res = []; pos = 0
    for a, b, mapping in ops:
        if a < pos:  # overlap guard (shouldn't happen)
            continue
        res.append(s[pos:a])
        seg = s[a:b]
        seg = re.sub(r'\b(?:var\d+|p_\d+_)\b', lambda mo: mapping.get(mo.group(0), mo.group(0)), seg)
        res.append(seg); pos = b
    res.append(s[pos:])
    s2 = "".join(res)
    if s2 != s:
        open(path, "w", encoding="utf-8", newline="\n").write(s2)
        return True
    return False

if __name__ == "__main__":
    roots = ["common/src/main/java", "forge/src/main/java", "fabric/src/main/java"]
    changed = 0
    for r in roots:
        for p in glob.glob(r + "/**/*.java", recursive=True):
            if process(p): changed += 1
    print("files changed:", changed)
    resid = 0
    for r in roots:
        for p in glob.glob(r + "/**/*.java", recursive=True):
            if re.search(r'\b(var\d+|p_\d+_)\b', open(p, encoding="utf-8").read()):
                resid += 1; print("RESIDUAL:", p)
    print("files with residual var/p_:", resid)
