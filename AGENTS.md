# ikanbot-tv — AGENTS.md

## Project

Single-module Android TV app. Wraps `https://www.ikanbot.com/` in a WebView and
redirects video-playback URLs to the system browser (bypasses the site's security
verification).

- **Entrypoint**: `MainActivity` → loads `https://www.ikanbot.com/` in a `FocusableWebView`
- **Video redirect**: when a URL matches a video pattern (`/play/`, `.mp4`, `m3u8`, etc.)
  or a dynamically created `<iframe>`/`<video>` element is detected, `VideoPlayerActivity`
  plays it natively via ExoPlayer (Media3). Verification pages (captcha/验证) still open
  in the system browser via `BrowserHelperActivity` / `Intent.ACTION_VIEW`.
  Three interception layers:
  1. `shouldOverrideUrlLoading` — intercepts top-level navigation to video URLs
  2. `MutationObserver` + `window.open`/`location.assign` hook via injected JS — catches
     dynamically created iframe/video elements
  3. `@JavascriptInterface` bridge (`WebAppInterface`) — routes JS-detected URLs to native code
- **Antidetection JS**: injected after every `onPageFinished` — spoofs `webdriver`,
  `plugins`, `languages`, `platform`, `hardwareConcurrency`, blocks notification prompt
- **TV remote**: `dispatchKeyEvent` remaps DPAD_CENTER/ENTER to WebView focus;
  `FocusableWebView` draws a green outline on the focused element
- **3 source files** in `app/src/main/java/com/ikanbot/tv/`:
  `MainActivity.kt`, `FocusableWebView.kt`, `BrowserHelperActivity.kt`

## Build

**Gradle wrapper files are committed to the repo** (`gradlew`, `gradlew.bat`,
`gradle/wrapper/gradle-wrapper.jar`). No local Gradle installation needed:

```bash
./gradlew assembleDebug
```

| Command | Purpose |
|---|---|
| `./gradlew assembleDebug` | Debug APK |
| `./gradlew assembleRelease` | Release APK (needs `signing/release.jks`) |
| `./gradlew lint` | Lint |

- **JDK 17+** required. Android SDK API 34 + Build-Tools 34.x.
- Release build: `isMinifyEnabled = true`, ProGuard rules in `app/proguard-rules.pro`.
- Signing: run `signing/gen-key.bat` to create `signing/release.jks` (hardcoded
  passwords `ikanbot123`).

## Known config bugs — fix before first build

Three files contained literal `` `n `` (backtick-n) instead of actual newlines.
If `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`, or
`app/proguard-rules.pro` have properties on a single line separated by `` `n ``,
replace `` `n `` with real line breaks.

The `distributionUrl` in `gradle-wrapper.properties` originally used `\\://`
instead of `://` — this has been fixed to use `://`.

These bugs have been fixed in the current version of these files, but may
reappear if an editor saves them with literal backtick-n sequences.

## App structure

| Path | Role |
|---|---|
| `app/src/main/java/com/ikanbot/tv/MainActivity.kt` | WebView host, video URL detection, JS injection, shouldOverrideUrlLoading interception |
| `app/src/main/java/com/ikanbot/tv/FocusableWebView.kt` | Custom WebView: TV remote focus highlight |
| `app/src/main/java/com/ikanbot/tv/BrowserHelperActivity.kt` | Transparent activity → `Intent.ACTION_VIEW` (fallback for captcha/verify pages) |
| `app/src/main/java/com/ikanbot/tv/VideoPlayerActivity.kt` | ExoPlayer (Media3) native video player for intercepted video URLs |
| `app/src/main/res/layout/activity_main.xml` | Layout with top bar, loading overlay, error view |
| `app/src/main/res/layout/activity_video_player.xml` | ExoPlayer PlayerView layout |
| `app/src/main/res/values/` | colors.xml, strings.xml, themes.xml |
| `app/src/main/res/drawable/tv_banner.xml` | Leanback banner (references ic_launcher) |

## Key configuration

- `namespace = "com.ikanbot.tv"`, `applicationId = "com.ikanbot.tv"`
- `compileSdk = targetSdk = 34`, `minSdk = 21`
- Kotlin 1.9.20, Java 1.8 bytecode target
- `viewBinding = true`, `android.useAndroidX = true`
- No version catalog — plain dependency strings in `app/build.gradle.kts`
- Dependencies: `androidx.leanback`, `appcompat`, `constraintlayout`, `activity-ktx`,
  `lifecycle-viewmodel-ktx`, `core-ktx`, `media3-exoplayer`, `media3-exoplayer-hls`, `media3-ui`
- `usesCleartextTraffic="false"` in manifest (all traffic is HTTPS)

## Testing

`testImplementation("junit:junit:4.13.2")` is declared but **no test directory or
test files exist**. Any test infrastructure would need to be created from scratch.

## CI

Single workflow `.github/workflows/build.yml`:
- Triggers: push/PR to `main`/`master`, manual dispatch
- Generates wrapper, builds debug + release APK, uploads artifacts (30-day retention)

## Branch

Only `main` exists. No feature-branch or PR conventions documented.
