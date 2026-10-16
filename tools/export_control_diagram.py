"""Export the frozen expanded Archify SVG without changing delivered HTML."""
from pathlib import Path
import re
import subprocess
import tempfile

OUTPUT = Path.home() / 'Downloads/AOOP_Milestone2_Group13'


def main():
    name = 'MotorPH_Expanded_Control_Diagram'
    original = (OUTPUT / (name + '.html')).read_text(encoding='utf-8')
    styles = '\n'.join(re.findall(r'<style[^>]*>([\s\S]*?)</style>', original))
    svg = re.search(r'<svg\b[\s\S]*?</svg>', original).group()
    override = 'svg [data-detail]{display:inline!important;opacity:1!important}html,body{margin:0!important;padding:0!important;background:white!important}svg{display:block!important;width:2730px!important;height:1440px!important;max-width:none!important}'
    standalone = svg.replace('>', '><style>' + styles + '\nsvg [data-detail]{display:inline!important;opacity:1!important}</style>', 1)
    (OUTPUT / (name + '.svg')).write_text(standalone, encoding='utf-8')
    chrome = Path('C:/Program Files/Google/Chrome/Application/chrome.exe')
    with tempfile.TemporaryDirectory(prefix='motorph-diagram-export-') as temp:
        page = Path(temp) / 'export.html'
        page.write_text('<!doctype html><html data-theme="light" data-detail="full"><meta charset="utf-8"><style>' + styles + override + '</style><body>' + svg + '</body></html>', encoding='utf-8')
        result = subprocess.run([str(chrome), '--headless', '--disable-gpu', '--hide-scrollbars', '--no-pdf-header-footer',
            '--window-size=2730,1440', '--screenshot=' + str(OUTPUT / (name + '.png')), page.as_uri()], capture_output=True, timeout=30)
        if result.returncode != 0:
            raise RuntimeError('Diagram export failed')
    print('Expanded diagram exported as PNG and self-contained SVG; trusted HTML unchanged.')


if __name__ == '__main__':
    main()
