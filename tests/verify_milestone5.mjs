/** Local headless Chrome checks. Usage: node tests/verify_milestone5.mjs MVC_URL API_URL [OUTPUT_DIR]
 * Run only against the disposable apps started by run_milestone5.py --keep-running.
 * No npm packages required; uses the installed Chrome and Node 22 WebSocket.
 */
import { spawn } from 'node:child_process';
import { mkdtemp, readFile, writeFile, mkdir, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';

const [mvc, api, output = '/tmp/campuslibrary-m5-review'] = process.argv.slice(2);
if (!mvc || !api) throw new Error('Pass isolated MVC_URL and API_URL.');
if (!/^http:\/\/(127\.0\.0\.1|localhost):\d+$/.test(mvc) || !/^http:\/\/(127\.0\.0\.1|localhost):\d+$/.test(api))
  throw new Error('Only local test servers are supported.');
await mkdir(output, { recursive: true });
const profile = await mkdtemp(join(tmpdir(), 'campuslibrary-chrome-'));
const chrome = spawn('/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', [
  '--headless=new', '--no-first-run', '--no-default-browser-check', '--disable-extensions',
  '--remote-debugging-port=0', `--user-data-dir=${profile}`, 'about:blank'
], { stdio: 'ignore' });
const delay = ms => new Promise(resolve => setTimeout(resolve, ms));
let socket;
let loan;
const diagnostics = [];
const results = [];
async function apiCall(path, body) {
  const response = await fetch(api + path, { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) });
  assert.equal(response.status, 201);
  return response.json();
}
try {
  let port;
  for (let attempt = 0; attempt < 120; attempt++) {
    try { port = (await readFile(join(profile, 'DevToolsActivePort'), 'utf8')).split('\n')[0]; break; }
    catch { await delay(100); }
  }
  if (!port) throw new Error('Headless Chrome did not start.');
  const tabs = await (await fetch(`http://127.0.0.1:${port}/json/list`)).json();
  socket = new WebSocket(tabs.find(t => t.type === 'page').webSocketDebuggerUrl);
  await new Promise((resolve, reject) => { socket.addEventListener('open', resolve, {once:true}); socket.addEventListener('error', reject, {once:true}); });
  const pending = new Map();
  let nextId = 0;
  socket.addEventListener('message', event => {
    const message = JSON.parse(event.data);
    if (message.id) {
      const promise = pending.get(message.id);
      if (!promise) return;
      clearTimeout(promise.timer);
      pending.delete(message.id);
      if (message.error) promise.reject(new Error(JSON.stringify(message.error))); else promise.resolve(message.result);
    } else if (message.method === 'Runtime.exceptionThrown') diagnostics.push(message.params);
  });
  function call(method, params = {}) {
    const id = ++nextId;
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => { pending.delete(id); reject(new Error('CDP timeout: ' + method)); }, 15000);
      pending.set(id, {resolve, reject, timer});
      socket.send(JSON.stringify({id, method, params}));
    });
  }
  async function evaluate(expression) {
    const result = await call('Runtime.evaluate', {expression, returnByValue:true, awaitPromise:true});
    if (result.exceptionDetails) throw new Error(JSON.stringify(result.exceptionDetails));
    return result.result.value;
  }
  async function navigate(path) {
    await call('Page.navigate', {url:mvc + path});
    for (let attempt = 0; attempt < 100; attempt++) {
      if (await evaluate(`location.pathname === ${JSON.stringify(path.split('?')[0])} && document.readyState === 'complete'`)) return;
      await delay(50);
    }
    throw new Error('Navigation did not finish: ' + path);
  }
  async function capture(name) {
    await evaluate('document.fonts.ready.then(() => window.scrollTo(0,0))');
    const dimensions = await evaluate(`({width:innerWidth,scroll:document.documentElement.scrollWidth,title:document.title,headings:document.querySelectorAll('h1').length,background:getComputedStyle(document.body).backgroundColor,main:!!document.querySelector('main'),labels:[...document.querySelectorAll('input:not([type=hidden])')].every(i=>!!document.querySelector('label[for="'+i.id+'"]'))})`);
    assert.ok(dimensions.scroll <= dimensions.width + 1, name + ': full-page horizontal overflow');
    assert.equal(dimensions.headings, 1, name + ': exactly one primary heading');
    assert.ok(dimensions.labels, name + ': every input has a label');
    assert.ok(dimensions.main);
    const metrics = await call('Page.getLayoutMetrics');
    const screenshot = await call('Page.captureScreenshot', {format:'png', captureBeyondViewport:true,
      clip:{x:0,y:0,width:dimensions.width,height:Math.ceil(metrics.cssContentSize.height),scale:1}});
    await writeFile(join(output, name + '.png'), Buffer.from(screenshot.data,'base64'));
    results.push({name,...dimensions});
  }
  await call('Page.enable');
  await call('Runtime.enable');
  await call('Emulation.setEmulatedMedia', {features:[{name:'prefers-reduced-motion',value:'reduce'}]});
  const tag = randomUUID().replaceAll('-','').slice(0,12);
  const book = await apiCall('/api/books', {title:'Responsive layout verification — ' + 'A long title '.repeat(12),isbn:tag,author:'An author with a deliberately long name for layout verification',yearPublished:2024});
  const member = await apiCall('/api/members', {name:'Visual verification member',email:tag+'@example.com',studentId:tag});
  loan = await apiCall('/api/loans', {bookId:book.id,memberId:member.id});
  for (const [size,width,height] of [['desktop',1440,1000],['mobile',390,844]]) {
    await call('Emulation.setDeviceMetricsOverride', {width,height,deviceScaleFactor:1,mobile:size==='mobile'});
    for (const [path,name] of [['/Books','books'],['/Members','members'],['/Loans','loans'],['/Books/Create','create-book'],[`/Books/Edit/${book.id}`,'edit-book'],[`/Books/Delete/${book.id}`,'delete-book'],['/Members/Create','create-member'],['/Books/Edit/2147483647','not-found']]) {
      await navigate(path);
      await capture(`${size}-${name}`);
    }
    await navigate('/Books/Create');
    await evaluate(`document.querySelector('#Title').value=' ';document.querySelector('#Isbn').value='${tag}';document.querySelector('form').noValidate=true;document.querySelector('form').submit()`);
    for (let attempt=0;attempt<100;attempt++) {
      if (await evaluate(`document.readyState === 'complete' && !!document.querySelector('.field-validation-error')`)) break;
      await delay(50);
    }
    assert.ok(await evaluate(`!!document.querySelector('.field-validation-error')`), 'server validation visible');
    await capture(`${size}-validation`);
    await navigate('/Books');
    if (size === 'mobile') {
      await evaluate(`document.querySelector('.navbar-toggler').click()`);
      for(let attempt=0;attempt<40;attempt++) {
        if(await evaluate(`document.querySelector('#library-navigation').classList.contains('show')`)) break;
        await delay(50);
      }
      assert.equal(await evaluate(`document.querySelector('.navbar-toggler').getAttribute('aria-expanded')`), 'true');
      await capture('mobile-navigation');
    } else {
      await evaluate('document.activeElement.blur()');
      await call('Input.dispatchKeyEvent', {type:'keyDown',key:'Tab',code:'Tab',windowsVirtualKeyCode:9});
      await call('Input.dispatchKeyEvent', {type:'keyUp',key:'Tab',code:'Tab',windowsVirtualKeyCode:9});
      assert.equal(await evaluate('document.activeElement.textContent.trim()'), 'Skip to content');
      await capture('desktop-keyboard-focus');
    }
  }
  // Drive the real return form in the browser, then verify persistence through the API.
  await navigate('/Loans');
  await evaluate(`document.querySelector('form[action="/Loans/Return/${loan.id}"] button').click()`);
  for(let attempt=0;attempt<100;attempt++) {
    if(await evaluate(`document.readyState==='complete' && !!document.querySelector('[role=status]')`)) break;
    await delay(50);
  }
  assert.match(await evaluate('document.body.innerText'), /Book returned/);
  const returned = await (await fetch(`${api}/api/loans/${loan.id}`)).json();
  assert.ok(returned.returnDate);
  loan = null;
  await capture('mobile-return-success');
  assert.match(await evaluate('document.body.innerText'), /No open loans/);
  for(const file of ['/css/site.css','/js/site.js','/lib/bootstrap/dist/css/bootstrap.min.css','/lib/jquery-validation-unobtrusive/dist/jquery.validate.unobtrusive.min.js'])
    assert.equal((await fetch(mvc+file)).status,200,file);
  assert.equal(diagnostics.length,0,'No uncaught JavaScript exceptions');
  await writeFile(join(output,'results.json'),JSON.stringify({results,diagnostics},null,2));
  console.log(`PASS: ${results.length} rendered states; desktop/mobile, no document overflow, labels, keyboard focus, menu, validation and persisted return. Screenshots: ${output}`);
} finally {
  if(loan) await fetch(`${api}/api/loans/${loan.id}/return`,{method:'POST'});
  socket?.close();
  chrome.kill('SIGTERM');
  await new Promise(resolve => chrome.once('exit',resolve));
  await rm(profile,{recursive:true,force:true,maxRetries:5,retryDelay:200});
}
