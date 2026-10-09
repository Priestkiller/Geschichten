"""Audit new asset provenance and make review sheets without editing shipped images."""
from collections import Counter
from hashlib import sha256
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

output = Path(__file__).resolve().parent
project = output.parents[2]
source = json.loads((project / "docs/artwork-0.4.0/generated-files.json").read_text(encoding="utf-8"))["generated"]
prompts = json.loads((project / "docs/artwork-0.4.0/prompts.json").read_text(encoding="utf-8"))["portraits"]
assert len(source) == len(prompts) == 40
assert {p["id"] for p in source} == {p["id"] for p in prompts}
inventory = []
for record in source:
    path = Path(record["file"])
    assert path.read_bytes() == Path(record["original"]).read_bytes()
    with Image.open(path) as picture:
        assert picture.format == "PNG" and min(picture.size) >= 1000
        assert 0.73 <= picture.width / picture.height <= 0.86
        inventory.append({**record, "size": list(picture.size), "sha256": sha256(path.read_bytes()).hexdigest()})
assert len({i["sha256"] for i in inventory}) == 40
font = ImageFont.truetype("C:/Windows/Fonts/arial.ttf", 23)
small = ImageFont.truetype("C:/Windows/Fonts/arial.ttf", 16)
for index, category in enumerate(("Mittelerde", "Blade Runner", "Cyberpunk 2077", "Monster"), 1):
    figures = [f for f in inventory if f["genre"] == category]
    assert len(figures) == 10 and Counter(f["gender"] for f in figures) == {"weiblich": 5, "männlich": 5}
    canvas = Image.new("RGB", (1350, 840), "#10171e")
    draw = ImageDraw.Draw(canvas)
    draw.text((16, 10), category, font=font, fill="#e6b17d")
    for position, figure in enumerate(figures):
        x, y = (position % 5) * 270, 48 + (position // 5) * 392
        with Image.open(figure["file"]) as picture:
            picture = picture.convert("RGB")
            picture.thumbnail((256, 334))
            canvas.paste(picture, (x + (270 - picture.width) // 2, y))
        draw.text((x + 10, y + 337), figure["name"], font=font, fill="#f1eee7")
        draw.text((x + 10, y + 369), figure["gender"], font=small, fill="#adb5bc")
    canvas.save(output / f"portraits-{index}.jpg", quality=91)
(output / "new-portraits.json").write_text(json.dumps(inventory, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print("PASS: 40 unique original PNG portraits, 4 balanced categories, provenance and portrait dimensions verified.")
