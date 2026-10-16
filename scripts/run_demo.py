#!/usr/bin/env python3
"""Run the packaged Java services and a local gateway. Requires Java 21 and Python 3.9+."""
from contextlib import contextmanager
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import argparse
import signal
import socket
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
SERVICES = ['auth-service', 'user-service', 'doctor-service', 'appointment-service']
ROUTES = {'/api/auth/': 1, '/api/user-profiles': 2, '/api/doctors': 3, '/api/appointments': 4}

@contextmanager
def services(base_port=8080):
    """Own only the processes started here; always stop them when the demo or smoke test exits."""
    children, logs = [], []
    runtime = ROOT / '.runtime'
    runtime.mkdir(exist_ok=True)
    # Fail before starting anything if another application already owns one of our ports.
    for port in range(base_port, base_port + 5):
        with socket.socket() as probe:
            probe.bind(('127.0.0.1', port))
    try:
        for offset, service in enumerate(SERVICES, 1):
            jar = ROOT / service / 'target' / f'{service}-0.0.1-SNAPSHOT.jar'
            if not jar.exists():
                raise RuntimeError('Build first: bash auth-service/mvnw verify')
            log = open(runtime / f'{service}-{base_port}.log', 'w')
            logs.append(log)
            children.append(subprocess.Popen([
                'java', '-Xmx256m', '-jar', str(jar), '--spring.profiles.active=demo',
                f'--server.port={base_port + offset}', '--server.address=127.0.0.1',
                f'--services.doctor.url=http://127.0.0.1:{base_port + 3}'
            ], cwd=ROOT, stdout=log, stderr=subprocess.STDOUT))
        deadline = time.monotonic() + 90
        for offset, child in enumerate(children, 1):
            while True:
                if child.poll() is not None:
                    raise RuntimeError(f'{SERVICES[offset-1]} stopped. Check .runtime logs.')
                try:
                    with urlopen(f'http://127.0.0.1:{base_port + offset}/actuator/health', timeout=2) as response:
                        if response.status == 200:
                            break
                except (URLError, TimeoutError):
                    pass
                if time.monotonic() > deadline:
                    raise RuntimeError('Startup timed out. Check .runtime logs.')
                time.sleep(.3)
        yield
    finally:
        for child in children:
            if child.poll() is None:
                child.terminate()
        for child in children:
            try:
                child.wait(timeout=10)
            except subprocess.TimeoutExpired:
                child.kill()
                child.wait()
        for log in logs:
            log.close()


def gateway(base_port=8080):
    class Gateway(SimpleHTTPRequestHandler):
        def __init__(self, *args, **kwargs):
            super().__init__(*args, directory=str(ROOT / 'web'), **kwargs)

        def end_headers(self):
            self.send_header('X-Content-Type-Options', 'nosniff')
            self.send_header('Cache-Control', 'no-store')
            self.send_header('Content-Security-Policy', "default-src 'self'; style-src 'self'; script-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'")
            super().end_headers()

        def log_message(self, *args):
            pass  # Do not log API paths, user information, or authorization headers.

        def proxy(self):
            offset = next((offset for prefix, offset in ROUTES.items() if self.path.startswith(prefix)), None)
            if offset is None:
                self.send_error(404)
                return
            try:
                length = int(self.headers.get('Content-Length', '0'))
                if not 0 <= length <= 32768:
                    self.send_error(413)
                    return
                body = self.rfile.read(length) if length else None
                headers = {name: self.headers[name] for name in ('Authorization', 'Content-Type') if name in self.headers}
                request = Request(f'http://127.0.0.1:{base_port+offset}{self.path}', data=body, headers=headers, method=self.command)
                try:
                    response = urlopen(request, timeout=8)
                except HTTPError as error:
                    response = error
                with response:
                    data = response.read()
                    self.send_response(response.code)
                    self.send_header('Content-Type', response.headers.get('Content-Type', 'application/json'))
                    self.send_header('Content-Length', str(len(data)))
                    self.end_headers()
                    self.wfile.write(data)
            except (URLError, TimeoutError):
                self.send_error(502, 'Service unavailable')
            except ValueError:
                self.send_error(400)

        def do_GET(self):
            if self.path.startswith('/api/'):
                self.proxy()
            else:
                super().do_GET()

        do_POST = do_PUT = do_PATCH = proxy
    return ThreadingHTTPServer(('127.0.0.1', base_port), Gateway)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--port', type=int, default=8080)
    args = parser.parse_args()
    def stop(*_):
        raise KeyboardInterrupt
    signal.signal(signal.SIGTERM, stop)
    try:
        with services(args.port):
            print(f'Careway is ready: http://localhost:{args.port}', flush=True)
            print('Press Ctrl+C to stop. Demo data resets when stopped.', flush=True)
            with gateway(args.port) as server:
                server.serve_forever()
    except KeyboardInterrupt:
        print('\nDemo stopped.')
