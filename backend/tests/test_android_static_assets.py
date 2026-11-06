# Static validation of the native Android project (no JDK/Gradle available): XML well-formedness, assets, strings
import glob
import json
import re
import xml.dom.minidom

import pytest

ROOT = "/app/android/WaqiahQuran/app/src/main"


# XML resources + manifest must parse
@pytest.mark.parametrize("path", [f"{ROOT}/AndroidManifest.xml"] + glob.glob(f"{ROOT}/res/**/*.xml", recursive=True))
def test_xml_well_formed(path):
    xml.dom.minidom.parse(path)


# surah_56.json: 96 verses, 4 translations x 96, no embedded verse digits
def test_surah_asset():
    s = json.load(open(f"{ROOT}/assets/quran/surah_56.json", encoding="utf-8"))
    assert s["surah"]["number"] == 56 and s["surah"]["verseCount"] == 96
    assert [v["number"] for v in s["verses"]] == list(range(1, 97))
    assert all(v["textHafs"].strip() and v["textTanzil"].strip() for v in s["verses"])
    assert {t["id"] for t in s["translations"]} == {"fa.makarem", "fa.fooladvand", "fa.ansarian", "en.sahih"}
    for t in s["translations"]:
        assert len(t["texts"]) == 96 and all(x.strip() for x in t["texts"]), t["id"]


# reciters.json: 5 reciters, 96 monotonic timings each
def test_reciters_asset():
    r = json.load(open(f"{ROOT}/assets/quran/reciters.json", encoding="utf-8"))["reciters"]
    assert len(r) == 5
    for rc in r:
        tm = rc["timings"]
        assert [t["verse"] for t in tm] == list(range(1, 97)), rc["id"]
        assert all(t["fromMs"] < t["toMs"] for t in tm)
        assert all(a["toMs"] <= b["fromMs"] for a, b in zip(tm, tm[1:]))


# Every R.string reference exists; no unescaped apostrophes
def test_string_resources():
    d = xml.dom.minidom.parse(f"{ROOT}/res/values/strings.xml")
    names = set()
    for s in d.getElementsByTagName("string"):
        names.add(s.getAttribute("name"))
        text = "".join(c.toxml() for c in s.childNodes)
        assert not re.search(r"(?<!\\)'", text) or text.startswith('"'), s.getAttribute("name")
    refs = set()
    for f in glob.glob("/app/android/WaqiahQuran/app/src/**/*.kt", recursive=True):
        refs |= set(re.findall(r"R\.string\.(\w+)", open(f, encoding="utf-8").read()))
    assert refs - names == set()


# Persian editions should use Persian Yeh/Kaf (U+06CC/U+06A9), not Arabic (U+064A/U+0643)
def test_persian_translations_normalized():
    s = json.load(open(f"{ROOT}/assets/quran/surah_56.json", encoding="utf-8"))
    bad = [t["id"] for t in s["translations"] if t["language"] == "fa" and re.search("[\u064a\u0643]", " ".join(t["texts"]))]
    assert bad == [], f"Arabic Yeh/Kaf found in: {bad}"
