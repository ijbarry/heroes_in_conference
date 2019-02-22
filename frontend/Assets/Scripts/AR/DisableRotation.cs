using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class DisableRotation : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
	// Lock screen rotation in the AR camera, as rotation resets and reloads the GameObjects
     	Screen.orientation = ScreenOrientation.Portrait;   
    }
}
