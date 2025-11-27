# Enhanced Biometric Authentication in DHIS2 Android App

## 1. Summary

This document outlines the implementation of an enhanced biometric authentication system for the DHIS2 Android application. The goal was to move beyond simple biometric-as-password functionality to a more robust, cryptographically secure method for protecting user credentials.

The new implementation leverages Android's strongest security features by binding user authentication to hardware-backed cryptographic keys. This ensures that sensitive data, such as user passwords, can only be accessed after a successful and secure biometric verification.

## 2. What Was Implemented

### 2.1. Core Components

-   **`BiometricAuthenticator`**: A new class that interfaces with the Android `BiometricPrompt` API. It is strictly configured to only accept **`BIOMETRIC_STRONG`** (Class 3) authenticators, ensuring a high level of security. It is also capable of handling `CryptoObject`s, which is the cornerstone of this secure implementation.

-   **`CryptographyManager`**: This class is responsible for all cryptographic operations. Its key functions include:
    -   Generating a secret key using `KeyGenerator` and storing it in the `AndroidKeyStore`.
    -   Configuring the key to be authentication-bound, meaning it requires user authentication for every use (`setUserAuthenticationRequired(true)`).
    -   Encrypting and decrypting data using a `Cipher` that is unlocked by a successful biometric authentication.

-   **`PreferenceProvider` Integration**: The `PreferenceProvider` was extended to securely store the encrypted user credentials. It now has methods to save and retrieve a `CiphertextWrapper`, which contains the encrypted data and the initialization vector needed for decryption.

### 2.2. The User Flow

1.  **Enabling Biometric Authentication**: When a user with a `BIOMETRIC_STRONG` capable device logs in, they are prompted to enable biometric login. If they accept, the following happens:
    -   A new secret key is generated and stored in the `AndroidKeyStore`.
    -   The user's password is encrypted using this key.
    -   The encrypted password (and its initialization vector) is stored in `SharedPreferences`.

2.  **Logging in with Biometrics**: On subsequent app launches, the user is presented with the biometric prompt.
    -   Upon successful authentication, the `Cipher` object is unlocked by the Android system.
    -   This unlocked `Cipher` is then used to decrypt the stored password.
    -   The decrypted password is used to log the user in.

## 3. Why This Approach Was Chosen

### 3.1. Security

The primary driver for this implementation was to enhance security. The previous method of storing the password and using biometrics to retrieve it, while convenient, is less secure. This new approach offers several key security advantages:

-   **Hardware-Backed Security**: The cryptographic keys are stored in the `AndroidKeyStore`, which on most modern devices is a hardware-backed secure environment (like the TEE or Secure Element). This makes it extremely difficult for an attacker to extract the keys, even with root access to the device.
-   **Authentication-Bound Keys**: By binding the key to user authentication, we ensure that the key can only be used after the user has successfully authenticated. This is enforced by the Android OS itself, not just the app.
-   **No Plaintext Password Storage**: The user's password is never stored in plaintext. It is always encrypted, and can only be decrypted for a single login session after a successful biometric scan.

### 3.2. Best Practices

This implementation follows Google's recommended best practices for biometric authentication on Android. It uses the modern `BiometricPrompt` API, leverages the `AndroidKeyStore` for secure key storage, and uses `BIOMETRIC_STRONG` to ensure a high level of security.

## 4. Pros and Cons

### 4.1. Pros

-   **Greatly Enhanced Security**: As detailed above, this method is significantly more secure than simply using biometrics to unlock a stored password.
-   **Future-Proof**: By using the latest Android security APIs, this implementation is well-positioned to take advantage of future security enhancements in the Android platform.
-   **Improved User Trust**: For an app that handles sensitive health data, implementing the highest level of security can increase user trust and confidence.

### 4.2. Cons

-   **Limited to `BIOMETRIC_STRONG`**: This implementation will only work on devices that support `BIOMETRIC_STRONG`. While this is the most secure approach, it may exclude users with older devices or devices with less secure biometric hardware.
-   **Complexity**: The implementation is more complex than a simple password storage system. It requires a good understanding of Android's security and cryptography APIs.
-   **Key Invalidation**: If a user adds or removes a biometric from their device, the key we generated will be invalidated. The app must handle this case gracefully, likely by requiring the user to log in with their password and re-enable biometric authentication. The current implementation includes the `setInvalidatedByBiometricEnrollment(true)` flag to enable this, but the full user flow for re-enrollment would need to be considered.

## 5. Conclusion

The decision to implement a cryptographically secure biometric authentication system is a significant step forward for the security of the DHIS2 Android app. While it introduces some complexity and has limitations, the security benefits are substantial and align with the best interests of users who are entrusting the app with sensitive data. This implementation provides a strong foundation for a secure and user-friendly authentication experience. 