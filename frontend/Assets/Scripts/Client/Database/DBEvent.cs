using System;
using System.Collections;
using System.Collections.Generic;

/// <summary>
/// Contains all information about locally stored Event
/// </summary>
[Serializable]
public class DBEvent : IComparable<DBEvent> {
    private long eventID;
    public long EventID { get => eventID; }

    private DateTime startTime;
    public DateTime StartTime { get => startTime; set => startTime = value; }
    private DateTime endTime;
    public DateTime EndTime { get => endTime; set => endTime = value; }

    private string eventName;
    public string EventName { get => eventName; }

    private string eventDescription;
    public string EventDescription { get => eventDescription; }

    private bool userGoing;
    public bool UserGoing { get => userGoing; set => userGoing = value; }

    public int CompareTo(DBEvent other) {
        return StartTime.CompareTo(other.StartTime);
    }
}
