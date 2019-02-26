using System.Linq;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using System.IO;

/// <summary>
/// Generates a GO for each map and fills dropdown selector
/// </summary>
public class MapsUiGenerator : MonoBehaviour {
    [SerializeField] RectTransform mapsParent;
    [SerializeField] Dropdown mapsSelector;
    [SerializeField] List<RectTransform> mapGameObjects;

    private void Start() {
        mapGameObjects = new List<RectTransform>();
        List<DBMap> allMaps = NetworkDatabase.NDB.GetMaps();
        foreach(DBMap map in allMaps) {
            RectTransform mapGO = buildMapPanel();
            mapGO.gameObject.SetActive(false);
            mapGameObjects.Add(mapGO);
            if (NetworkDatabase.NDB.TryDownloadMap(map)) {
                // The map path is local (downloaded)
                Image img = mapGO.gameObject.AddComponent<Image>();
                img.preserveAspect = true;
                img.sprite = loadNewSprite(map.FP.Path);
            } else {
                Text errText = mapGO.gameObject.AddComponent<Text>();
                errText.text = "Network (or maybe disk) error encountered when loading map!";
                errText.alignment = TextAnchor.MiddleCenter;
                errText.fontSize = 100;
            }
        }

        mapsSelector.ClearOptions();
        mapsSelector.AddOptions(allMaps.Select(map => map.MapName).ToList());
        mapsSelector.onValueChanged.AddListener(delegate {
            for (int i = 0; i < mapGameObjects.Count; i++)
                mapGameObjects[i].gameObject.SetActive(i == mapsSelector.value);
        });
        if(mapGameObjects.Count > 0)
            mapGameObjects[0].gameObject.SetActive(true);
    }

    #region Build GameObject
    private RectTransform buildMapPanel() {
        RectTransform mapPanel = new GameObject("Map Panel").AddComponent<RectTransform>();
        mapPanel.SetParent(mapsParent);
        setRectTransformPos(mapPanel, 0, 0, 1, 1);
        return mapPanel;
    }
    #endregion

    #region Map Image Loading
    /// <summary>
    /// Load a PNG or JPG image from disk to a Texture2D, assign this texture to a new sprite and return its reference
    /// </summary>
    /// <param name="filePath">Where to load from</param>
    /// <param name="pixelsPerUnit"></param>
    /// <returns>The loaded Dprite</returns>
    private Sprite loadNewSprite(string filePath, float pixelsPerUnit = 100.0f) {
        Texture2D spriteTexture = loadTexture(filePath);
        Sprite newSprite = Sprite.Create(spriteTexture, new Rect(0, 0, spriteTexture.width, spriteTexture.height), new Vector2(0, 0), pixelsPerUnit);

        return newSprite;
    }

    /// <summary>
    /// Load a PNG or JPG file from disk to a Texture2D
    /// </summary>
    /// <param name="filePath"></param>
    /// <returns>The loaded Texture2D, null if load failed</returns>
    private Texture2D loadTexture(string filePath) {
        Texture2D tex2D;
        byte[] fileData;

        if (File.Exists(filePath)) {
            fileData = File.ReadAllBytes(filePath);
            tex2D = new Texture2D(2, 2);           // Create new "empty" texture
            if (tex2D.LoadImage(fileData))           // Load the imagedata into the texture (size is set automatically)
                return tex2D;                 // If data = readable -> return texture
        }
        return null;                     // Return null if load failed
    }
    #endregion

    #region Utiliy
    private void setRectTransformPos(RectTransform rt, float x0, float y0, float x1, float y1) {
        rt.anchorMin = new Vector2(x0, y0);
        rt.anchorMax = new Vector2(x1, y1);
        rt.offsetMin = Vector2.zero;
        rt.offsetMax = Vector2.zero;
    }
    #endregion
}
