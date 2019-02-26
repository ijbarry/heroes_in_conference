using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class InteractiveTimetable : MonoBehaviour
{
	string eventName;

	void Start()
	{

	}

	void Update()
	{
		if (Input.touchCount > 0 && Input.GetTouch(0).phase == TouchPhase.Began)
		{
			Ray ray = Camera.main.ScreenPointToRay(Input.GetTouch(0).position);

			if (Physics.Raycast(ray, out RaycastHit hit))
			{
				// Find name of object hit
				eventName = hit.transform.name;

				switch (eventName)
				{
					case "eventName" :
						/*
						TODO
						Update timetable
						Play animation ? 
						*/
						break;
				}
			}
		}
	}
}
