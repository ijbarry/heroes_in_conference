using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class KeyPad : MonoBehaviour
{
	public GameObject[] numpad;
	public GameObject sphere;
	public Material material;
	public AudioSource audioSource;
	public AudioClip audioClip;

	string buttonName;
	int[] inputCode;
	Renderer renderer;
	int pushCount;

	void Start()
	{
		inputCode = new int[4];
		renderer = sphere.GetComponent<Renderer>();
		pushCount = 0;
	}

	void Update()
	{
		// If 4 values have been entered, reset the counter, check if correct, act accordingly
		if (pushCount == 4)
		{
			pushCount = 0;
			// Change values for custom code
			if (inputCode[0] == 1 && inputCode[1] == 2 && inputCode[2] == 3 && inputCode[3] == 4)
			{
				renderer.material = material;
			}

			else
			{
				audioSource.clip = audioClip;
				audioSource.Play();
			}
		}

		if (Input.touchCount > 0 && Input.GetTouch(0).phase == TouchPhase.Began)
		{
			Ray ray = Camera.main.ScreenPointToRay(Input.GetTouch(0).position);

			if (Physics.Raycast(ray, out RaycastHit hit))
			{
				buttonName = hit.transform.name;
				
				// Find which button was pressed
				switch (buttonName)
				{
					case "button1" :
						inputCode[pushCount] = 1;
						pushCount++;
						break;
					case "button2" :
						inputCode[pushCount] = 2;
						pushCount++;
						break;
					case "button3" :
						inputCode[pushCount] = 3;
						pushCount++;
						break;
					case "button4" :
						inputCode[pushCount] = 4;
						pushCount++;
						break;
					case "button5" :
						inputCode[pushCount] = 5;
						pushCount++;
						break;
					case "button6" :
						inputCode[pushCount] = 6;
						pushCount++;
						break;
					case "button7" :
						inputCode[pushCount] = 7;
						pushCount++;
						break;
					case "button8" :
						inputCode[pushCount] = 8;
						pushCount++;
						break;
					case "button9" :
						inputCode[pushCount] = 9;
						pushCount++;
						break;
					case "button0" :
						inputCode[pushCount] = 0;
						pushCount++;
						break;
				}
			}
		} 
	}
}
