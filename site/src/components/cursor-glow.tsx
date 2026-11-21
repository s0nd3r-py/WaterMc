import * as React from "react"

/**
 * Lit grid that follows the pointer with a soft lag. Only this one childless layer's mask moves, so no other
 * element restyles per frame. Touch and reduced-motion users get a static glow on the right.
 */
export function CursorGlow({ containerRef }: { containerRef: React.RefObject<HTMLElement | null> }) {
  const layer = React.useRef<HTMLDivElement>(null)

  React.useEffect(() => {
    const host = containerRef.current
    const el = layer.current
    if (!host || !el) return
    const finePointer = window.matchMedia("(hover: hover) and (pointer: fine)").matches
    const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches
    if (!finePointer || reduced) return

    const home = () => ({ x: host.clientWidth * 0.82, y: host.clientHeight * 0.45 })
    let current = home()
    let target = home()
    let frame = 0

    const apply = () => {
      el.style.setProperty("--gx", `${current.x}px`)
      el.style.setProperty("--gy", `${current.y}px`)
    }
    const tick = () => {
      // ease toward the pointer: decorative, so momentum feels better than 1:1 tracking
      current = { x: current.x + (target.x - current.x) * 0.12, y: current.y + (target.y - current.y) * 0.12 }
      apply()
      frame = Math.abs(target.x - current.x) + Math.abs(target.y - current.y) > 0.5 ? requestAnimationFrame(tick) : 0
    }
    const kick = () => {
      if (!frame) frame = requestAnimationFrame(tick)
    }
    const onMove = (event: PointerEvent) => {
      const rect = host.getBoundingClientRect()
      target = { x: event.clientX - rect.left, y: event.clientY - rect.top }
      kick()
    }
    const onLeave = () => {
      target = home()
      kick()
    }

    apply()
    host.addEventListener("pointermove", onMove)
    host.addEventListener("pointerleave", onLeave)
    return () => {
      host.removeEventListener("pointermove", onMove)
      host.removeEventListener("pointerleave", onLeave)
      cancelAnimationFrame(frame)
    }
  }, [containerRef])

  return <div ref={layer} aria-hidden="true" className="hero-grid-glow pointer-events-none absolute inset-0" />
}
