// Headless clients that count every clientbound packet (count + bytes) by type.
// Bytes are decoded packet payload sizes, excluding TCP/packet framing and compression.
// Used as a proxy for client load: the server cannot measure client FPS, but it controls
// how many packets, explosions, particles and entity updates a client has to process.
//
// usage: node packet-counter.js <host> <port> <bots> <seconds> <out.json>
'use strict'
const mc = require('minecraft-protocol')
const fs = require('fs')

const [host = '127.0.0.1', port = '25565', botCount = '4', seconds = '600', out = 'packets.json'] = process.argv.slice(2)
const VERSION = '1.21.11'

const totals = {}
const perBot = []
const sessions = []
const keepAliveTimeout = Number(process.env.WATERBENCH_KEEPALIVE_TIMEOUT_MS || 300000)
let started = 0
let measuring = false
let ended = 0

function count (stats, name, bytes) {
  const s = stats[name] || (stats[name] = { count: 0, bytes: 0 })
  s.count++
  s.bytes += bytes
}

function startBot (i) {
  const stats = {}
  perBot.push(stats)
  const session = { index: i, reachedPlay: false, firstPlayAtUnixMs: null, ended: false, endedAtUnixMs: null, reason: null, explosions: [], scenarios: [], activeScenario: null, phaseErrors: [], errors: [] }
  sessions[i] = session
  const client = mc.createClient({ host, port: Number(port), username: 'bench' + i, version: VERSION, auth: 'offline', hideErrors: true, checkTimeoutInterval: keepAliveTimeout })
  client.on('packet', (data, meta, buffer) => {
    if (meta.state !== 'play') return
    if (!session.reachedPlay) session.firstPlayAtUnixMs = Date.now()
    session.reachedPlay = true
    const marker = meta.name === 'system_chat'
      ? JSON.stringify(data).match(/WATERBENCH_PHASE_(START|END):([A-Za-z0-9_.-]+):([A-Za-z0-9_.-]+):(\d+)/) : null
    if (marker && marker[1] === 'START') {
      if (session.activeScenario) session.phaseErrors.push('overlapping phase: ' + session.activeScenario.name)
      session.activeScenario = { label: marker[2], name: marker[3], sequence: Number(marker[4]), startedAtUnixMs: Date.now(), byType: {} }
      if (!measuring) { measuring = true; started = Date.now() }
    }
    // Общий счёт совместим с прежним режимом; точные интервалы задаются маркерами.
    if (measuring) {
      const bytes = buffer ? buffer.length : 0
      count(stats, meta.name, bytes)
      count(totals, meta.name, bytes)
      if (meta.name === 'explosion' && session.explosions.length < 256) {
        session.explosions.push({ center: data.center, playerKnockback: data.playerKnockback ?? null })
      }
    }
    if (session.activeScenario) count(session.activeScenario.byType, meta.name, buffer ? buffer.length : 0)
    if (marker && marker[1] === 'END') {
      const phase = session.activeScenario
      if (!phase || phase.label !== marker[2] || phase.name !== marker[3] || phase.sequence !== Number(marker[4])) {
        session.phaseErrors.push('unmatched phase end: ' + marker[3])
      } else {
        phase.endedAtUnixMs = Date.now()
        session.scenarios.push(phase)
        session.activeScenario = null
        client.write('custom_payload', { channel: 'watermc:bench_ack', data: Buffer.from(marker[2] + ':' + marker[3] + ':' + marker[4], 'utf8') })
      }
    }
    switch (meta.name) {
      case 'position':
        client.write('teleport_confirm', { teleportId: data.teleportId })
        break
      case 'chunk_batch_finished':
        // same value a vanilla client reports on a fast machine
        client.write('chunk_batch_received', { chunksPerTick: 25 })
        break
      case 'login':
        setTimeout(() => { try { client.write('player_loaded', {}) } catch (e) {} }, 500)
        break
    }
  })
  client.on('error', e => {
    session.errors.push({ atUnixMs: Date.now(), code: e.code || null, message: e.message, completedPhases: session.scenarios.length, activePhase: session.activeScenario ? session.activeScenario.name : null })
    console.error('bot', i, e.message)
  })
  client.on('end', reason => {
    session.ended = true
    session.endedAtUnixMs = Date.now()
    session.reason = reason || null
    console.error('bot', i, 'disconnected', reason)
    // the benchmark server shuts itself down when done: stop as soon as every bot is gone
    if (++ended === Number(botCount)) setTimeout(finish, 250)
  })
}

for (let i = 0; i < Number(botCount); i++) setTimeout(() => startBot(i), i * 300)

// measuring starts after all bots joined and loaded spawn chunks
setTimeout(() => { if (!measuring) { measuring = true; started = Date.now(); console.log('measuring') } }, Number(botCount) * 300 + 15000)

function dump () {
  const elapsed = Math.max(0.001, (Date.now() - (started || Date.now())) / 1000)
  const sorted = Object.entries(totals).sort((a, b) => b[1].bytes - a[1].bytes)
  const sum = sorted.reduce((acc, [, v]) => ({ count: acc.count + v.count, bytes: acc.bytes + v.bytes }), { count: 0, bytes: 0 })
  const result = {
    bots: Number(botCount),
    seconds: elapsed,
    byteMetric: 'decoded packet payload; excludes transport framing and compression',
    phaseMetric: 'per-client play packets between server START/END messages; includes scenario warmup',
    total: sum,
    perBotPerSecond: { packets: sum.count / Number(botCount) / elapsed, bytes: sum.bytes / Number(botCount) / elapsed },
    keepAliveTimeoutMs: keepAliveTimeout,
    clients: sessions.map((session, i) => ({ ...session, ...Object.values(perBot[i]).reduce((acc, value) => ({ count: acc.count + value.count, bytes: acc.bytes + value.bytes }), { count: 0, bytes: 0 }) })),
    byType: Object.fromEntries(sorted)
  }
  fs.writeFileSync(out, JSON.stringify(result, null, 2))
  return result
}

setInterval(dump, 10000)
function finish () { const r = dump(); console.log('done', JSON.stringify(r.total)); process.exit(0) }
setTimeout(finish, Number(seconds) * 1000)
process.on('SIGINT', finish)
process.on('SIGTERM', finish)
