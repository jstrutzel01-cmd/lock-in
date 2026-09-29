# Lock-In Database Design

Week of Sep 17. Firestore is the main store (Oct 8 hooks it up). A small local cache on the phone holds what the lock needs to work offline.

Kotlin models are in `app/src/main/java/com/example/lock_in/data/Mdl.kt`. Field names are kept short, so the key below explains them.

## Collections

```
users/{uid}                      Usr
users/{uid}/meals/{id}           Meal
users/{uid}/workouts/{id}        Wkt       (finished workouts, history)
users/{uid}/routines/{id}        Rtn       (custom routines the user built)
users/{uid}/lock/cfg             LCfg   (single doc)
users/{uid}/credits/{id}         Cred      (screen time earned / spent)

foods/{id}                       Food      (bundled reference data, read only)
exercises/{id}                   Ex        (read only)
presets/{id}                     Rtn       (preset routines, pre = true)
```

Everything a user owns sits under `users/{uid}`, so the security rule is just "you can only read and write your own uid". The three top level collections are shared reference data and are read only for clients.

## users/{uid}

| field | type | meaning |
|---|---|---|
| uid | string | Firebase Auth uid |
| nm | string | display name |
| em | string | email |
| age | int | years |
| sex | string | "M" or "F" (used for BMR) |
| ht | double | height in cm (always stored metric) |
| wt | double | weight in kg (always stored metric) |
| act | string | activity level: SED, LITE, MOD, HIGH, XTRA |
| goal | string | CUT, KEEP, BULK |
| kcal | int | daily calorie target |
| pro / carb / fat | int | daily macro targets in grams |
| onb | bool | finished onboarding |
| ts | long | created, epoch ms |

Targets are calculated once at onboarding (and again if the user edits their stats) and saved, so the nutrition screen does not recalculate every time.

## meals

| field | type | meaning |
|---|---|---|
| day | string | "yyyy-MM-dd", used to query one day |
| slot | string | breakfast, lunch, dinner, snack |
| fid | string | food id, empty if entered manually |
| nm | string | food name (copied so history still reads right if the food changes) |
| g | double | grams eaten |
| kcal / pro / carb / fat | double | totals for this entry (already scaled by grams) |
| ts | long | logged at |

Daily totals = sum of meals where `day == today`. Needs an index on `day`.

## foods (reference)

| field | type | meaning |
|---|---|---|
| nm | string | name |
| kcal / pro / carb / fat | double | per serving |
| srv | double | serving size in grams (default 100) |

## exercises (reference)

| field | type | meaning |
|---|---|---|
| nm | string | name |
| grp | string | muscle group (chest, back, legs, ...) |
| eq | string | equipment |

## routines and presets (Rtn)

| field | type | meaning |
|---|---|---|
| nm | string | routine name |
| grp | string | main muscle group |
| pre | bool | true for presets |
| ex | list of ExSt | exercises in order |

ExSt = `{ eid, nm, sets, reps, wt }` (wt in kg). Stored as an embedded list instead of a subcollection because a routine is always loaded as a whole and stays small.

## workouts (history)

| field | type | meaning |
|---|---|---|
| rid | string | routine it came from |
| nm | string | routine name at the time |
| day | string | "yyyy-MM-dd" |
| min | int | duration in minutes |
| ex | list of ExSt | what was actually done |
| earn | int | screen time minutes credited |
| ts | long | finished at |

## lock/cfg

| field | type | meaning |
|---|---|---|
| on | bool | locking enabled |
| apps | list of string | package names to block |
| bank | int | minutes available right now |
| wkm | int | minutes earned per completed workout |
| nutm | int | minutes earned for hitting the daily nutrition goal |
| ts | long | last updated |

## credits

| field | type | meaning |
|---|---|---|
| amt | int | minutes, positive = earned, negative = spent |
| src | string | "wkt", "nut" or "use" |
| ref | string | workout id or day, so the same thing is never credited twice |
| ts | long | when |

`bank` in lock/cfg is the running total. The credits list is the history behind it, so it can be rebuilt if it ever gets out of sync.

## Local cache (SQLite on device)

The blocking service has to decide in under a second whether to show the lock screen, and it can't wait on the network. So the phone keeps its own copy of:

- `lock_cfg` : on, apps, bank
- `usage` : pkg, day, ms spent (so minutes can be taken out of the bank)
- today's meals, so the nutrition screen works offline

Firestore stays the source of truth and the cache syncs when online. For onboarding (Oct 1) the profile is saved on the phone for now (`Prf`) and moves to `users/{uid}` on Oct 8.
