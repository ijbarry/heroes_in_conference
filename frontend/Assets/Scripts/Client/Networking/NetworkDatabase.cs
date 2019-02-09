using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class NetworkDatabase : MonoBehaviour {
    public static NetworkDatabase NDB { get => NDB; private set => NDB = value; }
    private Database localDb;
    private Client client;

    void Awake() {
        if (NDB != null) {
            Debug.LogError("NetworkDatabase should be a singleton, deleting myself!");
            Destroy(this);
            return;
        }

        NDB = this;

        if (PlayerPrefs.HasKey("DB_File_Path")) {
            localDb = Database.LoadDatabase(PlayerPrefs.GetString("DB_File_Path"));
        } else {
            localDb = new Database();
        }

        client = new Client();
        // TODO: Authenticate with client
    }

    public void SetAchievement(long achievementID, bool wonAchievement = true) {
        localDb.SetAchievement(achievementID, wonAchievement);
        if(wonAchievement)
            client.CompleteAchievement(achievementID);
    }

    void Update() {
        
    }
}
