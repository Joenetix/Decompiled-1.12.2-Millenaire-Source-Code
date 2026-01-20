package org.millenaire.common.quest;

import org.millenaire.common.world.UserProfile;

/**
 * Represents an instance of a quest for a player.
 * Stub implementation - full functionality to be ported later.
 */
public class QuestInstance {
    public long uniqueid;
    public String questKey;
    public UserProfile profile;
    public int currentStep = 0;
    public boolean completed = false;

    public QuestInstance() {
    }

    public QuestInstance(String questKey, UserProfile profile) {
        this.questKey = questKey;
        this.profile = profile;
        this.uniqueid = System.currentTimeMillis();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        this.completed = true;
    }
}
