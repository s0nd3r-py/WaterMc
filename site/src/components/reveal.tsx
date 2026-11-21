import * as React from "react"

/** Fades a section in once when it scrolls into view; content stays visible without JS observers. */
export function Reveal({ as: Tag = "div", className, children, ...props }: React.HTMLAttributes<HTMLElement> & { as?: "div" | "section" }) {
  const ref = React.useRef<HTMLElement>(null)
  const [visible, setVisible] = React.useState(false)

  React.useEffect(() => {
    const node = ref.current
    if (!node || typeof IntersectionObserver === "undefined") {
      setVisible(true)
      return
    }
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setVisible(true)
          observer.disconnect()
        }
      },
      { rootMargin: "0px 0px -80px 0px" }
    )
    observer.observe(node)
    return () => observer.disconnect()
  }, [])

  return (
    <Tag ref={ref as React.Ref<HTMLDivElement>} data-visible={visible} className={["reveal", className].filter(Boolean).join(" ")} {...props}>
      {children}
    </Tag>
  )
}
