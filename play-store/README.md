# Play Store Graphics (§2.2)

Automated assets for the Google Play Console listing. Generated from real Compose UI via [Roborazzi](https://github.com/takahirom/roborazzi) + Robolectric.

## Asset specifications

| Asset | Size | Description |
|-------|------|-------------|
| Feature graphic | 1024×500 px | App name + hero dashboard on indigo/emerald gradient |
| Phone screenshots | 1080×1920 px | Minimum 4: Dashboard, Subscriptions, AI Chat, Renewals |
| Tablet screenshots | 1600×2560 px | Same four screens at tablet density |

## Generate assets

### Locally

From the project root:

```bash
./gradlew :app:recordRoborazziDebug --tests "com.example.playstore.*"
```

### On GitHub Actions (CI)

Instead of running Gradle tasks locally, you can run them via the **Play Store Graphics** GitHub Actions workflow:

1. **Record & Commit (Generate new screenshots):**
   - Navigate to your repository on GitHub.
   - Click on the **Actions** tab.
   - Select the **Play Store Graphics** workflow from the left sidebar.
   - Click **Run workflow**, select your target branch, choose `record` as the Action, and click the **Run workflow** button.
   - Once completed, the workflow will automatically commit the newly generated screenshots back to your repository and upload them as a workflow artifact.

2. **Verify (Check for regressions):**
   - The workflow runs automatically on every Pull Request to verify that any UI changes did not cause unexpected screenshot changes (regressions).
   - You can also trigger it manually by selecting `verify` from the manual trigger menu.

Output paths:

- `play-store/feature-graphic.png`
- `play-store/phone/01_dashboard.png` … `04_renewals.png`
- `play-store/tablet/01_dashboard.png` … `04_renewals.png`

Verify (CI / regression):

```bash
./gradlew :app:verifyRoborazziDebug --tests "com.example.playstore.*"
```

## Upload to Play Console

1. **Store presence → Main store listing → Graphics**
2. Feature graphic → `feature-graphic.png`
3. Phone screenshots → all four files under `phone/`
4. 7-inch / 10-inch tablet → files under `tablet/` (optional but recommended)

Screens use dark theme and seeded demo data (subscriptions + sample AI chat) so listings look populated without a live API key.
