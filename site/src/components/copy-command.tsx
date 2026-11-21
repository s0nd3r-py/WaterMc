import * as React from "react"
import { CheckIcon, CopyIcon } from "lucide-react"

import { Button } from "@/components/ui/button"
import { toast } from "@/components/ui/toast"
import { useLang } from "@/lib/i18n"

export function CopyCommand({ command }: { command: string }) {
  const { t } = useLang()
  const [copied, setCopied] = React.useState(false)

  async function copy() {
    try {
      await navigator.clipboard.writeText(command)
    } catch {
      toast.add({ title: t({ ru: "Не удалось скопировать", en: "Could not copy" }), type: "error" })
      return
    }
    setCopied(true)
    toast.add({ title: t({ ru: "Команда скопирована", en: "Command copied" }), type: "success" })
    window.setTimeout(() => setCopied(false), 1600)
  }

  return (
    <div className="flex items-center gap-2 rounded-lg border bg-muted/50 p-1.5 pl-3">
      <code tabIndex={0} className="min-w-0 flex-1 overflow-x-auto rounded-sm outline-none focus-visible:ring-3 focus-visible:ring-ring/50 py-1 font-mono text-[13px] leading-relaxed whitespace-pre text-foreground [scrollbar-width:none] [mask-image:linear-gradient(to_right,black_80%,transparent)]">{command}</code>
      <Button
        variant="ghost"
        size="icon-sm"
        onClick={copy}
        aria-label={t({ ru: "Скопировать команду", en: "Copy command" })}
      >
        {/* icon swap crossfades through a 2px blur so the two glyphs never read as two objects */}
        <span className="relative grid size-4 place-items-center">
          <CopyIcon
            className="col-start-1 row-start-1 transition-[opacity,filter,transform] duration-200 ease-(--ease-out) data-[copied=true]:scale-90 data-[copied=true]:opacity-0 data-[copied=true]:blur-[2px]"
            data-copied={copied}
          />
          <CheckIcon
            className="col-start-1 row-start-1 scale-90 text-primary opacity-0 blur-[2px] transition-[opacity,filter,transform] duration-200 ease-(--ease-out) data-[copied=true]:scale-100 data-[copied=true]:opacity-100 data-[copied=true]:blur-none"
            data-copied={copied}
          />
        </span>
      </Button>
    </div>
  )
}
