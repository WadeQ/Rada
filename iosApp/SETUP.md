# iOS setup notes

The repo has the SwiftUI sources and an asset catalog but no `.xcodeproj` / `Info.plist`, so a
few things have to be wired when the Xcode project is created.

## Target settings
- **Deployment target: iOS 16 or later** (`PhotosPicker`, `onTapGesture(count:coordinateSpace:)`).
- **App icon:** Build Settings → *Primary App Icon Set Name* = `AppIcon`
  (single 1024×1024 opaque PNG in `Assets.xcassets/AppIcon.appiconset`).
- **Accent colour:** *Global Accent Color Name* = `AccentColor` (lime `#CCFF00`).

## Launch screen (the Twiga on ink, shown before the app draws)
Add to `Info.plist`:

```xml
<key>UILaunchScreen</key>
<dict>
    <key>UIColorName</key>
    <string>LaunchInk</string>
    <key>UIImageName</key>
    <string>LaunchTwiga</string>
    <key>UIImageRespectsSafeAreaInsets</key>
    <false/>
</dict>
```

`LaunchTwiga` is 250pt wide, the same size `SplashView.markSize` draws the Twiga, so the
hand-off from the launch screen to the in-app splash doesn't jump.

## Regenerating the icon and launch image
`TwigaMark.swift` is the single source of the logo (same coordinates as the Android vector
`ic_launcher_foreground.xml`). The PNGs in `Assets.xcassets` were rendered from it with
`ImageRenderer`; re-render them if the mark changes. App icons must have no alpha channel.

## Not yet on iOS
There is no iOS backend, location layer or map, so these Android features have no iOS
counterpart yet: live Firestore reports (Home's chart and alerts, and Report submissions, are
local/mock here), the Drive map with follow-my-location, and the GPS speed pipeline.
