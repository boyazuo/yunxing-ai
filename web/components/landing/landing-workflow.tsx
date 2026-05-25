interface Step {
  title: string
  description: string
}

interface LandingWorkflowProps {
  title: string
  subtitle: string
  steps: {
    one: Step
    two: Step
    three: Step
  }
}

const stepKeys = ['one', 'two', 'three'] as const
const stepNumbers = ['01', '02', '03']

export function LandingWorkflow({ title, subtitle, steps }: LandingWorkflowProps) {
  return (
    <section id="workflow" className="scroll-mt-24 py-24">
      <div className="mx-auto max-w-6xl px-6">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-3xl font-bold tracking-tight text-foreground md:text-4xl">
            {title}
          </h2>
          <p className="mt-4 text-lg text-muted-foreground">{subtitle}</p>
        </div>

        <ol className="mt-16 grid gap-12 md:grid-cols-3 md:gap-8">
          {stepKeys.map((key, index) => {
            const step = steps[key]
            const isLast = index === stepKeys.length - 1
            return (
              <li key={key} className="flex flex-col items-center text-center">
                <div className="relative flex w-full justify-center">
                  {!isLast && (
                    <div
                      className="pointer-events-none absolute top-1/2 left-[calc(50%+1.75rem)] hidden h-px w-[calc(100%+2rem-3.5rem)] -translate-y-1/2 bg-gradient-to-r from-primary/25 to-primary/25 md:block"
                      aria-hidden
                    />
                  )}
                  <span className="relative z-10 flex size-14 items-center justify-center rounded-2xl border border-primary/20 bg-background font-mono text-lg font-semibold text-primary shadow-sm ring-8 ring-background">
                    {stepNumbers[index]}
                  </span>
                </div>
                <h3 className="mt-8 text-lg font-semibold text-foreground">{step.title}</h3>
                <p className="mt-3 max-w-xs text-sm leading-relaxed text-muted-foreground">
                  {step.description}
                </p>
              </li>
            )
          })}
        </ol>
      </div>
    </section>
  )
}
