#!/usr/bin/python3

from copy import deepcopy
import yaml
from os import walk

with open('.github/dependabot.template.yml') as f:
    data = yaml.safe_load(f)

entry = data['updates'].pop(-1)

for root, dirs, files in walk('examples'):
    for file in files:
        if file == 'build.gradle':
            entry['directory'] = root
            data['updates'].append(deepcopy(entry))

with open('.github/dependabot.yml', 'w') as f:
    f.write("# generated from scripts/generate-dependabot.py\n")
    yaml.safe_dump(data, f)


