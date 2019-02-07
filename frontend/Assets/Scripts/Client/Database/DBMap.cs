using System;
using System.Collections;
using System.Collections.Generic;

/// <summary>
/// Contains all information about locally stored map graphics
/// </summary>
[Serializable]
public class DBMap {
    private long mapID;
    public long MapID { get => mapID; }

    private DateTime validBefore;
    public DateTime ValidBefore { get => validBefore; set => validBefore = value; }

    private string filePath;
    public string FilePath { get => filePath; }

    private string mapName;
    public string MapName { get => mapName; }
}
