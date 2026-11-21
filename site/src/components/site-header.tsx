import * as React from "react"
import { cn } from "cn"
import { MenuIcon, MoonIcon, SunIcon } from "lucide-react"

import { useTheme } from "@/components/theme-provider"
import { Button, buttonVariants } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { REPO } from "@/content"
import { useLang, type L, type Lang } from "@/lib/i18n"

const BASE = import.meta.env.BASE_URL

export const navLinks: { href: string; label: L }[] = [
  { href: `${BASE}#features`, label: { ru: "Возможности", en: "Features" } },
  { href: `${BASE}#inside`, label: { ru: "Под капотом", en: "Inside" } },
  { href: `${BASE}#install`, label: { ru: "Установка", en: "Install" } },
  { href: `${BASE}config/`, label: { ru: "Настройки", en: "Config" } },
  { href: `${BASE}#faq`, label: { ru: "Вопросы", en: "FAQ" } },
]

function GithubIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true" {...props}>
      <path d="M12 .5a11.5 11.5 0 0 0-3.64 22.41c.58.1.79-.25.79-.56v-2c-3.2.7-3.88-1.37-3.88-1.37-.53-1.33-1.28-1.69-1.28-1.69-1.05-.71.08-.7.08-.7 1.16.08 1.77 1.19 1.77 1.19 1.03 1.77 2.7 1.26 3.36.96.1-.75.4-1.26.73-1.55-2.55-.29-5.24-1.28-5.24-5.69 0-1.26.45-2.29 1.19-3.09-.12-.29-.52-1.46.11-3.05 0 0 .97-.31 3.17 1.18a11 11 0 0 1 5.77 0c2.2-1.49 3.17-1.18 3.17-1.18.63 1.59.23 2.76.11 3.05.74.8 1.19 1.83 1.19 3.09 0 4.42-2.69 5.39-5.26 5.68.41.36.78 1.06.78 2.14v3.17c0 .31.21.67.8.56A11.5 11.5 0 0 0 12 .5Z" />
    </svg>
  )
}
export { GithubIcon }

function LangToggle() {
  const { lang, setLang, t } = useLang()
  return (
    <ToggleGroup
      variant="outline"
      size="sm"
      spacing={0}
      value={[lang]}
      onValueChange={(value) => value[0] && setLang(value[0] as Lang)}
      aria-label={t({ ru: "Язык", en: "Language" })}
    >
      <ToggleGroupItem value="ru" aria-label="Русский">RU</ToggleGroupItem>
      <ToggleGroupItem value="en" aria-label="English">EN</ToggleGroupItem>
    </ToggleGroup>
  )
}

function ThemeToggle() {
  const { theme, setTheme } = useTheme()
  const { t } = useLang()
  const isDark = theme === "dark" || (theme === "system" && document.documentElement.classList.contains("dark"))
  const label = isDark ? t({ ru: "Светлая тема", en: "Light theme" }) : t({ ru: "Тёмная тема", en: "Dark theme" })
  return (
    <Tooltip>
      <TooltipTrigger
        render={<Button variant="ghost" size="icon" aria-label={label} onClick={() => setTheme(isDark ? "light" : "dark")} />}
      >
        {isDark ? <SunIcon /> : <MoonIcon />}
      </TooltipTrigger>
      <TooltipContent side="bottom">{label}</TooltipContent>
    </Tooltip>
  )
}

export function SiteHeader() {
  const { t } = useLang()
  const [menuOpen, setMenuOpen] = React.useState(false)
  return (
    <header className="sticky top-0 z-40 border-b bg-background/75 backdrop-blur-md supports-[backdrop-filter]:bg-background/60">
      <div className="mx-auto flex h-14 max-w-6xl items-center gap-6 px-4 sm:px-6">
        <a href={BASE} className="flex items-center gap-2.5 rounded-md font-semibold tracking-tight focus-visible:ring-3 focus-visible:ring-ring/50 focus-visible:outline-none">
          <img src={`${BASE}logo.png`} alt="" className="size-7 rounded-md" width={28} height={28} />
          <span>
            Water<span className="text-primary">Mc</span>
          </span>
        </a>
        <nav aria-label={t({ ru: "Основное меню", en: "Main" })} className="hidden items-center gap-1 md:flex">
          {navLinks.map((link) => (
            <a
              key={link.href}
              href={link.href}
              aria-current={link.href === `${BASE}config/` && location.pathname.endsWith("/config/") ? "page" : undefined}
              className={cn(buttonVariants({ variant: "ghost", size: "sm" }), "text-muted-foreground aria-[current=page]:text-foreground")}
            >
              {t(link.label)}
            </a>
          ))}
        </nav>
        <div className="ml-auto flex items-center gap-1.5">
          <LangToggle />
          <ThemeToggle />
          <a
            href={REPO}
            aria-label="GitHub"
            className={cn(buttonVariants({ variant: "ghost", size: "icon" }), "hidden sm:inline-flex")}
          >
            <GithubIcon />
          </a>
          <Sheet open={menuOpen} onOpenChange={setMenuOpen}>
            <SheetTrigger render={<Button variant="ghost" size="icon" className="md:hidden" aria-label={t({ ru: "Открыть меню", en: "Open menu" })} />}>
              <MenuIcon />
            </SheetTrigger>
            <SheetContent side="right">
              <SheetHeader>
                <SheetTitle>WaterMc</SheetTitle>
              </SheetHeader>
              <nav aria-label={t({ ru: "Основное меню", en: "Main" })} className="flex flex-col gap-1 px-4">
                {navLinks.map((link) => (
                  <a key={link.href} href={link.href} onClick={() => setMenuOpen(false)} className={cn(buttonVariants({ variant: "ghost" }), "h-10 justify-start text-base")}>
                    {t(link.label)}
                  </a>
                ))}
                <Separator className="my-2" />
                <a href={REPO} className={cn(buttonVariants({ variant: "ghost" }), "h-10 justify-start text-base")}>
                  <GithubIcon data-icon="inline-start" />
                  GitHub
                </a>
              </nav>
            </SheetContent>
          </Sheet>
        </div>
      </div>
    </header>
  )
}
