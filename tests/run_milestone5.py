"""Build, migrate, test and drop a dedicated Milestone 5 SQL database.

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
    database = 'CampusLibraryM5Tests_' + uuid.uuid4().hex[:12]
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
    with socket.socket() as listener:
        listener.bind(('127.0.0.1', 0))
        mvc_port = listener.getsockname()[1]
    mvc_url = f'http://127.0.0.1:{mvc_port}'
    env['CAMPUSLIBRARY_MVC_TEST_URL'] = mvc_url
    mvc_process = None
    process = None
    with tempfile.TemporaryDirectory(prefix='campuslibrary-m5-') as temporary:
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
                mvc_dir = ROOT / 'src/CampusLibrary.WebMvc'
                mvc_process = subprocess.Popen(['dotnet', str(mvc_dir / 'bin/Debug/net10.0/CampusLibrary.WebMvc.dll'),
                                                '--urls', mvc_url], cwd=mvc_dir, env=env, stdout=log, stderr=log)
                for attempt in range(120):
                    if mvc_process.poll() is not None:
                        raise RuntimeError('Test MVC app exited unexpectedly.')
                    try:
                        with urllib.request.urlopen(mvc_url, timeout=1):
                            break
                    except (urllib.error.URLError, TimeoutError):
                        time.sleep(0.25)
                else:
                    raise RuntimeError('Test MVC app did not become ready.')
                print('MVC test URL:', mvc_url, flush=True)
                result = subprocess.run([sys.executable, '-m', 'unittest', 'discover', '-s', 'tests', '-v'],
                                        cwd=ROOT, env=env)
                if result.returncode:
                    print('API diagnostics:', log_path.read_text()[-16000:])
                if '--browser' in sys.argv and result.returncode == 0:
                    subprocess.run(['node', 'tests/verify_milestone5.mjs', mvc_url, url,
                                    str(ROOT / '.impeccable/review/web')], cwd=ROOT, env=env, check=True)
                if '--keep-running' in sys.argv:
                    print('API test URL:', url, flush=True)
                    input('Isolated apps ready for visual verification. Press Enter to stop and remove test database. ')
                return result.returncode
            finally:
                if mvc_process is not None:
                    mvc_process.terminate()
                    try:
                        mvc_process.wait(timeout=10)
                    except subprocess.TimeoutExpired:
                        mvc_process.kill()
                        mvc_process.wait()
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
