using System;
using System.Collections;
using System.Collections.Generic;

/// <summary>
/// Contains all information about locally stored Player
/// </summary>
[Serializable]
public class DBPlayer {
    private long playerID;
    public long PlayerID { get => playerID; }

    private string playerName;
    public string PlayerName { get => playerName; }
}
