package ht.heist.heistclient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.swing.JOptionPane;
import java.awt.Desktop;

/**
 * Handles update checking for HeistClient by displaying informational popups about available updates.
 */
public final class SelfUpdate {
    
    private static final String GITHUB_RELEASES_BASE_URL = "https://api.github.com/repos/Tonic-Box/HeistClient/releases/tags";
    private static final String USER_AGENT = "HeistClient-Updater/1.0";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    
    private final HttpClient httpClient;

    public SelfUpdate() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
    
    /**
     * Checks for updates based on RuneLite version compatibility.
     * @return true if update is available, false if already up to date
     * @throws IOException if update check fails
     */
    public boolean checkAndUpdate() throws IOException {
        try {
            final String liveRLVersion = Versioning.getLiveRuneliteVersion();
            final String currentHeistClientVersion = Versioning.getHeistClientVersion();

            System.out.println("Live RuneLite version: " + liveRLVersion);
            System.out.println("Current HeistClient version: " + currentHeistClientVersion);

            // Check if HeistClient version matches RuneLite version
            if (currentHeistClientVersion.equals(liveRLVersion)) {
                System.out.println("HeistClient is already up to date for RuneLite v" + liveRLVersion);
                return false;
            }

            // Check if there's a HeistClient release for the live RL version
            final boolean releaseExists = checkIfReleaseExistsForRLVersion(liveRLVersion);

            if (!releaseExists) {
                // No HeistClient release available for current RL version
                showWaitForUpdateDialog(liveRLVersion);
                return false;
            }

            // Release exists - show update available dialog
            showUpdateAvailableDialog(currentHeistClientVersion, liveRLVersion, false);
            return true;

        } catch (final Exception e) {
            // Show "out of date" message for any errors (likely can't reach GitHub)
            showOutOfDateDialog();
            throw new IOException("Update check failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if a HeistClient release exists for the specified RuneLite version.
     * @param rlVersion the RuneLite version to check for
     * @return true if a release exists, false otherwise
     * @throws IOException if API request fails
     */
    private boolean checkIfReleaseExistsForRLVersion(final String rlVersion) throws IOException {
        try {
            final String releaseUrl = GITHUB_RELEASES_BASE_URL + "/" + rlVersion;
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(releaseUrl))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/vnd.github.v3+json")
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            final HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // 200 = release exists, 404 = release doesn't exist
            return response.statusCode() == 200;

        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Release check was interrupted", e);
        }
    }
    
    /**
     * Shows a dialog when update is available with option to open download page.
     * @param currentHeistClientVersion current HeistClient version string
     * @param targetRLVersion target RuneLite version string
     */
    public static void showUpdateAvailableDialog(final String currentHeistClientVersion, final String targetRLVersion, boolean vita) {
        try {
            String ident = vita ? "HeistClient" : "RuneLite";
            final int result = JOptionPane.showConfirmDialog(
                null,
                "<html><body style='width: 350px;'>" +
                "<h3>HeistClient Update Available</h3>" +
                "<p><b>Current HeistClient Version:</b> v" + currentHeistClientVersion + "</p>" +
                "<p><b>Target " + ident + " Version:</b> v" + targetRLVersion + "</p>" +
                "<br>" +
                "<p>A HeistClient release is available.</p>" +
                "<p>Would you like to open the download page?</p>" +
                "</body></html>",
                "Update Available",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                openReleasePageInBrowser(targetRLVersion);
            }

        } catch (final Exception e) {
            System.err.println("Failed to show update dialog: " + e.getMessage());
            showConsoleUpdateMessage(currentHeistClientVersion, targetRLVersion);
        }
    }

    /**
     * Shows a dialog when no HeistClient release exists for the current RuneLite version.
     * @param rlVersion the RuneLite version
     */
    private void showWaitForUpdateDialog(final String rlVersion) {
        try {
            JOptionPane.showMessageDialog(
                null,
                "<html><body style='width: 300px;'>" +
                "<h3>Please Wait for Update</h3>" +
                "<p><b>RuneLite Version:</b> v" + rlVersion + "</p>" +
                "<br>" +
                "<p>No HeistClient release is available for your current RuneLite version yet.</p>" +
                "<p>Please wait for an update to be released.</p>" +
                "</body></html>",
                "Wait for Update",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (final Exception e) {
            System.err.println("Failed to show wait dialog: " + e.getMessage());
            System.out.println("=== Please Wait for Update ===");
            System.out.println("RuneLite version: v" + rlVersion);
            System.out.println("No HeistClient release available for this RuneLite version yet.");
        }
    }
    
    /**
     * Shows a dialog when client is out of date (unable to check for updates).
     */
    private void showOutOfDateDialog() {
        try {
            JOptionPane.showMessageDialog(
                null,
                "<html><body style='width: 300px;'>" +
                "<h3>Update Check Failed</h3>" +
                "<p>Client may be out of date. Please check for updates manually.</p>" +
                "<br>" +
                "<p>Visit the HeistClient GitHub releases page for the latest version.</p>" +
                "</body></html>",
                "Update Check Failed",
                JOptionPane.WARNING_MESSAGE
            );

        } catch (final Exception e) {
            System.err.println("Failed to show out of date dialog: " + e.getMessage());
            System.out.println("=== Update Check Failed ===");
            System.out.println("Client may be out of date. Please check for updates manually.");
        }
    }
    
    /**
     * Opens the GitHub release page in the system's default browser.
     * @param version the version to link to
     */
    private static void openReleasePageInBrowser(final String version) {
        try {
            final String releaseUrl = "https://github.ht/heist-Box/HeistClient/releases/tag/" + version;

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(releaseUrl));
                System.out.println("Opened release page: " + releaseUrl);
            } else {
                System.out.println("Desktop browsing not supported. Release URL: " + releaseUrl);
            }

        } catch (final Exception e) {
            System.err.println("Failed to open browser: " + e.getMessage());
            System.out.println("Please visit: https://github.ht/heist-Box/HeistClient/releases/tag/v" + version);
        }
    }
    
    /**
     * Fallback console message when GUI dialogs fail.
     * @param currentVersion current version
     * @param latestVersion latest version
     */
    private static void showConsoleUpdateMessage(final String currentVersion, final String latestVersion) {
        System.out.println("=== HeistClient Update Available ===");
        System.out.println("Current: v" + currentVersion);
        System.out.println("Latest:  v" + latestVersion);
        System.out.println("Please visit: https://github.ht/heist-Box/HeistClient/releases/tag/v" + latestVersion);
    }
    
}
