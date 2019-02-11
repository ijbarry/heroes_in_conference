using System;
using System.Collections;
using System.Collections.Generic;

/// <summary>
/// Contains all information about locally stored Event
/// </summary>
[Serializable]
public class DBEvent : IComparable<DBEvent> {
    public readonly long EventID;
    public readonly DateTime StartTime;
    public readonly DateTime EndTime;
    public readonly string EventName;
    public readonly string EventDescription;

    private bool userGoing;
    public bool UserGoing { get => userGoing; set => userGoing = value; }

    public DBEvent(long eventID, DateTime startTime, DateTime endTime, string eventName, string eventDescription, bool userGoing = false) {
        EventID = eventID;
        StartTime = startTime;
        EndTime = endTime;
        EventName = eventName;
        EventDescription = eventDescription;
        this.userGoing = userGoing;
    }

    public int CompareTo(DBEvent other) {
        return StartTime.CompareTo(other.StartTime);
    }
}
