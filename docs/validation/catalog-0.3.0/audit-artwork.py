"""Audit unchanged portrait files and build review sheets; never modifies source artwork."""
from pathlib import Path
from collections import Counter
import hashlib
import json
import re
from PIL import Image, ImageDraw, ImageFont, ImageOps

ROOT = Path(__file__).resolve().parents[3]
OUT = Path(__file__).resolve().parent
DATA = ROOT / "app/src/main/java/dev/vincent/geschichten/data"
FEMALE = set("runa elara mira aelwyn nyra sylwen thora seris nessa pyra astrid sigrid liv solveig johanna vera nora ines hedda sana lyra keira ada thalora veshra".split())
original = (ROOT / "app/src/test/java/dev/vincent/geschichten/data/DeliveredV2CatalogFixture.kt").read_text(encoding="utf-8")
added = (DATA / "AdditionalCharacters.kt").read_text(encoding="utf-8")
pattern = re.compile(r'id = "([^\"]+)",\s*name = "([^\"]+)",\s*role = "([^\"]+)",\s*genre = "([^\"]+)"')
figures = [dict(zip(("id", "name", "role", "category"), row)) for row in pattern.findall(original) + pattern.findall(added)]
assert len(figures) == 50 and len({p["id"] for p in figures}) == 50
assert set(Counter(p["category"] for p in figures).values()) == {10}
assert len(FEMALE) == 25 and FEMALE <= {p["id"] for p in figures}
font_path = Path("C:/Windows/Fonts/segoeui.ttf")
font = ImageFont.truetype(str(font_path), 16)
small = ImageFont.truetype(str(font_path), 12)

for figure in figures:
    file = ROOT / f'app/src/main/res/drawable-nodpi/portrait_{figure["id"]}.png'
    with Image.open(file) as im:
        im.verify()
    with Image.open(file) as im:
        figure["dimensions"] = list(im.size)
    assert figure["dimensions"][0] >= 512 and figure["dimensions"][1] >= 640
    figure["gender"] = "weiblich" if figure["id"] in FEMALE else "männlich"
    figure["portrait"] = str(file.relative_to(ROOT))
    figure["sha256"] = hashlib.sha256(file.read_bytes()).hexdigest()

categories = list(dict.fromkeys(p["category"] for p in figures))
for number, category in enumerate(categories, 1):
    group = [p for p in figures if p["category"] == category]
    assert sum(p["id"] in FEMALE for p in group) == 5
    sheet = Image.new("RGB", (1100, 645), "#10171c")
    draw = ImageDraw.Draw(sheet)
    draw.text((15, 8), f'{category}: 5 weibliche und 5 männliche Figuren', font=font, fill="#f0cba7")
    for index, figure in enumerate(group):
        x, y = (index % 5) * 220, 42 + (index // 5) * 300
        with Image.open(ROOT / figure["portrait"]) as im:
            thumb = ImageOps.contain(im.convert("RGB"), (208, 250), Image.Resampling.LANCZOS)
            sheet.paste(thumb, (x + (220-thumb.width)//2, y))
        draw.text((x+8, y+254), f'{figure["name"]} ({figure["gender"]})', font=font, fill="#f3f0eb")
        draw.text((x+8, y+277), figure["role"], font=small, fill="#b9c0c9")
    sheet.save(OUT / f"portrait-review-{number}.jpg", quality=94)

(OUT / "catalog-inventory.json").write_text(json.dumps({"figures": figures, "count": 50, "categories": {c: {"total": 10, "female": 5, "male": 5} for c in categories}}, ensure_ascii=False, indent=2)+"\n", encoding="utf-8")
print("PASS: 50 distinct portrait files; 10 figures per category; 5 female and 5 male each.")
