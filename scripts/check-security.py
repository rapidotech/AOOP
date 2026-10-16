"""Focused C1/C2 source guard. Prints locations, never credential values."""
from pathlib import Path
import argparse
import csv
import re

parser = argparse.ArgumentParser()
parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[1])
args = parser.parse_args()
source = args.root / 'AOOP - MotorPH from OOP/src/oop_motorph'
problems = []
config = source / 'DatabaseConfig.java'
if config.exists() and re.search(r'\b(?:PASSWORD|USERNAME)\s*=\s*"[^"\r\n]+"', config.read_text(encoding='utf-8')):
    problems.append('DatabaseConfig.java contains an embedded credential literal')
seed = source / 'resources/CSV_Users.csv'
if seed.exists():
    with seed.open(encoding='utf-8-sig', newline='') as stream:
        rows = list(csv.reader(stream))
    if len(rows) > 1:
        problems.append('CSV_Users.csv contains reusable account seed records')
if problems:
    print('\n'.join(problems))
    raise SystemExit(1)
print('Focused security guard passed: no embedded configuration credentials or reusable account seeds in current source.')
