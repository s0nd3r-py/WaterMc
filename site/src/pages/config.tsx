import * as React from "react"
import { SearchIcon } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { configGroups } from "@/content"
import { useLang } from "@/lib/i18n"

export function ConfigPage() {
  const { t } = useLang()
  const [query, setQuery] = React.useState("")
  const q = query.trim().toLowerCase()

  const groups = configGroups
    .map((group) => ({
      ...group,
      keys: group.keys.filter((k) => !q || k.key.toLowerCase().includes(q) || t(k.text).toLowerCase().includes(q)),
    }))
    .filter((group) => group.keys.length > 0)

  return (
    <div className="mx-auto max-w-6xl px-4 pt-16 sm:px-6">
      <div className="flex max-w-2xl flex-col gap-3">
        <p className="text-xs font-semibold tracking-[0.14em] text-primary uppercase">config/watermc.yml</p>
        <h1 className="text-4xl font-semibold tracking-tight sm:text-5xl">{t({ ru: "Справочник настроек", en: "Config reference" })}</h1>
        <p className="text-pretty text-muted-foreground">
          {t({
            ru: "Ключи, которые WaterMc добавляет к настройкам Leaf. Новые функции выключены по умолчанию, ядро стартует безопасно «из коробки».",
            en: "Keys WaterMc adds on top of Leaf's settings. New features are off by default, so the core starts safely out of the box.",
          })}
        </p>
      </div>

      <div className="relative mt-8 max-w-md">
        <SearchIcon className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
        <Input
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder={t({ ru: "Поиск по ключам и описанию", en: "Search keys and descriptions" })}
          aria-label={t({ ru: "Поиск настроек", en: "Search settings" })}
          className="h-10 pl-9"
        />
      </div>

      <div className="mt-10 flex flex-col gap-12">
        {groups.length === 0 && (
          <p className="text-muted-foreground">{t({ ru: "Ничего не найдено.", en: "Nothing found." })}</p>
        )}
        {groups.map((group) => (
          <section key={group.id} aria-labelledby={`g-${group.id}`}>
            <h2 id={`g-${group.id}`} className="mb-4 text-xl font-semibold tracking-tight">
              {t(group.title)}
            </h2>
            <div className="overflow-hidden rounded-xl border">
              <Table className="table-fixed">
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-[44%] pl-4">{t({ ru: "Ключ", en: "Key" })}</TableHead>
                    <TableHead className="w-28">{t({ ru: "По умолчанию", en: "Default" })}</TableHead>
                    <TableHead className="hidden pr-4 md:table-cell">{t({ ru: "Что делает", en: "What it does" })}</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {group.keys.map((k) => (
                    <TableRow key={k.key}>
                      <TableCell className="pl-4 align-top whitespace-normal">
                        <code className="font-mono text-[13px] text-foreground [overflow-wrap:anywhere]">
                          {/* break only after dots, never inside a word */}
                          {k.key.split(".").map((part, i, parts) => (
                            <React.Fragment key={i}>
                              {part}
                              {i < parts.length - 1 && (
                                <>
                                  .<wbr />
                                </>
                              )}
                            </React.Fragment>
                          ))}
                        </code>
                        <p className="mt-1.5 text-sm text-muted-foreground md:hidden">{t(k.text)}</p>
                      </TableCell>
                      <TableCell className="align-top">
                        <Badge variant={k.def === "true" ? "default" : "outline"} className="max-w-full font-mono">
                          <span className="truncate">{k.def}</span>
                        </Badge>
                      </TableCell>
                      <TableCell className="hidden pr-4 align-top text-sm whitespace-normal text-muted-foreground md:table-cell">{t(k.text)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </section>
        ))}
      </div>
    </div>
  )
}
