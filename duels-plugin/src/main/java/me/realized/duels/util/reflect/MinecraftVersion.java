package me.realized.duels.util.reflect;

final class MinecraftVersion {

    private MinecraftVersion() {
    }

    /** Returns the legacy minor version or the year for the 26.x version scheme. */
    static int featureVersion(final String version) {
        final String[] parts = version.split("\\.");
        final int major = Integer.parseInt(parts[0]);
        return major == 1 ? Integer.parseInt(parts[1]) : major;
    }
}
