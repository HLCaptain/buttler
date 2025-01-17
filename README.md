# Butler AI Chat App

![Butler App Icon](docs/assets/butler_logo.webp)

Butler is an experimental AI chat app, which is a playground for testing out new technologies and development practices.

## How to Run

Easier way is to:

1. Go to recent [releases](https://github.com/HLCaptain/butler/releases/latest).
2. Download `Butler-windows-x64-2.0.0-release.jar`.
3. Start desktop app with Java 21 or higher. (I just opened it from my console/powershell using `java -jar` commands).
4. Download `composeApp-release.apk`.
5. Install the Android app on your device. (Android 8 required (API 26)).

If you want to build and run the app from source:

1. Clone the repository.
2. Open with [Android Studio](https://developer.android.com/studio).
3. Run the Android app on an emulator or a physical device. (You need to add some debug key info in `local.properties` and place your key in the `composeApp` project directory).
4. Run the Desktop app on your computer (with `butler:composeApp [run]` label in Android Studio).

![Select 'run'](docs/assets/select_run.png)

## Usage

A few key steps to showcase app features.

The demo version of the app available in the [releases page](https://github.com/HLCaptain/butler/releases/latest) starts out in the [model selection screen](#new-chat-and-model-selection) for creating a *New Chat*, because a credential key is already added (no-config).

### Adaptive Layout and Shared Element Transitions

On first open, the user is greeted with an onboarding flow, from which they can select 3 types of chatting:

- with locally available models (currently not working)
- models available via OpenAI API
- models available via Butler Server (currently not working)

The app supports adaptive layouts for different screen sizes. Remember, the app usage is shown using mostly the regular tablet/desktop layout, but the app is also optimized for mobile devices.

#### Tablet layout

![Tablet](docs/assets/tablet.png)

#### Mobile layout

![Mobile](docs/assets/mobile.png)

### Add OpenAI API credentials

Selecting the OpenAI API option, the user is prompted to enter their API key with their desired provider.

The credential grid list items are animating with shared element transitions.

The first API key should be added, which is for OpenRouter. You can use this API key, as it is limited to 1$ and I have no credits on my account. **Keep in mind, that you can only talk to `:free` models!**

![API key list](docs/assets/api_key_list.png)

Add more API keys by clicking the floating action button.

![Add API Key](docs/assets/add_api_key.png)

Press `Next` if finished on the credential list screen.

### New Chat and model selection

To start a chat, the user is prompted to select a model. The available models are listed by company.

![Model selection](docs/assets/model_selection.png)

The user can filter by text or select a "Free" filter to see only (possibly) free models.

![Model filtering](docs/assets/model_filtering.png)

For each model ID, there are available providers the user can select from.

![Provider selection](docs/assets/provider_selection.png)

The `New Chat` button is always visible from the navigation rail and drawer of the app to bring the user to the model selection screen.

When pressing the `Select Host` button, a new chat is opened.

### Chatting

![Empty Chat](docs/assets/empty_chat.png)

The user can record their voice and send it to the model. The appropriate model can also respond to images sent.

![Chat with image](docs/assets/chat_with_image.png)

The user can also open up the chat's details, by pressing the double arrow button on the top right.

![Chat details](docs/assets/chat_details.png)

For more complex things like audio transcription and image generation, the user can select the appropriate model.

![Model selection for chat](docs/assets/model_selection_chat.png)

### Profile and settings

The user can access their profile and settings by pressing the profile icon on the bottom left.

As we are not signed in, by pressing the `Login` button, we are taken back to the onboarding flow. Also no profile details are shown.

![Profile](docs/assets/profile.png)

After pressing `Settings`, the user can change the app's theme.

![Settings](docs/assets/settings_theme.png)

![Light theme](docs/assets/light_theme.png)

#### Dynamic Theming on Android

Not supported on Desktop, but Android features dynamic theming on recent releases.

![Dynamic theming off](docs/assets/dynamic_theming_off.png)

![Dynamic theming on](docs/assets/dynamic_theming_on.png)

## Contributing

See more information in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)
