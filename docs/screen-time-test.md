# Screen Time API Test

Week of Sep 17. Goal: make sure Android lets us see which app is open, because the whole lock feature depends on that.

## Plan for the lock

1. **Usage access** (`PACKAGE_USAGE_STATS`) lets us read `UsageStatsManager`. From that we get the app currently in the foreground and how long each app was used today.
2. **Display over other apps** (`SYSTEM_ALERT_WINDOW`) lets us put the lock screen on top of a blocked app. Android 10+ blocks starting activities from the background without it.
3. Later (Nov 5) a foreground service checks the top app about once a second. If it's on the blocked list and `bank` is 0, it shows the lock screen.

Both permissions have to be turned on by the user in Settings; the app can't ask with a normal popup. The test screen has buttons that open the correct settings page.

## How to run it

1. Run the app on a real phone (emulators work but don't have much usage history).
2. Sign in, finish onboarding, go to the **Lock** tab, tap **Screen time API test**.
3. Tap **Grant** for both permissions and turn Lock-In on in each settings page.
4. Open another app (YouTube, Instagram, etc.) for a few seconds, then come back.
5. That app should show up under **Recent apps** with the time you opened it, and **Today** should list your usage for the day.

Code: `lock/Usg.kt` (API calls) and `lock/UsgT.kt` (the screen).

## Results

| | |
|---|---|
| Phone / Android version | |
| Usage access works | |
| Overlay permission works | |
| Recent app shows up after switching | |
| Today's usage matches Digital Wellbeing | |
| Notes | |
