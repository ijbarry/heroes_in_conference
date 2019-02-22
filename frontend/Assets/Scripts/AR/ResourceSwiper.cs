using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ResourceSwiper : MonoBehaviour
{
	public GameObject resource;
	public Swipe swipe;
	public AudioClip chop;
	public AudioSource audioSource;

	int swipeCount;

	void Start()
	{
		swipeCount = 0;
		audioSource = gameObject.GetComponent<AudioSource>();
	}

	void Update()
	{
		if (swipe.GetLeft() || swipe.GetRight())
		{
			swipeCount++;
		}
		if (swipeCount > 3)
		{
			// Add to inventory
			resource.SetActive(false);
		}
	}
}
