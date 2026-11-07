"""Build, migrate, test and drop a dedicated Milestone 4 SQL database.

Uses the existing Development connection settings, with only Database changed.
Does not change the running development API, its port, or CampusLibraryDb.
Requires SQL create/drop permissions and the existing dotnet-ef tool.
"""
import json
import os
from pathlib import Path
import re
import socket
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.request
import uuid

ROOT = Path(__file__).resolve().parents[1]
API = ROOT / 'src/CampusLibrary.WebApi'
EF = ['dotnet', 'ef', '-p', 'src/CampusLibrary.Data', '-s', 'src/CampusLibrary.WebApi']


def main():
    settings = json.loads((API / 'appsettings.Development.json').read_text())
    connection = os.environ.get('ConnectionStrings__DefaultConnection') or settings['ConnectionStrings']['DefaultConnection']
    database = 'CampusLibraryM4Tests_' + uuid.uuid4().hex[:12]
    connection, replacements = re.subn(r'(?i)(database|initial catalog)\s*=\s*[^;]+',
                                       'Database=' + database, connection)
    if replacements != 1:
        raise RuntimeError('Expected exactly one Database field in the development connection string.')
    env = {**os.environ, 'ASPNETCORE_ENVIRONMENT': 'Development',
           'DOTNET_ENVIRONMENT': 'Development', 'ConnectionStrings__DefaultConnection': connection}
    with socket.socket() as listener:
        listener.bind(('127.0.0.1', 0))
        port = listener.getsockname()[1]
    url = f'http://127.0.0.1:{port}'
    env['CAMPUSLIBRARY_TEST_URL'] = url
    process = None
    with tempfile.TemporaryDirectory(prefix='campuslibrary-m4-') as temporary:
        log_path = Path(temporary) / 'api.log'
        with log_path.open('w') as log:
            try:
                subprocess.run(['dotnet', 'build', '--no-restore', '-p:UseSharedCompilation=false', '-m:1'], cwd=ROOT, env=env, check=True)
                subprocess.run(EF[:2] + ['database', 'update'] + EF[2:] + ['--no-build'],
                               cwd=ROOT, env=env, check=True)
                process = subprocess.Popen(['dotnet', str(API / 'bin/Debug/net10.0/CampusLibrary.WebApi.dll'),
                                            '--urls', url], cwd=API, env=env, stdout=log, stderr=log)
                for attempt in range(120):
                    if process.poll() is not None:
                        raise RuntimeError('Test API exited unexpectedly.')
                    try:
                        with urllib.request.urlopen(url + '/api/members', timeout=1) as response:
                            if response.status == 200:
                                break
                    except (urllib.error.URLError, TimeoutError):
                        time.sleep(0.25)
                else:
                    raise RuntimeError('Test API did not become ready.')
                result = subprocess.run([sys.executable, '-m', 'unittest', 'discover', '-s', 'tests', '-v'],
                                        cwd=ROOT, env=env)
                if result.returncode:
                    print('API diagnostics:', log_path.read_text()[-16000:])
                return result.returncode
            finally:
                if process is not None:
                    process.terminate()
                    try:
                        process.wait(timeout=10)
                    except subprocess.TimeoutExpired:
                        process.kill()
                        process.wait()
                # env always points at the generated isolated database above.
                subprocess.run(EF[:2] + ['database', 'drop', '--force'] + EF[2:] + ['--no-build'],
                               cwd=ROOT, env=env, check=True)


if __name__ == '__main__':
    sys.exit(main())
