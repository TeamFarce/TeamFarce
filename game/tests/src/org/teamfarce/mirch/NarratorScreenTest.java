package org.teamfarce.mirch;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import org.junit.Before;
import org.junit.Test;
import org.teamfarce.mirch.entities.Suspect;
import org.teamfarce.mirch.screens.NarratorScreen;

import static org.junit.Assert.assertEquals;

public class NarratorScreenTest extends GameTest {
    private NarratorScreen screen;
    private MIRCH game;

    @Before
    public void init_tests() {
        Skin skin = new Skin();
        game = new MIRCH();
        game.setGameSnapshot(new GameSnapshot(null, null, null, null, null));
        game.getCurrentGameSnapshot().victim =
            new Suspect(game, "Test", "test", "Colin.png", new Vector2Int(0, 0), null);
        screen = new NarratorScreen(game, skin);
    }

    @Test
    public void setSpeech() {
        screen.setSpeech("Test Speech");

        assertEquals("Test Speech", screen.getSpeech());
    }

    @Test
    public void updateSpeech() {
        screen.setSpeech("Test Speech");
        screen.updateSpeech();

        assertEquals(screen.getCurrentSpeech(), "T");
    }

}
