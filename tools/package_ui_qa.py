"""Export only validated synthetic UI captures and sanitized verification reports."""
from pathlib import Path
import hashlib
import html
import json
import re
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]
RUN = Path.home() / '.codex/tmp/astra-runs/aoop-20261017-ui-overhaul'
OUTPUT = Path.home() / 'Downloads/MotorPH_UI_Overhaul'
PORTFOLIO = Path.home() / 'Downloads/AOOP_Milestone2_Group13'


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    images = OUTPUT / 'screenshots'
    images.mkdir(exist_ok=True)
    final = ROOT / '.impeccable/review/round2'
    sources = {p.name: p for p in final.glob('*.png')}
    sources.update({p.name: p for p in (final / 'request-create').glob('*.png')})
    sources.update({p.name: p for p in (ROOT / '.impeccable/review/f6-correction').glob('*.png')})
    if len(sources) != 42:
        raise ValueError('The complete reviewed final capture matrix is required')
    hashes = []
    for name, source in sorted(sources.items()):
        shutil.copyfile(source, images / name)
        hashes.append({'file': name, 'sha256': hashlib.sha256(source.read_bytes()).hexdigest()})
    before = OUTPUT / 'before'
    before.mkdir(exist_ok=True)
    for p in (RUN / 'baseline').glob('*.png'):
        shutil.copyfile(p, before / p.name)
    report_files = {
        ROOT / '.gstack/qa-reports/qa-report-motorph-desktop-2026-10-17.md': 'QA_REPORT.md',
        ROOT / '.gstack/qa-reports/impeccable-finish-review-2026-10-17.md': 'INITIAL_VISUAL_REVIEW.md',
        ROOT / '.gstack/qa-reports/impeccable-finish-verdict-2026-10-17.md': 'FINAL_VISUAL_VERDICT.md',
        ROOT / 'DESIGN.md': 'DESIGN.md', ROOT / 'docs/UI_REDESIGN.md': 'UI_GUIDE.md',
        RUN / 'final-release-gate.txt': 'final-release-gate.txt',
        RUN / 'native-workflow/native-workflow-results.txt': 'native-workflow-results.txt',
    }
    for source, name in report_files.items():
        shutil.copyfile(source, OUTPUT / name)
    gate = (RUN / 'final-release-gate.txt').read_text(encoding='utf-8').replace('\r', '')
    counts = {name: int(value) for name, value in re.findall(r'^(.+?) regression checks passed: (\d+) assertions', gate, re.M)}
    if sum(counts.values()) != 351 or 'Complete local release gate passed' not in gate:
        raise ValueError('Complete final release evidence required')
    native = (RUN / 'native-workflow/native-workflow-results.txt').read_text(encoding='utf-8')
    if native.count('\nPASS ') != 38:
        raise ValueError('Complete real UI workflow evidence required')
    results = {'status': 'DONE_WITH_CONCERNS', 'application_revision': subprocess.check_output(['git', 'log', '-1', '--format=%H', '--', 'AOOP - MotorPH from OOP/src'], cwd=ROOT).decode().strip(),
        'offline_regression_assertions': counts, 'offline_total': 351, 'native_component_matrix_checks': 158,
        'targeted_create_and_empty_checks': 18, 'real_ui_to_clone_checks': 38, 'backend_runtime_checks': 76, 'sql_chain_checks': 15,
        'final_captures': 42, 'visual_disposition': 'ship; all six scored corrections resolved',
        'evidence_kind': 'Owned Swing window rendering and direct component actions; synthetic captures; real workflow effects verified only on owned clone3307',
        'excluded': 'Incomplete global Robot pass retained privately; no unrelated desktop captures included',
        'limits': ['Physical OS keyboard/mouse/focus not tested end to end', 'Physical printing and performance benchmarks not established', 'Original database and independent archive custody boundaries remain as documented'],
        'images': hashes}
    (OUTPUT / 'QA_RESULTS.json').write_text(json.dumps(results, indent=2), encoding='utf-8')
    # Keep later corrective proof separate from the original reviewed matrix.
    startup = ROOT / '.impeccable/review/startup-fix'
    workflow = ROOT / '.impeccable/review/startup-workflows/native-workflow-results.txt'
    if (startup / 'native-run.txt').is_file() and workflow.is_file():
        correction = OUTPUT / 'startup-fix'
        correction.mkdir(exist_ok=True)
        for name in ('login-desktop.png', 'login-laptop.png', 'native-run.txt', 'capture-provenance.json'):
            shutil.copyfile(startup / name, correction / name)
        shutil.copyfile(workflow, correction / workflow.name)
        results['startup_correction'] = {
            'cause': 'Production launcher used Metal while prior QA forced Nimbus',
            'fix': 'Shared AppTheme installer selects Nimbus; QA starts from Metal',
            'offline_assertions': 353, 'native_matrix_checks': 163,
            'ui_to_clone_checks': 38, 'proof': 'startup-fix/',
            'scope': 'Post-delivery correction; original release counts above are historical'}
        (OUTPUT / 'QA_RESULTS.json').write_text(json.dumps(results, indent=2), encoding='utf-8')
    cards = ''.join('<figure><a href="screenshots/' + html.escape(name) + '"><img loading="lazy" src="screenshots/' + html.escape(name) + '" alt="' + html.escape(name.removesuffix('.png').replace('-', ' ')) + '"></a><figcaption>' + html.escape(name.removesuffix('.png').replace('-', ' ')) + '</figcaption></figure>' for name in sorted(sources))
    (OUTPUT / 'index.html').write_text('''<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>MotorPH UI and QA</title><style>body{margin:0;background:#f4f6f0;color:#1b2d26;font:15px/1.6 "Segoe UI",sans-serif}header{padding:36px 5%;background:#14392b;color:white}h1{margin:0 0 8px;font-size:32px}header p{max-width:75ch}main{padding:28px 5%}nav a{color:#16704c;margin-right:24px}section{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,500px),1fr));gap:24px}figure{margin:0;background:white;padding:14px}img{width:100%;height:auto;display:block}figcaption{padding-top:10px}a:focus-visible{outline:3px solid #16704c;outline-offset:4px}</style><header><h1>MotorPH desktop overhaul</h1><p>Responsive Employee and HR workspaces, rebuilt sign-in, focused task forms and a consistent native design system. All screenshots use synthetic records.</p></header><main><nav><a href="QA_REPORT.md">QA report</a><a href="FINAL_VISUAL_VERDICT.md">Independent verdict</a><a href="QA_RESULTS.json">Verification results</a></nav><p>351 regression assertions · 176 native component checks · 38 UI-to-database checks · 76 backend and 15 SQL/chain checks. Direct Swing actions and owned-window rendering; physical OS input and printing are separate claims.</p><section>''' + cards + '</section></main></html>', encoding='utf-8')
    # Add the current evidence to the existing security portfolio without replacing historical captures.
    public = PORTFOLIO / 'evidence/ui/overhaul'
    public.mkdir(parents=True, exist_ok=True)
    for p in images.glob('*.png'):
        shutil.copyfile(p, public / p.name)
    shutil.copyfile(OUTPUT / 'QA_RESULTS.json', public / 'qa-results.json')
    if (OUTPUT / 'startup-fix').is_dir():
        shutil.copytree(OUTPUT / 'startup-fix', public / 'startup-fix', dirs_exist_ok=True)
    shutil.copyfile(OUTPUT / 'QA_REPORT.md', PORTFOLIO / 'evidence/ui-overhaul-qa-report.txt')
    shutil.copyfile(OUTPUT / 'FINAL_VISUAL_VERDICT.md', PORTFOLIO / 'evidence/ui-overhaul-visual-verdict.txt')
    archive = OUTPUT.with_suffix('.zip')
    files = sorted(p for p in OUTPUT.rglob('*') if p.is_file() and p.name != 'manifest.json')
    manifest = [{'file': p.relative_to(OUTPUT).as_posix(), 'sha256': hashlib.sha256(p.read_bytes()).hexdigest()} for p in files]
    (OUTPUT / 'manifest.json').write_text(json.dumps(manifest, indent=2), encoding='utf-8')
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as z:
        for p in files + [OUTPUT / 'manifest.json']:
            z.write(p, p.relative_to(OUTPUT))
    with zipfile.ZipFile(archive) as z:
        if z.testzip() is not None:
            raise ValueError('QA archive CRC failed')
    print('Validated UI evidence exported:42 final synthetic captures; archive ' + str(archive))


if __name__ == '__main__':
    main()
