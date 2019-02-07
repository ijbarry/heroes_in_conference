using System;
using System.Linq;
using System.Collections;
using System.Collections.Generic;

[Serializable]
public class Database {
    private Dictionary<long, DBMap> maps;
    private Dictionary<long, DBEvent> events;
    private Dictionary<long, DBAchievement> achievements;

    public void SetAchievement(long achievementID, bool wonAchievement = true) {
        if (!achievements.ContainsKey(achievementID))
            return;

        achievements[achievementID].Won = wonAchievement;
    }

    public void AddAchievement(long achievementID, string achievementName, string achievementDescription, bool wonAchievement = false) {
        if (achievements.ContainsKey(achievementID))
            return;

        achievements.Add(achievementID, new DBAchievement(achievementID, achievementName, achievementDescription, wonAchievement));
    }

    public List<DBAchievement> GetAllWonAchievements() {
        return achievements.Values.Where(a => a.Won).ToList();
    }
}
