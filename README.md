# Do First

An Android app that stops you procrastinating. Add your tasks, and whenever you open Instagram, Reddit, a game, or any app you pick while tasks are still pending, your phone buzzes and a full red screen tells you which task to do first.

## What it does
- **Task list** — the top task is the one you're told to do first. Reorder with ↑, tick off with ✓.
- **Watcher** — notices the moment a distracting app opens, vibrates, and covers it with the "Do this first" screen. If you stay in the app, it nags again every minute.
- **On the nag screen** — "I'll do it now" sends you to the home screen, "✓ I finished it" ticks off the task, "Give me 5 minutes" snoozes once. Back button also sends you home.
- **Distractions** — Instagram and Reddit are on by default, every game is caught automatically, and you can tick any other app.
- When all tasks are done, it leaves you alone.

## Build the APK (no computer setup needed)
1. Put this folder in a GitHub repository.
2. GitHub Actions builds it automatically (about 5 minutes).
3. Open the repo's **Releases** page on your phone and download `DoFirst.apk`.

To build on a computer instead: open the folder in Android Studio and press Run.

## Install on your phone
1. Open `DoFirst.apk` and allow "install unknown apps" when asked.
2. Open Do First → **Turn on watcher** → find Do First under Accessibility → turn it on.
3. If it says "restricted setting": Settings → Apps → Do First → ⋮ → **Allow restricted settings**, then turn it on again.
4. On Xiaomi, Oppo, Vivo, Realme or OnePlus phones, also set Do First's battery to **No restrictions** / enable **Autostart**, or the phone may quietly switch the watcher off.

## Privacy
Do First only checks which app is open. It never reads your screen, and nothing leaves your phone.
