using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ResourceGather : MonoBehaviour
{
	public GameObject resource;
	public Material collected;	

	int hitCounter;
	Renderer sphereRenderer;

	void Start()
	{
		hitCounter = 0;
		sphereRenderer = resource.GetComponent<Renderer>();
	}

	void Update()
	{
		if (Input.touchCount > 0 && Input.GetTouch(0).phase == TouchPhase.Began)
		{
			Ray ray = Camera.main.ScreenPointToRay(Input.GetTouch(0).position);

			if (Physics.Raycast(ray, out RaycastHit hit))
			{
				hitCounter++;
			}

			if (hitCounter == 3)
			{
				sphereRenderer.material = collected;
			}
		}
	}
}
