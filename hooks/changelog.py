"""Builds the Changelog page from the CHANGELOG.md file on each version branch.

The page (docs/changelog.md) holds a `<!-- changelog -->` marker; at build time it is replaced with the latest
releases, the newest release's new features, and every release per Minecraft version. The files are read
from GitHub, so the page follows the branches without being edited by hand.
"""

import re
import urllib.request

REPO = "https://raw.githubusercontent.com/ixDarkLorD/UltimineAddition"
BRANCHES = [("26.1.2", "26.1.2"), ("1.21.1", "1.21.1"), ("1.20.1", "1.20.1")]
# One mod, its changelog at the branch root.
MODS = [("", "FTB Ultimine Addition", ":material-pickaxe:")]
MARKER = "<!-- changelog -->"
HEADING = re.compile(r"^## v?(?P<version>\S+)\s+(?:Release\s*)?(?:[|\-–]\s*)?(?P<rest>.*)$")


def fetch(branch, mod):
    try:
        with urllib.request.urlopen(f"{REPO}/{branch}/{mod + '/' if mod else ''}CHANGELOG.md", timeout=20) as response:
            return response.read().decode("utf-8")
    except Exception:
        return None


def releases(text):
    """The file's "## " sections, newest first: (version, details, body lines)."""
    result, current = [], None
    for line in text.splitlines():
        if line.startswith("## "):
            match = HEADING.match(line)
            version = match.group("version") if match else line[3:].strip()
            details = match.group("rest").strip() if match else ""
            current = (version, details, [])
            result.append(current)
        elif current is not None and line.strip() != "<hr>":
            current[2].append(line)
    return result


def status(details):
    """ "Unreleased (Minecraft 26.1.2)" -> "Unreleased"; "Oct 30, 2025" stays. """
    return re.sub(r"\s*\(.*\)$", "", details).strip() or "—"


def indent(lines, spaces):
    pad = " " * spaces
    return "\n".join(pad + line if line.strip() else "" for line in lines)


def bold_headings(lines):
    """Headings inside a release become bold lines, so they don't flood the table of contents."""
    return [f"**{line.lstrip('#').strip()}**" if line.startswith("#") else line for line in lines]


def subsection(body, title_fragment):
    """The lines of a "### ..." subsection whose title contains title_fragment."""
    out, inside = [], False
    for line in body:
        if line.startswith("### "):
            inside = title_fragment.lower() in line.lower()
            continue
        if inside:
            out.append(line)
    return out


def render():
    data = {(mod, branch): releases(text) for mod, _, _ in MODS for branch, _ in BRANCHES
            if (text := fetch(branch, mod)) is not None}
    if not data:
        return '!!! warning "Changelogs unavailable"\n    The changelogs could not be loaded while building this page.\n'

    out = ["## Latest releases", "", "| Mod | Minecraft | Version | Status |", "|---|---|---|---|"]
    for mod, name, icon in MODS:
        for branch, label in BRANCHES:
            entries = data.get((mod, branch))
            if entries:
                version, details, _ = entries[0]
                out.append(f"| {icon} {name} | {label} | `{version}` | {status(details)} |")
    out.append("")

    out += ["## What's new", ""]
    newest_branch = BRANCHES[0][0]
    for mod, name, icon in MODS:
        entries = data.get((mod, newest_branch))
        if not entries:
            continue
        version, details, body = entries[0]
        features = subsection(body, "New Features") or body
        out.append(f'???+ success "{name} {version} — {status(details)}"')
        out.append(indent(bold_headings(features), 4))
        out.append("")

    for mod, name, icon in MODS:
        out += [f"## {icon} {name}", ""]
        for branch, label in BRANCHES:
            entries = data.get((mod, branch))
            if not entries:
                continue
            out.append(f'=== "Minecraft {label}"')
            out.append("")
            for index, (version, details, body) in enumerate(entries):
                # Each release is a collapsible box (the newest open), so the table of contents stays short.
                box = "???+" if index == 0 else "???"
                title = f"{version} — {details}" if details else version
                out.append(indent([f'{box} {"success" if index == 0 else "note"} "{title}"'], 4))
                out.append(indent(bold_headings(body), 8))
                out.append("")
        out.append("")
    return "\n".join(out)


def on_page_markdown(markdown, page, config, files):
    if MARKER in markdown:
        return markdown.replace(MARKER, render())
    return markdown
