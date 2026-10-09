"""Arrange actual Android/Compose captures; never modify shipped portraits."""
from pathlib import Path
import json
import shutil
from PIL import Image, ImageDraw, ImageFont, ImageOps

out = Path(__file__).resolve().parent
project = out.parents[2]
catalog = json.loads((out / "catalog-current.json").read_text(encoding="utf-8"))
source = project / "app/build/ui-screenshots/artwork-regression/all-90"
expected = {f'{kind}-{p["id"]}.png' for p in catalog for kind in ("card", "chat")}
assert {p.name for p in source.glob("*.png")} == expected
saved = out / "rendered-all-90"
shutil.copytree(source, saved, dirs_exist_ok=True)
shutil.copytree(project / "app/build/ui-screenshots", out / "native-screenshots", dirs_exist_ok=True)
font = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 17)
for number, category in enumerate(("Mittelerde", "Blade Runner", "Cyberpunk 2077", "Monster"), 1):
    figures = [p for p in catalog if p["genre"] == category]
    for kind, size in (("card", (208, 280)), ("chat", (208, 451))):
        row_height = size[1] + 28
        sheet = Image.new("RGB", (1100, 43 + 2 * row_height), "#10171c")
        draw = ImageDraw.Draw(sheet)
        draw.text((12, 8), f'{category} – tatsächliche {"Figurenkarten" if kind == "card" else "Chatansichten"}', font=font, fill="#f0cba7")
        for index, figure in enumerate(figures):
            x, y = (index % 5) * 220, 43 + (index // 5) * row_height
            with Image.open(saved / f'{kind}-{figure["id"]}.png') as picture:
                thumb = ImageOps.contain(picture.convert("RGB"), size, Image.Resampling.LANCZOS)
                sheet.paste(thumb, (x + (220 - thumb.width) // 2, y))
            draw.text((x + 8, y + size[1] + 4), figure["name"], font=font, fill="#f3f0eb")
        sheet.save(out / f"{kind}-review-{number}.jpg", quality=95)
print("PASS: preserved 90 actual character cards and 90 first-chat screenshots.")
