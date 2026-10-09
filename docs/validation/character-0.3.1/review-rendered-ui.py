"""Preserve and arrange actual Robolectric/Compose screenshots for visual review."""
from pathlib import Path
import json
import shutil
from PIL import Image, ImageDraw, ImageFont, ImageOps

OUT = Path(__file__).resolve().parent
ROOT = OUT.parents[2]
inventory = json.loads((OUT / "catalog-inventory.json").read_text(encoding="utf-8"))
source = ROOT / "app/build/ui-screenshots/artwork-regression/all-50"
expected = {f'{kind}-{p["id"]}.png' for p in inventory["figures"] for kind in ("card", "chat")}
actual = {p.name for p in source.glob("*.png")}
assert actual == expected, (expected - actual, actual - expected)
saved = OUT / "rendered-all-50"
shutil.copytree(source, saved, dirs_exist_ok=True)
font = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 17)
for number, category in enumerate(inventory["categories"], 1):
    figures = [p for p in inventory["figures"] if p["category"] == category]
    for kind, size in (("card", (208, 280)), ("chat", (208, 451))):
        row_height = size[1] + 28
        sheet = Image.new("RGB", (1100, 43 + 2*row_height), "#10171c")
        draw = ImageDraw.Draw(sheet)
        draw.text((12, 8), f'{category} – tatsächliche {"Figurenkarten" if kind == "card" else "Chatansichten"}', font=font, fill="#f0cba7")
        for index, figure in enumerate(figures):
            x, y = (index % 5) * 220, 43 + (index // 5) * row_height
            with Image.open(saved / f'{kind}-{figure["id"]}.png') as im:
                thumb = ImageOps.contain(im.convert("RGB"), size, Image.Resampling.LANCZOS)
                sheet.paste(thumb, (x + (220-thumb.width)//2, y))
            draw.text((x+8, y+size[1]+4), figure["name"], font=font, fill="#f3f0eb")
        sheet.save(OUT / f"{kind}-review-{number}.jpg", quality=95)
print("PASS: preserved all 50 actual character cards and all 50 actual first-chat screenshots.")
