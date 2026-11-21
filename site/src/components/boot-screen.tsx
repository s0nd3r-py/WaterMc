import * as React from "react"

import { useLang } from "@/lib/i18n"

type Line = { tag?: string; text: string; tone?: "accent" | "ok" | "done" | "dim" }

/* Real lines from a WaterMc 1.21.11 startup log, trimmed for the screen. */
const LINES: Line[] = [
  { tag: "bootstrap", text: "Running Java 21 (OpenJDK 64-Bit Server VM, Eclipse Adoptium Temurin-21)" },
  { tag: "bootstrap", text: "Loading WaterMc 1.21.11-R1 for Minecraft 1.21.11", tone: "accent" },
  { tag: "WaterMc", text: "Loading config..." },
  { tag: "WaterMc", text: "Successfully loaded config", tone: "ok" },
  { tag: "PluginInitializerManager", text: "Initializing plugins..." },
  { text: "Loaded 1470 recipes" },
  { text: "Loaded 1584 advancements" },
  { text: "Starting minecraft server version 1.21.11" },
  { text: "This server is running WaterMc version 1.21.11-R1", tone: "accent" },
  { tag: "MoonriseCommon", text: "Preparing worlds with 4 chunk worker threads (2 after startup)" },
  { text: 'Preparing level "world"' },
  { text: "Preparing spawn area: 100%", tone: "ok" },
  { text: 'Done! For help, type "help"', tone: "done" },
]

const SESSION_KEY = "watermc-boot-shown"
const LINE_MS = [90, 110, 70, 80, 120, 60, 60, 110, 90, 130, 100, 160, 0] // uneven, like a real console
const HOLD_MS = 420
const EXIT_MS = 320

function shouldShow() {
  if (typeof window === "undefined") return false
  if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return false
  try {
    return sessionStorage.getItem(SESSION_KEY) !== "1" // once per visit, not on every page view
  } catch {
    return true
  }
}

function stamp(base: number, line: number) {
  return new Date(base + line * 137).toTimeString().slice(0, 8)
}

/** Intro: the page "boots" like a WaterMc console, then reveals the hero. Click, Esc or Enter skips it. */
export function BootScreen() {
  const { t } = useLang()
  const [show] = React.useState(shouldShow)
  const [count, setCount] = React.useState(0)
  const [leaving, setLeaving] = React.useState(false)
  const [gone, setGone] = React.useState(!show)
  const [base] = React.useState(Date.now)

  const finish = React.useCallback(() => {
    setLeaving(true)
    window.setTimeout(() => {
      setGone(true)
      delete document.documentElement.dataset.booting
    }, EXIT_MS)
    try {
      sessionStorage.setItem(SESSION_KEY, "1")
    } catch {
      // ignore
    }
  }, [])

  React.useEffect(() => {
    if (!show) {
      delete document.documentElement.dataset.booting
      return
    }
    document.documentElement.dataset.booting = "true"
    if (count < LINES.length) {
      const id = window.setTimeout(() => setCount((c) => c + 1), count === 0 ? 180 : LINE_MS[count - 1])
      return () => window.clearTimeout(id)
    }
    const id = window.setTimeout(finish, HOLD_MS)
    return () => window.clearTimeout(id)
  }, [show, count, finish])

  React.useEffect(() => {
    if (!show || leaving) return
    const skip = (event: KeyboardEvent) => {
      if (event.key === "Escape" || event.key === "Enter" || event.key === " ") finish()
    }
    window.addEventListener("keydown", skip)
    return () => window.removeEventListener("keydown", skip)
  }, [show, leaving, finish])

  if (gone) return null

  return (
    <div
      className="boot dark fixed inset-0 z-[60] grid place-items-center bg-background px-4 text-foreground"
      data-leaving={leaving}
      onClick={finish}
      role="presentation"
    >
      <div className="boot-window w-full max-w-2xl overflow-hidden rounded-xl border bg-card shadow-2xl shadow-black/40">
        <div className="flex items-center gap-2 border-b px-4 py-2.5">
          <span className="size-3 rounded-full bg-[oklch(0.68_0.19_25)]" />
          <span className="size-3 rounded-full bg-[oklch(0.82_0.16_85)]" />
          <span className="size-3 rounded-full bg-[oklch(0.75_0.17_150)]" />
          <span className="ml-2 font-mono text-xs text-muted-foreground">java -jar watermc-paperclip-1.21.11.jar --nogui</span>
        </div>
        <div className="h-[340px] overflow-hidden px-4 py-3 font-mono text-[12.5px] leading-[1.65] sm:text-[13px]" aria-hidden="true">
          {LINES.slice(0, count).map((line, i) => (
            <div key={i} className="boot-line flex gap-2 whitespace-pre-wrap">
              <span className="shrink-0 text-muted-foreground/70">[{stamp(base, i)} INFO]:</span>
              <span
                className={
                  line.tone === "done"
                    ? "font-semibold text-primary"
                    : line.tone === "accent"
                      ? "text-foreground"
                      : line.tone === "ok"
                        ? "text-[oklch(0.8_0.14_160)]"
                        : "text-muted-foreground"
                }
              >
                {line.tag && <span className="text-primary">[{line.tag}] </span>}
                {line.text}
              </span>
            </div>
          ))}
          {count >= LINES.length && <span className="boot-cursor mt-1 inline-block h-4 w-2 translate-y-0.5 bg-primary" />}
        </div>
      </div>
      <button
        type="button"
        onClick={finish}
        className="absolute right-4 bottom-4 rounded-md px-3 py-1.5 text-xs text-muted-foreground hover:text-foreground focus-visible:ring-3 focus-visible:ring-ring/50 focus-visible:outline-none"
      >
        {t({ ru: "Пропустить", en: "Skip" })} <kbd className="ml-1 rounded border px-1 font-mono text-[10px]">Esc</kbd>
      </button>
    </div>
  )
}
