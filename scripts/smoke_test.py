#!/usr/bin/env python3
"""Cross-service smoke test: real HTTP, real tokens, isolated in-memory databases."""
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone, timedelta
from urllib.request import Request, urlopen
from urllib.error import HTTPError
import json
import threading
from run_demo import services, gateway

PORT = 18080
checks = 0

def call(path, method='GET', data=None, token=None, expected=200):
    global checks
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    request = Request(f'http://127.0.0.1:{PORT}{path}', method=method, headers=headers,
                      data=json.dumps(data).encode() if data is not None else None)
    try:
        response = urlopen(request, timeout=10)
    except HTTPError as error:
        response = error
    with response:
        content = response.read()
        assert response.code == expected, f'{method} {path}: expected {expected}, received {response.code}: {content[:500]!r}'
        checks += 1
        return json.loads(content) if content else {}

def main():
    with services(PORT), gateway(PORT) as server:
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        try:
            doctor = call('/api/auth/login', 'POST', {'email': 'doctor@example.test', 'password': 'DemoDoctor123!'})['token']
            directory = call('/api/doctors', 'POST', {'name': 'Dr Synthetic', 'specialty': 'Primary Care',
                'licenseNumber': 'DEMO-LICENSE', 'clinicAddress': 'Demo clinic', 'available': True}, doctor, 201)
            tokens = []
            for index in range(2):
                email = f'patient{index}@example.test'
                call('/api/auth/signup', 'POST', {'name': 'Synthetic Patient', 'email': email,
                    'password': 'PatientPassword123!', 'role': 'PATIENT'}, expected=201)
                tokens.append(call('/api/auth/login', 'POST', {'email': email, 'password': 'PatientPassword123!'})['token'])
            call('/api/auth/signup', 'POST', {'name': 'Fake Staff', 'email': 'fake@example.test',
                'password': 'LongPassword123!', 'role': 'ADMIN'}, expected=400)
            profile = call('/api/user-profiles', 'POST', {'authUserId': 999, 'name': 'Synthetic Patient',
                'phone': '5551234567', 'address': 'Demo address', 'dateOfBirth': '1990-01-01', 'gender': 'unspecified'}, tokens[0], 201)
            assert profile['authUserId'] != 999, 'Profile trusted a forged identity'
            call('/api/user-profiles/me', token=tokens[1], expected=404)
            call('/api/appointments/me', expected=401)
            doctors = call('/api/doctors', token=tokens[0])
            assert doctors['content'][0]['id'] == directory['id']
            start = datetime.now(timezone.utc) + timedelta(days=1)
            start = start.replace(minute=0, second=0, microsecond=0)
            payload = {'doctorId': directory['id'], 'startsAt': start.isoformat(), 'reason': 'Synthetic checkup'}
            visit = call('/api/appointments', 'POST', payload, tokens[0], 201)
            call(f'/api/appointments/{visit["id"]}', token=tokens[1], expected=403)
            call('/api/appointments', 'POST', payload, tokens[1], 409)
            call(f'/api/appointments/{visit["id"]}/cancel', 'PATCH', token=tokens[0])
            call('/api/appointments', 'POST', payload, tokens[1], 201)
            visits = call('/api/appointments/me', token=doctor)
            assert len(visits['content']) == 2
            # Real simultaneous HTTP requests, not just sequential uniqueness checks.
            payload['startsAt'] = (start + timedelta(hours=1)).isoformat()
            def book(token):
                request = Request(f'http://127.0.0.1:{PORT}/api/appointments', method='POST',
                    headers={'Authorization':'Bearer ' + token, 'Content-Type':'application/json'}, data=json.dumps(payload).encode())
                try:
                    with urlopen(request, timeout=10) as result: return result.status
                except HTTPError as error:
                    return error.code
            with ThreadPoolExecutor(max_workers=2) as pool:
                statuses = sorted(pool.map(book, tokens))
            assert statuses == [201, 409], f'Concurrent booking produced {statuses}'
            print(f'PASS: {checks} HTTP checks and concurrent booking across all four services.')
        finally:
            server.shutdown()
            thread.join(timeout=5)

if __name__ == '__main__':
    main()
