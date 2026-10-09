"""Preserve actual Compose captures and arrange all 90 revised starts for visual review."""
from pathlib import Path
import json
import shutil
from PIL import Image, ImageDraw, ImageFont, ImageOps

out = Path(__file__).resolve().parent
project = out.parents[2]
catalog = json.loads((out / 'catalog-current.json').read_text(encoding='utf-8'))
source = project / 'app/build/ui-screenshots'
saved = out / 'native-screenshots'
shutil.copytree(source, saved, dirs_exist_ok=True)
for prefix in ('profile', 'start', 'end'):
    assert len(list((saved / 'profiles/all-90').glob(f'{prefix}-*.png'))) == 90
assert len(list((saved / 'artwork-regression/all-90').glob('*.png'))) == 180
font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 17)
for number, category in enumerate(dict.fromkeys(p['genre'] for p in catalog), 1):
    figures = [p for p in catalog if p['genre'] == category]
    for kind, size in (('start', (208, 451)), ('end', (208, 451)), ('profile', (208, 420))):
        height = size[1] + 28
        sheet = Image.new('RGB', (1100, 43 + 2 * height), '#10171c')
        draw = ImageDraw.Draw(sheet)
        draw.text((12, 8), f'{category} – tatsächliche Android-Ansichten: {kind}', font=font, fill='#f0cba7')
        for index, figure in enumerate(figures):
            x, y = index % 5 * 220, 43 + index // 5 * height
            with Image.open(saved / 'profiles/all-90' / f'{kind}-{figure["id"]}.png') as picture:
                thumb = ImageOps.contain(picture.convert('RGB'), size, Image.Resampling.LANCZOS)
                sheet.paste(thumb, (x + (220 - thumb.width) // 2, y))
            draw.text((x + 8, y + size[1] + 4), figure['name'], font=font, fill='#f3f0eb')
        sheet.save(out / f'{kind}-review-{number}.jpg', quality=95)
print('PASS: 90 start views, 90 readable ends, 90 profiles and 180 portrait views preserved; 27 review sheets.')
