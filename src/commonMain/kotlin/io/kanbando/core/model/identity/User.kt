package io.kanbando.core.model.identity

/** Globally unique user identifier — UUID string, generated on first app install. */
typealias UserId = String

/**
 * A Kanbando user.
 *
 * [id] is the canonical, permanent identity — a UUID generated locally on first install.
 * [publicKey] is the Ed25519 public key derived from the user's private key.
 * Identity providers (OAuth) are optional verification mechanisms only.
 */
data class User(
    val id: UserId,
    /** Ed25519 public key, base64url encoded. Used to verify signed data. */
    val publicKey: String,
    val displayName: String,
    /** Optional OAuth provider links for cross-device verification and account recovery. */
    val identityProviders: List<IdentityProvider> = emptyList(),
)

data class IdentityProvider(
    val provider: String,   // "google" | "apple" | "github"
    val subject: String,    // provider's user ID
)
