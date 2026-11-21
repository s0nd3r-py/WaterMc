import { StrictMode } from "react"
import { createRoot } from "react-dom/client"

import "./index.css"
import { SiteFooter } from "@/components/site-footer"
import { SiteHeader } from "@/components/site-header"
import { ThemeProvider } from "@/components/theme-provider"
import { Toaster } from "@/components/ui/toast"
import { TooltipProvider } from "@/components/ui/tooltip"
import { LangProvider, useLang } from "@/lib/i18n"

function SkipLink() {
  const { t } = useLang()
  return (
    <a
      href="#main"
      className="sr-only rounded-md bg-primary px-3 py-2 text-primary-foreground focus:not-sr-only focus:fixed focus:top-3 focus:left-3 focus:z-50"
    >
      {t({ ru: "К содержимому", en: "Skip to content" })}
    </a>
  )
}

/** Shared shell for every page: providers, header, footer. */
export function mount(page: React.ReactNode) {
  createRoot(document.getElementById("root")!).render(
    <StrictMode>
      <ThemeProvider defaultTheme="system">
        <LangProvider>
          <TooltipProvider delay={400}>
            <Toaster>
              <SkipLink />
              <SiteHeader />
              <main id="main">{page}</main>
              <SiteFooter />
            </Toaster>
          </TooltipProvider>
        </LangProvider>
      </ThemeProvider>
    </StrictMode>
  )
}
