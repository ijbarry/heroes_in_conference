using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MicDetect : MonoBehaviour
{
    public GameObject sphere;
    public Material material;

    private Renderer renderer;
    private string device;

    float volume;
    AudioClip record; //clipRecord
    bool isOn;

    void Start()
    {
        renderer = sphere.GetComponent<Renderer>();
    }

	//mic initialization
	public void InitMic()
	{
		if (device == null)
		{
			device = Microphone.devices[0];
		}
		record = Microphone.Start(device, true, 999, 44100);
		isOn = true;
	}

	public void StopMicrophone()
	{
		Microphone.End(device);
		isOn = false;
	}

	int sampleSize = 128;

	//get data from microphone into audioclip
	float MicrophoneLevelMax()
	{
		float levelMax = 0;
		float[] waveData = new float[sampleSize];
		int micPosition = Microphone.GetPosition(null) - (sampleSize + 1); // null means the first microphone
		if (micPosition < 0) return 0;
		record.GetData(waveData, micPosition);
		// Getting a peak on the last 128 samples
		for (int i = 0; i < sampleSize; i++)
		{
			float wavePeak = waveData[i] * waveData[i];
			if (levelMax < wavePeak)
			{
				levelMax = wavePeak;
			}
		}
		return levelMax;
	}

	void Update()
	{
		// levelMax equals to the highest normalized value power 2, a small number because < 1
		// pass the value to a static var so we can access it from anywhere
		volume = MicrophoneLevelMax();

		if (volume > 0.001 && renderer.isVisible)
		{
			renderer.material = material;
		}
	}

	// start mic when scene starts
	void OnEnable()
	{
		InitMic();
		isOn = true;
	}

	//stop mic when loading a new level or quit application
	void OnDisable()
	{
		StopMicrophone();
	}

	void OnDestroy()
	{
		StopMicrophone();
	}


	// make sure the mic gets started & stopped when application gets focused
	void OnApplicationFocus(bool focus)
	{
		if (focus)
		{
			if (!isOn)
			{
				InitMic();
			}
		}
		if (!focus)
		{
			StopMicrophone();
		}
	}
}
