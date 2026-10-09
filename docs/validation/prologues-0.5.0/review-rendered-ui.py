"""Preserve and arrange actual production Compose captures for visual inspection."""
from pathlib import Path
import json
import shutil
from PIL import Image, ImageDraw, ImageFont, ImageOps

out = Path(__file__).resolve().parent
project = out.parents[2]
catalog = json.loads((out / "catalog-current.json").read_text(encoding="utf-8"))
source = project / "app/build/ui-screenshots"
saved = out / "native-screenshots"
shutil.copytree(source, saved, dirs_exist_ok=True)
assert len(list((saved / "artwork-regression/all-90").glob("*.png"))) == 180
assert len(list((saved / "profiles/all-90").glob("*.png"))) == 90
font = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 17)
for number, category in enumerate(dict.fromkeys(p["genre"] for p in catalog), 1):
    figures = [p for p in catalog if p["genre"] == category]
    for kind, size in (("card", (208, 280)), ("chat", (208, 451)), ("profile", (208, 420))):
        directory = saved / ("profiles/all-90" if kind == "profile" else "artwork-regression/all-90")
        height = size[1] + 28
        sheet = Image.new("RGB", (1100, 43 + 2 * height), "#10171c")
        draw = ImageDraw.Draw(sheet)
        draw.text((12, 8), f"{category} – tatsächliche Android-Ansichten: {kind}", font=font, fill="#f0cba7")
        for index, figure in enumerate(figures):
            x, y = index % 5 * 220, 43 + index // 5 * height
            with Image.open(directory / f'{kind}-{figure["id"]}.png') as picture:
                thumb = ImageOps.contain(picture.convert("RGB"), size, Image.Resampling.LANCZOS)
                sheet.paste(thumb, (x + (220 - thumb.width) // 2, y))
            draw.text((x + 8, y + size[1] + 4), figure["name"], font=font, fill="#f3f0eb")
        sheet.save(out / f"{kind}-review-{number}.jpg", quality=95)
print("PASS: 90 cards, 90 first chats, 90 personality dialogs preserved; 27 review sheets.")
