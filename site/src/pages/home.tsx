import { ArrowRightIcon, DownloadIcon, EyeOffIcon, FolderTreeIcon, GlobeIcon, MapIcon, ScrollTextIcon, ShieldCheckIcon, TriangleAlertIcon, UsersIcon, ZapIcon } from "lucide-react"
import { cn } from "cn"

import * as React from "react"

import { BootScreen } from "@/components/boot-screen"
import { CursorGlow } from "@/components/cursor-glow"
import { CopyCommand } from "@/components/copy-command"
import { Reveal } from "@/components/reveal"
import { GithubIcon } from "@/components/site-header"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { buttonVariants } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { DOWNLOAD, REPO, RUN_COMMAND, configGroups, faq, features, hero, inside, sources, steps } from "@/content"
import { useLang, type L } from "@/lib/i18n"

const BASE = import.meta.env.BASE_URL

const featureIcons: Record<string, React.ComponentType> = {
  map: MapIcon,
  zap: ZapIcon,
  users: UsersIcon,
  globe: GlobeIcon,
  "eye-off": EyeOffIcon,
  folder: FolderTreeIcon,
  shield: ShieldCheckIcon,
  scroll: ScrollTextIcon,
}

function SectionHeading({ eyebrow, title, text }: { eyebrow: L; title: L; text?: L }) {
  const { t } = useLang()
  return (
    <div className="mb-10 flex max-w-2xl flex-col gap-3">
      <p className="text-xs font-semibold tracking-[0.14em] text-primary uppercase">{t(eyebrow)}</p>
      <h2 className="text-3xl font-semibold tracking-tight text-balance sm:text-4xl">{t(title)}</h2>
      {text && <p className="text-base text-pretty text-muted-foreground">{t(text)}</p>}
    </div>
  )
}

/* hero entrance: non-uniform stagger, runs once (after the boot console when it plays) */
const delays = [0, 50, 90, 130, 170, 210]
const enter = (i: number) => ({ style: { "--delay": `${delays[i]}ms` } as React.CSSProperties })

const donorCount = sources.length
const keyCount = configGroups.reduce((sum, group) => sum + group.keys.length, 0)

function Hero() {
  const { t } = useLang()
  const stats: { value: string; label: L }[] = [
    { value: "1.21.11", label: { ru: "Версия Minecraft", en: "Minecraft version" } },
    { value: String(donorCount), label: { ru: "Проектов, из которых взяты оптимизации", en: "Projects we ported optimizations from" } },
    { value: String(keyCount), label: { ru: "Новых настроек в watermc.yml", en: "New settings in watermc.yml" } },
  ]
  const sectionRef = React.useRef<HTMLElement>(null)
  return (
    <section ref={sectionRef} className="relative isolate overflow-hidden border-b">
      <div aria-hidden="true" className="pointer-events-none absolute inset-0 -z-10">
        <div className="hero-grid absolute inset-0" />
        <CursorGlow containerRef={sectionRef} />
      </div>
      <div className="mx-auto max-w-6xl px-4 pt-20 pb-16 sm:px-6 sm:pt-28 lg:pt-36">
        <a
          href={`${REPO}/releases/latest`}
          {...enter(0)}
          className="enter inline-flex items-center gap-2 rounded-full border border-primary/25 bg-primary/10 py-1 pr-3 pl-1.5 text-xs font-medium text-primary transition-colors hover:bg-primary/15 focus-visible:ring-3 focus-visible:ring-ring/50 focus-visible:outline-none"
        >
          <img src={`${BASE}logo.png`} alt="" width={18} height={18} className="size-[18px] rounded-full" />
          {t({ ru: "Вышел 1.21.11-R1", en: "1.21.11-R1 released" })}
          <ArrowRightIcon className="size-3.5" aria-hidden="true" />
        </a>
        <h1
          {...enter(1)}
          className="enter mt-7 max-w-4xl text-5xl leading-[1.02] font-semibold tracking-[-0.04em] text-balance sm:text-7xl lg:text-[88px]"
        >
          {t({ ru: "Пусть сервер ", en: "Keep your server " })}
          <em className="bg-linear-to-br from-[oklch(0.9_0.1_210)] to-primary bg-clip-text pr-1 font-semibold text-transparent">
            {t({ ru: "течёт", en: "flowing" })}
          </em>
          {t({ ru: ", а лаги идут ко дну.", en: ", let lag sink." })}
        </h1>
        <p {...enter(2)} className="enter mt-7 max-w-xl text-lg text-pretty text-muted-foreground">
          {t(hero.lead)}
        </p>
        <div {...enter(3)} className="enter mt-10 flex flex-wrap gap-3">
          <a href={DOWNLOAD} className={cn(buttonVariants({ size: "lg" }), "h-12 px-6 text-[15px]")}>
            <DownloadIcon data-icon="inline-start" />
            {t({ ru: "Скачать", en: "Download" })}
          </a>
          <a href="#install" className={cn(buttonVariants({ variant: "outline", size: "lg" }), "h-12 px-6 text-[15px]")}>
            {t({ ru: "Как начать", en: "Get started" })}
            <ArrowRightIcon data-icon="inline-end" />
          </a>
          <a href={REPO} className={cn(buttonVariants({ variant: "outline", size: "lg" }), "h-12 px-6 text-[15px]")}>
            <GithubIcon data-icon="inline-start" />
            {t({ ru: "Открыть GitHub", en: "View on GitHub" })}
          </a>
        </div>
        <dl {...enter(4)} className="enter mt-20 grid overflow-hidden rounded-xl border bg-card/60 backdrop-blur-sm sm:grid-cols-3 lg:mt-28">
          {stats.map((stat, i) => (
            <div key={i} className={cn("flex flex-col gap-1.5 p-6 sm:p-7", i > 0 && "border-t sm:border-t-0 sm:border-l")}>
              <dt className="order-2 font-mono text-xs text-muted-foreground">{t(stat.label)}</dt>
              <dd className="order-1 font-mono text-4xl font-medium tracking-tight sm:text-5xl">{stat.value}</dd>
            </div>
          ))}
        </dl>
      </div>
    </section>
  )
}

function Features() {
  const { t } = useLang()
  return (
    <Reveal as="section" id="features" className="mx-auto max-w-6xl scroll-mt-20 px-4 py-20 sm:px-6">
      <SectionHeading
        eyebrow={{ ru: "Возможности", en: "Features" }}
        title={{ ru: "Всё, что даёт Leaf, и сверху ещё больше", en: "Everything Leaf gives you, and more on top" }}
        text={{
          ru: "WaterMc основан на Leaf (Paper → Purpur → Gale → Leaf) и переписывает горячие места сервера: генерацию мира, запуск, ИИ мобов и хранение данных.",
          en: "WaterMc builds on Leaf (Paper → Purpur → Gale → Leaf) and rewrites the server's hot spots: world generation, startup, mob AI and data storage.",
        }}
      />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {features.map((feature) => {
          const Icon = featureIcons[feature.icon]
          return (
            <Card key={feature.icon} className="lift">
              <CardHeader className="gap-3">
                <span className="grid size-9 place-items-center rounded-lg bg-primary/10 text-primary">
                  <Icon />
                </span>
                <CardTitle>{t(feature.title)}</CardTitle>
                <CardDescription className="leading-relaxed">{t(feature.text)}</CardDescription>
              </CardHeader>
            </Card>
          )
        })}
      </div>
    </Reveal>
  )
}

function Inside() {
  const { t } = useLang()
  return (
    <Reveal as="section" id="inside" className="mx-auto max-w-6xl scroll-mt-20 px-4 py-20 sm:px-6">
      <SectionHeading
        eyebrow={{ ru: "Под капотом", en: "Under the hood" }}
        title={{ ru: "Что переписано внутри", en: "What is rewritten inside" }}
      />
      <Tabs defaultValue={inside[0].id} className="gap-6">
        <TabsList className="h-auto max-w-full flex-wrap">
          {inside.map((tab) => (
            <TabsTrigger key={tab.id} value={tab.id}>
              {t(tab.label)}
            </TabsTrigger>
          ))}
        </TabsList>
        {inside.map((tab) => (
          <TabsContent key={tab.id} value={tab.id}>
            <div className="grid gap-4 md:grid-cols-2">
              {tab.items.map((item) => (
                <Card key={item.name} size="sm">
                  <CardHeader>
                    <CardTitle className="font-mono text-sm">{item.name}</CardTitle>
                    <CardDescription className="leading-relaxed">{t(item.text)}</CardDescription>
                  </CardHeader>
                </Card>
              ))}
            </div>
          </TabsContent>
        ))}
      </Tabs>
    </Reveal>
  )
}

function Sources() {
  const { t } = useLang()
  return (
    <Reveal as="section" id="sources" className="mx-auto max-w-6xl scroll-mt-20 px-4 py-20 sm:px-6">
      <SectionHeading
        eyebrow={{ ru: "Из модов и форков", en: "From mods and forks" }}
        title={{ ru: "Лучшее из экосистемы, переписанное под 1.21.11", en: "The best of the ecosystem, rewritten for 1.21.11" }}
        text={{ ru: "Авторы и лицензии каждой правки перечислены в репозитории.", en: "Authors and licenses of every change are listed in the repository." }}
      />
      <div className="grid gap-x-8 sm:grid-cols-2">
        {sources.map((source) => (
          <a
            key={source.name}
            href={source.url}
            className="group flex flex-col gap-1 rounded-lg border-b py-4 transition-colors focus-visible:ring-3 focus-visible:ring-ring/50 focus-visible:outline-none"
          >
            <span className="font-medium group-hover:text-primary">{source.name}</span>
            <span className="text-sm leading-relaxed text-muted-foreground">{t(source.text)}</span>
          </a>
        ))}
      </div>
    </Reveal>
  )
}

function Install() {
  const { t } = useLang()
  return (
    <Reveal as="section" id="install" className="mx-auto max-w-6xl scroll-mt-20 px-4 py-20 sm:px-6">
      <SectionHeading eyebrow={{ ru: "Установка", en: "Install" }} title={{ ru: "Три шага до запуска", en: "Three steps to launch" }} />
      <ol className="grid gap-4 md:grid-cols-3">
        {steps.map((step, i) => (
          <li key={i} className="flex min-w-0">
            <Card className="w-full min-w-0">
              <CardHeader className="gap-3">
                <span className="grid size-8 place-items-center rounded-lg bg-primary font-mono text-sm font-semibold text-primary-foreground">{i + 1}</span>
                <CardTitle>{t(step.title)}</CardTitle>
                <CardDescription className="leading-relaxed">{t(step.text)}</CardDescription>
              </CardHeader>
              {i === 0 && (
                <CardContent className="mt-auto">
                  <a href={DOWNLOAD} className={buttonVariants()}>
                    <DownloadIcon data-icon="inline-start" />
                    {t({ ru: "Скачать", en: "Download" })}
                  </a>
                </CardContent>
              )}
              {i === 1 && (
                <CardContent className="mt-auto">
                  <CopyCommand command={RUN_COMMAND} />
                </CardContent>
              )}
            </Card>
          </li>
        ))}
      </ol>
      <Alert className="mt-6">
        <TriangleAlertIcon />
        <AlertTitle>{t({ ru: "Сделайте бэкап", en: "Take a backup" })}</AlertTitle>
        <AlertDescription>
          {t({
            ru: "При первом запуске поверх старого сервера WaterMc переносит файлы по новым папкам.",
            en: "On the first start over an existing server, WaterMc moves files into the new folders.",
          })}
        </AlertDescription>
      </Alert>
    </Reveal>
  )
}

function Faq() {
  const { t } = useLang()
  return (
    <Reveal as="section" id="faq" className="mx-auto max-w-6xl scroll-mt-20 px-4 py-20 sm:px-6">
      <SectionHeading eyebrow={{ ru: "Вопросы", en: "FAQ" }} title={{ ru: "Частые вопросы", en: "Common questions" }} />
      <Accordion className="max-w-3xl">
        {faq.map((item, i) => (
          <AccordionItem key={i} value={`q${i}`}>
            <AccordionTrigger className="text-base">{t(item.q)}</AccordionTrigger>
            <AccordionContent className="text-muted-foreground">{t(item.a)}</AccordionContent>
          </AccordionItem>
        ))}
      </Accordion>
    </Reveal>
  )
}

export function HomePage() {
  return (
    <>
      <BootScreen />
      <Hero />
      <Features />
      <Inside />
      <Sources />
      <Install />
      <Faq />
    </>
  )
}
