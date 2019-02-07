using System;
using System.Collections;
using System.Collections.Generic;

/// <summary>
/// Contains all information about locally stored Achievement
/// </summary>
[Serializable]
public class DBAchievement {
    private long achievementID;
    public long AchievementID { get => achievementID; }

    private string achievementName;
    public string AchievementName { get => achievementName; }

    private string achievementDescription;
    public string AchievementDescription { get => achievementDescription; }

    private bool won;
    public bool Won { get => won; set => won = value; }

    public DBAchievement(long achievementID, string achievementName, string achievementDescription, bool wonAchievement) {
        this.achievementID = achievementID;
        this.achievementName = achievementName;
        this.achievementDescription = achievementDescription;
        this.won = wonAchievement;
    }
}
