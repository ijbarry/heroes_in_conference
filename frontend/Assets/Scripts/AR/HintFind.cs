using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class HintFind : MonoBehaviour
{
	public GameObject sphere;
	public Material collected;
	public AudioSource audioSource;
	public AudioClip complete;
	public AudioClip incomplete;

	string hintNode;
	Renderer sphereRenderer;

	void Start()
	{
		// Get the sphere renderer to access its material
		sphereRenderer = sphere.GetComponent<Renderer>();
		// Get the ImageTarget AudioSource component
		audioSource = gameObject.GetComponent<AudioSource>();
	}

	void Update()
	{
		// Only trigger if the first touch has just started, so holding down does not repeat
		if (Input.touchCount > 0 && Input.GetTouch(0).phase == TouchPhase.Began)
		{
			// Ray cast from Camera to touch position
			Ray ray = Camera.main.ScreenPointToRay(Input.GetTouch(0).position);

			if (Physics.Raycast(ray, out RaycastHit hit))
			{
				// Update the name of item hit
				hintNode = hit.transform.name;

				switch (hintNode)
				{
					case "hintX" :
						sphereRenderer.material = collected;
						
						// TODO inventory update, animation, add achievement, disintegration animation
						break;
				}
			}
		}
	}
}
