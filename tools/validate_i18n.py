"""Validate offline UI catalogs without rewriting them; Python standard library only."""
from collections import Counter
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets/i18n"
languages = json.loads((ASSETS / "languages.json").read_text(encoding="utf-8"))
assert len(languages) == len(set(languages)), "Duplicate language options"
assert "en" in languages
canonical = json.loads((ASSETS / "en.json").read_text(encoding="utf-8"))
assert all(key == value for key, value in canonical.items()), "English keys are source text"
slots = re.compile(r"\{\d+\}")

for language in languages:
    assert re.fullmatch(r"[A-Za-z]+(?:-[A-Za-z0-9]+)*", language), language
    data = json.loads((ASSETS / f"{language}.json").read_text(encoding="utf-8"))
    assert set(data) == set(canonical), (language, "Catalog keys differ")
    for key, value in data.items():
        assert isinstance(value, str) and value.strip(), (language, key, "Empty translation")
        assert "\ufffd" not in value, (language, key, "Unicode replacement character")
        assert Counter(slots.findall(key)) == Counter(slots.findall(value)), (language, key, "Placeholder mismatch")

literal_keys = set()
for source in (ROOT / "app/src/main/java/com/hatsyrei/maidnative/ui").rglob("*.kt"):
    for literal in re.findall(r'\btr\(("(?:[^"\\]|\\.)*")', source.read_text(encoding="utf-8")):
        literal_keys.add(json.loads(literal))
assert literal_keys <= canonical.keys(), literal_keys - canonical.keys()
print(f"Validated {len(languages)} languages, {len(canonical)} keys each, "
      f"{len(languages) * len(canonical)} entries; no missing keys or placeholder mismatches.")
