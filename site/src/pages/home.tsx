import { DownloadIcon, EyeOffIcon, FolderTreeIcon, GlobeIcon, MapIcon, ScrollTextIcon, ShieldCheckIcon, TriangleAlertIcon, UsersIcon, ZapIcon } from "lucide-react"
import { cn } from "cn"

import { CopyCommand } from "@/components/copy-command"
import { Reveal } from "@/components/reveal"
import { GithubIcon } from "@/components/site-header"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { buttonVariants } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { DOWNLOAD, REPO, RUN_COMMAND, faq, features, hero, inside, sources, steps } from "@/content"
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

/* hero entrance: non-uniform stagger (0 → 150ms), runs once */
const delays = [0, 50, 90, 120, 150]
const enter = (i: number) => ({ style: { "--delay": `${delays[i]}ms` } as React.CSSProperties })

function Hero() {
  const { t } = useLang()
  return (
    <section className="relative overflow-hidden">
      <div aria-hidden="true" className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute top-[-320px] left-1/2 size-[820px] -translate-x-1/2 rounded-full bg-[radial-gradient(circle,var(--brand-glow),transparent_65%)]" />
        <svg className="absolute bottom-0 left-0 h-48 w-full text-primary opacity-30" viewBox="0 0 1440 200" preserveAspectRatio="none">
          <path d="M0 120 C 240 60, 480 180, 720 120 S 1200 40, 1440 110" fill="none" stroke="currentColor" strokeWidth="1.5" />
          <path d="M0 160 C 300 100, 560 200, 860 150 S 1260 90, 1440 150" fill="none" stroke="currentColor" strokeWidth="1" opacity=".6" />
        </svg>
      </div>
      <div className="mx-auto flex max-w-6xl flex-col items-center px-4 pt-20 pb-24 text-center sm:px-6 sm:pt-28">
        <img
          src={`${BASE}logo.png`}
          alt=""
          width={88}
          height={88}
          {...enter(0)}
          className="enter size-22 rounded-3xl shadow-[0_0_60px_var(--brand-glow)]"
        />
        <h1 {...enter(1)} className="enter mt-8 text-6xl font-semibold tracking-[-0.035em] sm:text-7xl md:text-8xl">
          Water
          <span className="bg-linear-to-b from-[oklch(0.88_0.1_215)] to-primary bg-clip-text text-transparent">Mc</span>
        </h1>
        <p {...enter(2)} className="enter mt-6 max-w-2xl text-lg text-pretty text-muted-foreground sm:text-xl">
          {t(hero.lead)}
        </p>
        <div {...enter(3)} className="enter mt-10 flex flex-wrap justify-center gap-3">
          <a href={DOWNLOAD} className={cn(buttonVariants({ size: "lg" }), "h-11 px-5 text-[15px]")}>
            <DownloadIcon data-icon="inline-start" />
            {t(hero.download)}
          </a>
          <a href={REPO} className={cn(buttonVariants({ variant: "outline", size: "lg" }), "h-11 px-5 text-[15px]")}>
            <GithubIcon data-icon="inline-start" />
            GitHub
          </a>
        </div>
        <div {...enter(4)} className="enter mt-8 flex flex-wrap justify-center gap-2">
          {["Minecraft 1.21.11", "Java 21", "Paper · Spigot · Bukkit API", "GPL-3.0"].map((label) => (
            <Badge key={label} variant="secondary">
              {label}
            </Badge>
          ))}
        </div>
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
      <Hero />
      <Features />
      <Inside />
      <Sources />
      <Install />
      <Faq />
    </>
  )
}
