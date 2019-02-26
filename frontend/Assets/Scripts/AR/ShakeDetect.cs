using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ShakeDetect : MonoBehaviour
{
	public GameObject sphere;
	public Material material;

	Renderer sphereRenderer;

	// How often to update the accelerometer
	float updateAcceleration = 1.0f / 60.0f;
	// Threshold for magnitude of shake vector
	float minShake;
	// Value of filter vector
	Vector3 lowPassValue;
	// Acceleration vectors
	Vector3 acceleration;
	Vector3 deltaAcceleration;

	void Start()
	{
		lowPassValue = Input.acceleration;
		// Recommended value according to certain manufacturers
		minShake = 2.0f;
		sphereRenderer = sphere.GetComponent<Renderer>();
		sphere.SetActive(false);
	}

	void Update()
	{
		acceleration = Input.acceleration;
		lowPassValue = Vector3.Lerp(lowPassValue, acceleration, updateAcceleration);
		deltaAcceleration = acceleration - lowPassValue;

		if (deltaAcceleration.sqrMagnitude >= minShake)
		{
			sphere.SetActive(true);
		}

		if (Input.touchCount > 0 && Input.GetTouch(0).phase == TouchPhase.Began)
		{
			Ray ray = Camera.main.ScreenPointToRay(Input.GetTouch(0).position);

			if (Physics.Raycast(ray, out RaycastHit hit))
			{
				if (hit.transform.name.Equals(sphere.name))
				{
					sphereRenderer.material = material;
				}
			}
		}
	}
}
