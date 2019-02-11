using System;
using System.Collections;
using System.Collections.Generic;
using System.Net;
using System.Threading.Tasks;
using System.IO;
using Newtonsoft.Json;

public class Client {
    /// <summary>
    /// The server that the client will be talking to
    /// </summary>
    public string ServerAddress { get => (serverAddress + (ServerAddress.EndsWith("/") ? "" : "/")); set => serverAddress = value; }
    private string serverAddress;

    /// <summary>
    /// The session token that we get when authenticated with the server
    /// </summary>
    private string sessionToken;

    private string Get(string uri) {
        HttpWebRequest request = (HttpWebRequest)WebRequest.Create(uri + "?" + sessionToken);
        //request.AutomaticDecompression = DecompressionMethods.GZip | DecompressionMethods.Deflate;

        using (HttpWebResponse response = (HttpWebResponse)request.GetResponse())
        using (Stream stream = response.GetResponseStream())
        using (StreamReader reader = new StreamReader(stream)) {
            return reader.ReadToEnd();
        }
    }


    /// <summary>
    /// Sends a get request to server to fetch all the maps and then downloads all the map graphics
    /// </summary>
    /// <returns>List of maps we got from the server</returns>
    public List<DBMap> GetMaps() {
        string request = ServerAddress + "api/maps";
        dynamic jsonMaps = JsonConvert.DeserializeObject(Get(request));

        List<DBMap> maps = new List<DBMap>();
        //TODO: Parse json to maps
        return maps;
    }

    public bool CompleteAchievement(long achievementID) {
        // TODO: get user
        string request = ServerAddress + "api/" + "TODO: USER HERE" + "/achieved/" + achievementID;
        dynamic jsonAck = JsonConvert.DeserializeObject(Get(request));
        //bool success = jsonAck.Success;
        return true;
    }
}
