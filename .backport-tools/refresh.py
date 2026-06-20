#!/usr/bin/env python3
"""Full reproducible refresh of all Java sources from the Origin decompiled jars.
 - common classes (in both jars): de-SRG forge variant  -> common/
 - forge-only classes:            de-SRG forge variant  -> forge/
 - fabric-only classes:           de-intermediary fabric -> fabric/
 - loader-specific datagen:       forge->forge, fabric->fabric
Then re-applies all systematic decompiler-artifact fixes.
"""
import os, re, glob

BT = ".backport-tools"
FORGE_DIR = "Origin/ultimine_addition-forge-1.20.1-2001.1.5.2"
FAB_DIR   = "Origin/ultimine_addition-fabric-1.20.1-2001.1.5.2"
PKG = "net/ixdarklord/ultimine_addition"

def load_tsv(p, n=2):
    d = {}
    for line in open(p, encoding="utf-8"):
        x = line.rstrip("\n").split("\t")
        if len(x) >= n: d[x[0]] = x[1:] if n > 2 else x[1]
    return d

srg  = load_tsv(BT + "/srg2named.tsv")
imem = load_tsv(BT + "/int_member.tsv")
isimple, ifqcn = {}, {}
for line in open(BT + "/int_class.tsv", encoding="utf-8"):
    x = line.rstrip("\n").split("\t")
    if len(x) == 3: isimple[x[0]] = x[1]; ifqcn[x[0]] = x[2]

SRG_TOK = re.compile(r'\b([mf]_\d+_)\b')
def desrg(t): return SRG_TOK.sub(lambda m: srg.get(m.group(1), m.group(1)), t)

def deint(t):
    t = re.sub(r'import\s+net\.minecraft\.(class_\d+);',
               lambda m: ("import " + ifqcn[m.group(1)] + ";") if m.group(1) in ifqcn else m.group(0), t)
    t = re.sub(r'\b(?:method|field)_\d+\b', lambda m: imem.get(m.group(0), m.group(0)), t)
    t = re.sub(r'\bclass_\d+\b', lambda m: isimple.get(m.group(0), m.group(0)), t)
    return t

def rel_set(root):
    out = set()
    for dp, _, fn in os.walk(root):
        for f in fn:
            if f.endswith(".java"):
                full = os.path.join(dp, f).replace("\\", "/")
                if PKG + "/" in full: out.add(full.split(PKG + "/")[1])
    return out

forge_set, fab_set = rel_set(FORGE_DIR), rel_set(FAB_DIR)
# SAFETY GUARD: never wipe src if the Origin reference is missing/empty.
if len(forge_set) < 100 or len(fab_set) < 100:
    raise SystemExit("ABORT: Origin sources missing/incomplete (forge=%d fabric=%d). "
                     "Refusing to wipe src. Restore the Origin/ folder first."
                     % (len(forge_set), len(fab_set)))
common     = forge_set & fab_set
forge_only = forge_set - fab_set
fab_only   = fab_set - forge_set

DATAGEN_LOADER = {
    "datagen/DataGeneration.java",
    "datagen/model/ItemModelGenerator.java",
    "datagen/model/ItemModelProvider.java",
    "datagen/recipe/conditions/LegacyModeCondition.java",
    "datagen/recipe/RecipeGenerator.java",
    "datagen/tag/BlockTagGenerator.java",
    "datagen/tag/ItemTagGenerator.java",
}

def read(d, rel): return open(d + "/" + PKG + "/" + rel, encoding="utf-8").read()
def write(sub, rel, text):
    dst = sub + "/src/main/java/" + PKG + "/" + rel
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    open(dst, "w", encoding="utf-8", newline="\n").write(text)

# wipe existing java
for sub in ["common", "forge", "fabric"]:
    for p in glob.glob(sub + "/src/main/java/**/*.java", recursive=True):
        os.remove(p)

# place
for rel in sorted(common):
    if rel in DATAGEN_LOADER:
        write("forge",  rel, desrg(read(FORGE_DIR, rel)))
        write("fabric", rel, deint(read(FAB_DIR, rel)))
    else:
        write("common", rel, desrg(read(FORGE_DIR, rel)))
for rel in sorted(forge_only):
    write("forge", rel, desrg(read(FORGE_DIR, rel)))
for rel in sorted(fab_only):
    write("fabric", rel, deint(read(FAB_DIR, rel)))

# ---- fixes ----
def fix_env(p):
    s = o = open(p, encoding="utf-8").read()
    s = s.replace("import net.minecraftforge.api.distmarker.OnlyIn;", "import net.fabricmc.api.Environment;")
    s = s.replace("import net.minecraftforge.api.distmarker.Dist;", "import net.fabricmc.api.EnvType;")
    s = re.sub(r'@OnlyIn\(\s*Dist\.CLIENT\s*\)', '@Environment(EnvType.CLIENT)', s)
    s = re.sub(r'@OnlyIn\(\s*Dist\.DEDICATED_SERVER\s*\)', '@Environment(EnvType.SERVER)', s)
    s = s.replace("Dist.CLIENT", "EnvType.CLIENT").replace("Dist.DEDICATED_SERVER", "EnvType.SERVER")
    if s != o: open(p, "w", encoding="utf-8", newline="\n").write(s)

for sub in ["common", "forge"]:
    for p in glob.glob(sub + "/src/main/java/**/*.java", recursive=True): fix_env(p)

# @ExpectPlatform restore (common)
for p in glob.glob("common/src/main/java/**/*.java", recursive=True):
    s = open(p, encoding="utf-8").read()
    if "@Transformed" not in s and "ExpectPlatform.Transformed" not in s: continue
    o = s
    s = re.sub(r'^import dev\.architectury\.injectables\.annotations\.ExpectPlatform\.Transformed;\n', '', s, flags=re.M)
    s = re.sub(r'^import net\.ixdarklord\.ultimine_addition\.[A-Za-z0-9_.]*\.(?:forge|fabric)\.[A-Za-z0-9_]*Impl;\n', '', s, flags=re.M)
    s = re.sub(r'^\s*@Transformed\s*\n', '', s, flags=re.M)
    s = re.sub(r'^(\s*)return \w*Impl\.\w+\([^;]*\);\s*$', r'\1throw new AssertionError();', s, flags=re.M)
    s = re.sub(r'^(\s*)\w*Impl\.\w+\([^;]*\);\s*$', r'\1throw new AssertionError();', s, flags=re.M)
    if s != o: open(p, "w", encoding="utf-8", newline="\n").write(s)

# fabric nested-class names + MixinHoeItem GameEvent.Context
NESTED = {
    "class_2074": ("net.minecraft.advancements.critereon.ItemPredicate", "ItemPredicate.Builder"),
    "class_4751": ("net.minecraft.client.renderer.block.model.BlockModel", "BlockModel.GuiLight"),
    "class_7874": ("net.minecraft.core.HolderLookup", "HolderLookup.Provider"),
}
for p in glob.glob("fabric/src/main/java/**/*.java", recursive=True):
    s = o = open(p, encoding="utf-8").read()
    for tok, (fqcn, simple) in NESTED.items():
        if tok in s:
            s = re.sub(r'import net\.minecraft\.[A-Za-z0-9_.]*' + tok + ';', 'import ' + fqcn + ';', s)
            s = s.replace(tok, simple)
    if "class_7397" in s:  # GameEvent$Context, GameEvent already imported
        s = re.sub(r'^import net\.minecraft\.[A-Za-z0-9_.]*class_7397;\n', '', s, flags=re.M)
        s = s.replace("class_7397", "GameEvent.Context")
    if s != o: open(p, "w", encoding="utf-8", newline="\n").write(s)

# nested-type extends-clause scoping fixes
def patch(path, a, b):
    s = open(path, encoding="utf-8").read()
    if a in s: open(path, "w", encoding="utf-8", newline="\n").write(s.replace(a, b))

patch("common/src/main/java/" + PKG + "/client/gui/screens/ShapeSelectorScreen.java",
      "private class SelectBox extends ObjectSelectionList<ShapeEntry> {",
      "private class SelectBox extends ObjectSelectionList<SelectBox.ShapeEntry> {")
patch("common/src/main/java/" + PKG + "/common/advancement/UltimineObtainTrigger.java",
      "extends SimpleCriterionTrigger<Instance> {",
      "extends SimpleCriterionTrigger<UltimineObtainTrigger.Instance> {")

# report
def count(sub): return len(glob.glob(sub + "/src/main/java/**/*.java", recursive=True))
print("placed: common=%d forge=%d fabric=%d" % (count("common"), count("forge"), count("fabric")))
print("sets: common=%d forge_only=%d fabric_only=%d" % (len(common), len(forge_only), len(fab_only)))
resid = 0
for sub in ["common", "forge", "fabric"]:
    for p in glob.glob(sub + "/src/main/java/**/*.java", recursive=True):
        if re.search(r'\b(m_\d+_|f_\d+_|class_\d+|method_\d+|field_\d+)\b', open(p, encoding="utf-8").read()):
            resid += 1; print("RESIDUAL OBFUSCATION:", p)
print("residual-obfuscation files:", resid)
