﻿using System;
using System.Linq;
using System.Collections;
using System.IO;
using System.Collections.Generic;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;

[Serializable]
public class Database {
    public Database() {
        maps = new Dictionary<long, DBMap>();
        events = new Dictionary<long, DBEvent>();
        achievements = new Dictionary<long, DBAchievement>();
    }
    public static Database LoadDatabase(string path) {
        if (!File.Exists(path))
            return new Database();

        using (StreamReader streamReader = new StreamReader(path)) {
            BinaryFormatter binaryFormatter = new BinaryFormatter();
            Database db;
            try {
                db = (Database)binaryFormatter.Deserialize(streamReader.BaseStream);
                // TODO: catch error if not DB object
            }
            catch (SerializationException ex) {
                throw new SerializationException(((object)ex).ToString() + "\n" + ex.Source);
            }
            return db;
        }
    }

    #region Achievements
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
    #endregion

    #region Events
    private Dictionary<long, DBEvent> events;

    public void SetGoingEvent(long eventID, bool isGoing = true) {
        events[eventID].UserGoing = isGoing;
    }
    
    public List<DBEvent> GetCalendar() {
        return new List<DBEvent>(events.Values);
    }
    #endregion

    #region Maps
    private Dictionary<long, DBMap> maps;
    public List<DBMap> GetMaps() {
        return new List<DBMap>(maps.Values);
    }
    public void SetAllMaps(List<DBMap> allMaps) {
        foreach(DBMap map in allMaps) {
            maps.Add(map.MapID, map);
        }
    }
    #endregion
}
