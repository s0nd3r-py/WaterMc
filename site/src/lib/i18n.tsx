import * as React from "react"

export type Lang = "ru" | "en"
export type L = { ru: string; en: string }

const STORAGE_KEY = "watermc-lang"

function detectLang(): Lang {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved === "ru" || saved === "en") return saved
  } catch {
    // storage can be blocked (private mode); fall back to the browser language
  }
  const nav = (navigator.language || "en").toLowerCase()
  return nav.startsWith("ru") || nav.startsWith("uk") || nav.startsWith("be") ? "ru" : "en"
}

type LangState = { lang: Lang; setLang: (lang: Lang) => void; t: (text: L) => string }

const LangContext = React.createContext<LangState | undefined>(undefined)

export function LangProvider({ children }: { children: React.ReactNode }) {
  const [lang, setLangState] = React.useState<Lang>(detectLang)

  React.useEffect(() => {
    document.documentElement.lang = lang
  }, [lang])

  const setLang = React.useCallback((next: Lang) => {
    setLangState(next)
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      // ignore: the choice just won't be remembered
    }
  }, [])

  const t = React.useCallback((text: L) => text[lang], [lang])

  return <LangContext.Provider value={{ lang, setLang, t }}>{children}</LangContext.Provider>
}

export function useLang() {
  const ctx = React.useContext(LangContext)
  if (!ctx) throw new Error("useLang must be used inside LangProvider")
  return ctx
}
