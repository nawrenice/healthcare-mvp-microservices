// Keep the bearer token in memory. Reloading the page signs out; no persistent browser token storage.
let session = null;
let profileExists = false;
const $ = id => document.getElementById(id);
const values = form => Object.fromEntries(new FormData(form).entries());
function message(text, error = false) {
  $('message').textContent = text;
  $('message').className = error ? 'error' : 'success';
}
async function api(path, method = 'GET', data) {
  const response = await fetch(path, {
    method, headers: { ...(data ? {'Content-Type': 'application/json'} : {}),
      ...(session ? {Authorization: `Bearer ${session.token}`} : {}) },
    ...(data ? {body: JSON.stringify(data)} : {})
  });
  let body;
  try { body = await response.json(); } catch { body = {}; }
  if (!response.ok) {
    const detail = body.message || (body.errors ? Object.values(body.errors).join(', ') : null);
    const error = new Error(detail || `Request failed (${response.status}). Check the services and try again.`);
    error.status = response.status;
    throw error;
  }
  return body;
}
function onSubmit(id, handler) {
  $(id).addEventListener('submit', async event => {
    event.preventDefault();
    const button = event.currentTarget.querySelector('button');
    button.disabled = true;
    try { await handler(event.currentTarget); } catch (error) { message(error.message, true); }
    finally { button.disabled = false; }
  });
}
onSubmit('signup', async form => {
  await api('/api/auth/signup', 'POST', {...values(form), role: 'PATIENT'});
  form.reset(); message('Your account is ready. Sign in to get started.');
});
onSubmit('login', async form => {
  const result = await api('/api/auth/login', 'POST', values(form));
  if (!['PATIENT', 'DOCTOR'].includes(result.role)) throw new Error('This portal currently supports patient and doctor workspaces.');
  session = result;
  form.reset();
  $('auth').hidden = $('welcome').hidden = true;
  $('workspace').hidden = $('logout').hidden = false;
  $('patient-area').hidden = result.role !== 'PATIENT';
  document.querySelector('nav a[href="#directory"]').hidden = result.role !== 'PATIENT';
  $('patient-profile').hidden = result.role !== 'PATIENT';
  $('doctor-profile').hidden = result.role !== 'DOCTOR';
  $('profile-title').textContent = result.role === 'DOCTOR' ? 'Doctor profile' : 'Patient profile';
  $('role-label').textContent = `${result.role} WORKSPACE`;
  $('workspace-title').textContent = 'Your care, connected.';
  await refresh(); message('You’re signed in. Your workspace is ready.');
});
$('logout').onclick = () => location.reload();
$('refresh').onclick = () => refresh().catch(error => message(error.message, true));
function node(tag, text, className) {
  const element = document.createElement(tag);
  if (text !== undefined) element.textContent = text; // Use textContent for all server-supplied content.
  if (className) element.className = className;
  return element;
}
async function loadProfile() {
  const doctor = session.role === 'DOCTOR';
  const form = $(doctor ? 'doctor-profile' : 'patient-profile');
  try {
    const profile = await api(doctor ? '/api/doctors/me' : '/api/user-profiles/me');
    profileExists = true;
    for (const input of form.querySelectorAll('input')) {
      if (input.type === 'checkbox') input.checked = Boolean(profile[input.name]);
      else input.value = profile[input.name] || '';
    }
  } catch (error) {
    if (error.status !== 404) throw error;
    profileExists = false;
  }
}
async function loadDoctors() {
  if (session.role !== 'PATIENT') return;
  const {content} = await api('/api/doctors?size=100');
  $('doctor-list').replaceChildren();
  $('doctor-select').replaceChildren(new Option('Select a doctor', ''));
  if (!content.length) $('doctor-list').append(node('p', 'No doctors available yet. In demo mode, sign in as the doctor and create a profile first.', 'muted'));
  for (const doctor of content) {
    const row = node('div', undefined, 'item');
    const details = node('div');
    details.append(node('strong', doctor.name), node('p', doctor.specialty), node('p', doctor.clinicAddress, 'muted'));
    row.append(details); $('doctor-list').append(row);
    $('doctor-select').append(new Option(`${doctor.name} · ${doctor.specialty}`, doctor.id));
  }
}
async function loadAppointments() {
  const {content} = await api('/api/appointments/me?size=100');
  $('appointment-list').replaceChildren();
  if (!content.length) $('appointment-list').append(node('p', 'No appointments yet. Your upcoming visits will appear here.', 'muted'));
  for (const visit of content) {
    const row = node('div', undefined, 'item');
    const details = node('div');
    details.append(node('strong', new Date(visit.startsAt).toLocaleString()), node('p', visit.reason),
      node('p', `Visit #${visit.id} · ${session.role === 'DOCTOR' ? 'Patient #' + visit.patientAuthUserId : 'Doctor #' + visit.doctorId}`, 'muted'), node('span', visit.status, 'status'));
    const actions = node('div', undefined, 'actions');
    if (visit.status === 'BOOKED' && new Date(visit.startsAt) > new Date()) actions.append(action('Cancel visit', visit.id, 'cancel'));
    if (session.role === 'DOCTOR' && visit.status === 'BOOKED' && new Date(visit.endsAt) <= new Date()) actions.append(action('Complete visit', visit.id, 'complete'));
    row.append(details, actions); $('appointment-list').append(row);
  }
}
function action(label, id, operation) {
  const button = node('button', label);
  button.onclick = async () => {
    button.disabled = true;
    try { await api(`/api/appointments/${id}/${operation}`, 'PATCH'); await loadAppointments(); message('Appointment updated.'); }
    catch (error) { message(error.message, true); button.disabled = false; }
  };
  return button;
}
async function refresh() { await Promise.all([loadProfile(), loadDoctors(), loadAppointments()]); }
onSubmit('booking', async form => {
  const request = values(form);
  request.doctorId = Number(request.doctorId);
  request.startsAt = new Date(request.startsAt).toISOString();
  await api('/api/appointments', 'POST', request);
  form.reset(); await loadAppointments(); message('Your appointment is booked.');
});
onSubmit('patient-profile', async form => {
  const request = values(form);
  request.dateOfBirth = request.dateOfBirth || null;
  request.phone = request.phone || null;
  await api(profileExists ? '/api/user-profiles/me' : '/api/user-profiles', profileExists ? 'PUT' : 'POST', request);
  profileExists = true; message('Your profile is saved.');
});
onSubmit('doctor-profile', async form => {
  const request = values(form);
  request.available = form.elements.available.checked;
  await api(profileExists ? '/api/doctors/me' : '/api/doctors', profileExists ? 'PUT' : 'POST', request);
  profileExists = true; message('Your doctor profile is saved.');
});
