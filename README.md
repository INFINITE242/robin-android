# Robin — Voice Assistant

Robin is an experimental Android voice assistant connected to an ESP32-C3
board with an SSD1306 OLED display. This repository is a learning project
for Android, voice interaction, APIs, and microcontroller integration.

## Current features

- Android interface with English, Hindi, and Kannada language options
- Android speech recognition and text-to-speech
- Gemini API integration for AI-generated replies
- Optional sending of replies to an ESP32 OLED over local Wi-Fi
- GitHub Actions workflow to build the Android APK

Features depend on device, network, API quota, and firmware configuration.
This project is a work in progress, not a finished offline assistant.

## Important: API costs and limits

The current AI implementation uses the Gemini API. Availability and free
usage limits are controlled by Google and can change. Free usage is not
unlimited, and requests may fail when quota is exhausted. Paid billing is
not required to contribute to this repository, but the current cloud AI
feature is not guaranteed to remain free or always available.

Never commit API keys, Wi-Fi passwords, signing keys, or personal tokens.
Configure your own API key locally in the app. Do not share screenshots
or logs containing secrets.

## Hardware

- ESP32-C3 SuperMini
- SSD1306 128x64 I2C OLED
- INMP441 I2S microphone
- MAX98357A I2S amplifier and compatible speaker

Example wiring used during development:

| Component | ESP32-C3 pin |
|---|---|
| OLED SDA | GPIO 10 |
| OLED SCL | GPIO 8 |
| INMP441 SCK | GPIO 4 |
| INMP441 WS | GPIO 5 |
| INMP441 SD | GPIO 6 |
| MAX98357A BCLK | GPIO 2 |
| MAX98357A LRC | GPIO 1 |
| MAX98357A DIN | GPIO 3 |
| Amplifier SD | GPIO 7 |

Check your exact board and module pinout before wiring. Use appropriate
power and common ground. Never connect 5 V to an ESP32 GPIO.

## ESP32 firmware

The firmware developed for this project uses Arduino libraries including
WiFi, WebServer, Wire, Adafruit GFX, and Adafruit SSD1306.

The prototype exposes:
- `GET /status` — basic online status
- `GET /reply?text=...` — displays a supplied reply on the OLED

These endpoints are intended for a trusted local network. Do not expose
the unauthenticated server to the public internet. Configure Wi-Fi
credentials locally and do not publish them.

The Android app's ESP32 address may need updating when the board's local
IP address changes.

## Build the Android app

1. Open the repository's Actions tab on GitHub.
2. Select the Android build workflow.
3. Run or wait for a build.
4. Download the `Robin-APK` artifact from a successful run.

The workflow and Android Gradle configuration are the source of truth
for the build environment. Install APKs only from sources you trust.

## Setup

1. Clone this repository.
2. Open it in a compatible Android development environment, or use the
   included GitHub Actions workflow.
3. Configure your own Gemini API key in the app if using cloud AI.
4. Upload the matching ESP32 firmware and configure local Wi-Fi if using
   the OLED integration.
5. Test each feature independently.

## Roadmap

- [ ] Document a repeatable clean build
- [ ] Verify Android-to-ESP32 communication on changing IP addresses
- [ ] Improve error messages and API quota handling
- [ ] Evaluate an actually offline AI option on supported phones
- [ ] Test English, Hindi, and Kannada speech separately
- [ ] Add screenshots and verified test results

## Contributing

Issues and pull requests are welcome. Please include reproduction steps
for bugs and never include API keys, passwords, or other secrets.

## License

No license has been declared yet. Until a license is added, do not assume
that the code is licensed for unrestricted reuse or redistribution.
