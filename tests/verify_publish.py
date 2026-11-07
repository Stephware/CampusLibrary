"""Verify fresh Release publish artifacts exclude local development settings."""
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def main():
    for project in ('CampusLibrary.WebApi', 'CampusLibrary.WebMvc'):
        with tempfile.TemporaryDirectory(prefix='campuslibrary-publish-') as folder:
            result = subprocess.run(
                ['dotnet', 'publish', str(ROOT / 'src' / project), '-c', 'Release',
                 '--no-restore', '-p:UseSharedCompilation=false', '-o', folder],
                cwd=ROOT, capture_output=True, text=True)
            if result.returncode:
                raise RuntimeError(result.stdout + result.stderr)
            assert not (Path(folder) / 'appsettings.Development.json').exists(), (
                project + ': local Development settings must not be shipped')
            print(project + ': published successfully without local Development settings')


if __name__ == '__main__':
    main()
