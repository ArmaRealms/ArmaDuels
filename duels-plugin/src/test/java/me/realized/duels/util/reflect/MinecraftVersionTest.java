package me.realized.duels.util.reflect;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MinecraftVersionTest {

    @Test
    public void preservesLegacyFeatureVersions() {
        assertEquals(8, MinecraftVersion.featureVersion("1.8.8"));
        assertEquals(13, MinecraftVersion.featureVersion("1.13.2"));
        assertEquals(21, MinecraftVersion.featureVersion("1.21.11"));
    }

    @Test
    public void yearBasedVersionsDoNotActivateLegacyCompatibility() {
        assertEquals(26, MinecraftVersion.featureVersion("26.1.2"));
        assertEquals(26, MinecraftVersion.featureVersion("26.2"));
        assertEquals(26, MinecraftVersion.featureVersion("26.2-pre1"));
        assertEquals(27, MinecraftVersion.featureVersion("27.1"));
    }
}
