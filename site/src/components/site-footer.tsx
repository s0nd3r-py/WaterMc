import { Separator } from "@/components/ui/separator"
import { REPO } from "@/content"
import { useLang } from "@/lib/i18n"

const BASE = import.meta.env.BASE_URL

export function SiteFooter() {
  const { t } = useLang()
  const links = [
    { href: REPO, label: "GitHub" },
    { href: `${REPO}/releases`, label: t({ ru: "Релизы", en: "Releases" }) },
    { href: `${BASE}config/`, label: t({ ru: "Настройки", en: "Config" }) },
    { href: `${REPO}/blob/main/docs/about-watermc.md`, label: t({ ru: "Документация", en: "Docs" }) },
  ]
  return (
    <footer className="mt-24">
      <Separator />
      <div className="mx-auto flex max-w-6xl flex-col gap-6 px-4 py-10 text-sm text-muted-foreground sm:flex-row sm:justify-between sm:px-6">
        <div className="flex flex-col gap-1">
          <p className="font-medium text-foreground">
            WaterMc <span className="font-normal text-muted-foreground">· GPL-3.0 · © 2026 s0nd3r-py</span>
          </p>
          <p>{t({ ru: "Основан на Leaf, Gale, Purpur и Paper.", en: "Built on Leaf, Gale, Purpur and Paper." })}</p>
        </div>
        <div className="flex flex-col gap-2 sm:items-end">
          <nav aria-label={t({ ru: "Ссылки", en: "Links" })} className="flex flex-wrap gap-x-5 gap-y-1">
            {links.map((link) => (
              <a key={link.href} href={link.href} className="rounded-sm underline-offset-4 hover:text-foreground hover:underline focus-visible:ring-3 focus-visible:ring-ring/50 focus-visible:outline-none">
                {link.label}
              </a>
            ))}
          </nav>
          <p className="max-w-md sm:text-right">
            {t({
              ru: "Не является официальным продуктом Minecraft и не связан с Mojang или Microsoft.",
              en: "Not an official Minecraft product. Not affiliated with Mojang or Microsoft.",
            })}
          </p>
        </div>
      </div>
    </footer>
  )
}
