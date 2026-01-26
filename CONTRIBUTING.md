# Contributing to OTP Copy

Thank you for your interest in contributing to OTP Copy! This document provides guidelines and instructions for contributing to this project.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue with:
- A clear, descriptive title
- Steps to reproduce the bug
- Expected behavior
- Actual behavior
- Android version and device information
- Screenshots if applicable

### Suggesting Features

Feature suggestions are welcome! Please create an issue with:
- A clear description of the feature
- Use cases and benefits
- Any implementation ideas you have

### Code Contributions

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
4. **Test thoroughly**
5. **Commit with clear messages**
   ```bash
   git commit -m "Add feature: brief description"
   ```
6. **Push to your fork**
7. **Create a Pull Request**

## Development Setup

### Prerequisites
- Android Studio (latest stable version)
- JDK 8 or higher
- Android SDK with API 24 and API 34
- Git

### Setting Up the Project

1. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/android-otp-copy.git
   cd android-otp-copy
   ```

2. Open in Android Studio:
   - File → Open → Select the project directory
   - Wait for Gradle sync to complete

3. Run the app:
   - Connect an Android device or start an emulator
   - Click Run (green play button)

## Code Style

### Kotlin Style Guide

Follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html):

- Use 4 spaces for indentation
- Use camelCase for functions and variables
- Use PascalCase for classes
- Maximum line length: 120 characters

### Code Organization

- Keep classes focused and single-purpose
- Extract complex logic into separate functions
- Add comments for complex algorithms
- Use meaningful variable and function names

## Adding New OTP Patterns

To add support for new OTP patterns:

1. Open `OtpNotificationListener.kt`
2. Add your pattern to the `OTP_PATTERNS` list in the companion object
3. Test with various notification formats
4. Document the pattern in comments

Example:
```kotlin
// Matches "Confirmation code: 123456"
Pattern.compile("(?:confirmation code|confirm code)\\s*:?\\s*(\\d{4,8})", Pattern.CASE_INSENSITIVE)
```

## Testing

### Manual Testing Checklist

Before submitting a PR, test:

- [ ] App installs successfully
- [ ] Notification permission request works
- [ ] Permission status updates correctly
- [ ] OTPs are detected from test notifications
- [ ] OTPs are copied to clipboard
- [ ] Toast notification shows the copied OTP
- [ ] App works after device restart
- [ ] App works on different Android versions

### Testing OTP Detection

Create test notifications using ADB:

```bash
# Send a test notification with OTP
adb shell "cmd notification post -t 'Bank Alert' 'OTP' 'Your OTP is 123456'"
```

## Pull Request Guidelines

### Before Submitting

- Ensure your code compiles without errors
- Test on at least one physical device
- Update documentation if needed
- Add your changes to the "Unreleased" section of CHANGELOG.md (if it exists)

### PR Description

Include:
- Summary of changes
- Motivation and context
- Testing performed
- Screenshots (for UI changes)
- Related issues (use "Fixes #123" to auto-close issues)

### Review Process

- A maintainer will review your PR
- Address any feedback or requested changes
- Once approved, your PR will be merged

## Architecture Overview

### Key Components

1. **MainActivity**
   - Entry point of the app
   - Handles permission checking
   - Provides UI for settings access

2. **OtpNotificationListener**
   - Extends NotificationListenerService
   - Monitors incoming notifications
   - Extracts and copies OTPs

3. **OTP Pattern Matching**
   - Multiple regex patterns for flexibility
   - Supports various OTP formats
   - Validates OTP length (4-8 digits)

### Data Flow

```
Notification Arrives
    ↓
OtpNotificationListener.onNotificationPosted()
    ↓
Extract title, text, and bigText
    ↓
Try all OTP patterns
    ↓
First match found
    ↓
Copy to clipboard
    ↓
Show toast notification
```

## Improving OTP Detection

When improving OTP detection:

1. **Add patterns gradually**: Test each new pattern individually
2. **Consider false positives**: Ensure patterns don't match non-OTP numbers
3. **Prioritize specificity**: More specific patterns should come first in the list
4. **Test with real notifications**: Use actual banking/service notifications

## Security Considerations

When contributing, keep in mind:

- **No network access**: The app should never send data over the network
- **No persistent storage**: Don't store notification data
- **Minimal permissions**: Only request necessary permissions
- **User privacy**: Respect user privacy at all times

## Documentation

When adding features:

- Update README.md with user-facing changes
- Update USER_GUIDE.md with usage instructions
- Add code comments for complex logic
- Update this CONTRIBUTING guide if development process changes

## Questions?

If you have questions about contributing:
- Open a discussion on GitHub
- Check existing issues and PRs
- Review the code and documentation

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on the code, not the person
- Help create a welcoming environment

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (MIT License).

---

Thank you for contributing to OTP Copy! 🎉
